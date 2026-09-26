package org.gce.racehub.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.db.inMemoryDriver
import org.gce.racehub.race.data.repository.RaceRepositoryNetworkImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The repository is the error boundary and owns the cache-vs-network rules,
 * so it is tested against a fake API (MockEngine) and a real in-memory database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RaceRepositoryNetworkImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = FakeApi(dispatcher)
    private val local = LocalDataSource(inMemoryDriver())
    private val repo = RaceRepositoryNetworkImpl(api.client, "https://api.test", local, dispatcher)

    private fun test(block: suspend TestScope.() -> Unit) = runTest(dispatcher) { block() }

    private val races = """
        {"data":{"races":[
          {"round":2,"slug":"chinese-grand-prix","grandPrix":"Chinese Grand Prix","circuit":"Shanghai","country":"China","city":"Shanghai","dateTime":"2026-03-15T07:00","status":"Completed","weather":"Clear"},
          {"round":5,"slug":"canadian-grand-prix","grandPrix":"Canadian Grand Prix","circuit":"Gilles Villeneuve","country":"Canada","city":"Montreal","dateTime":"2026-05-24T20:00","status":"Upcoming"}
        ]}}
    """.trimIndent()

    private val dashboard = """
        {"data":{"dashboard":{"seasonYear":2026,
          "driverStandings":[{"position":1,"name":"Kimi Antonelli","team":"Mercedes","points":72,"wins":2}],
          "constructorStandings":[{"position":1,"name":"Mercedes","points":135,"wins":3}],
          "trendingThreads":[{"id":"t1","title":"Miami thoughts","likes":3,"createdAt":"2026-05-06T02:47"}]}}}
    """.trimIndent()

    // ── Race schedule: cache first, errors as values ────────────────────────

    @Test
    fun `cold cache fetches maps and caches the schedule`() = test {
        api.responses["GetRaces"] = races
        val result = repo.getRaceSchedule()
        assertIs<DataResult.Success<*>>(result)
        assertEquals(listOf("chinese-grand-prix", "canadian-grand-prix"), local.getAllRaces().map { it.id })
        assertEquals("Canadian Grand Prix", local.getAllRaces().last().name)
    }

    @Test
    fun `offline on a cold cache is a Network failure`() = test {
        api.failures["GetRaces"] = FakeApi.Failure.Offline
        assertEquals(DataResult.Failure(DataError.Network), repo.getRaceSchedule())
    }

    @Test
    fun `HTTP 500 on a cold cache is a Server failure`() = test {
        api.failures["GetRaces"] = FakeApi.Failure.ServerError
        assertEquals(DataResult.Failure(DataError.Server), repo.getRaceSchedule())
    }

    @Test
    fun `GraphQL errors without data are a Server failure`() = test {
        api.responses["GetRaces"] = """{"errors":[{"message":"boom"}]}"""
        assertEquals(DataResult.Failure(DataError.Server), repo.getRaceSchedule())
    }

    @Test
    fun `a failed refresh still serves the cached schedule`() = test {
        api.responses["GetRaces"] = races
        repo.getRaceSchedule()
        api.failures["GetRaces"] = FakeApi.Failure.Offline

        val result = repo.getRaceSchedule()
        assertIs<DataResult.Success<*>>(result)
        assertEquals(2, (result as DataResult.Success).data.size)
    }

    // ── Dashboard: one request serves three reads ───────────────────────────

    @Test
    fun `cold standings and trending reads share a single dashboard request`() = test {
        api.responses["GetDashboard"] = dashboard
        val drivers = repo.getDriverStandings()
        val constructors = repo.getConstructorStandings()
        val trending = repo.getTrendingThreads()
        advanceUntilIdle()

        assertEquals("Kimi Antonelli", (drivers as DataResult.Success).data.single().driverName)
        assertEquals("Mercedes", (constructors as DataResult.Success).data.single().name)
        assertEquals("Miami thoughts", (trending as DataResult.Success).data.single().title)
        assertEquals(1, api.count("GetDashboard"))
    }

    @Test
    fun `warm reads within the freshness window send no dashboard request`() = test {
        api.responses["GetDashboard"] = dashboard
        repo.getDriverStandings()
        advanceUntilIdle()

        repo.getDriverStandings()
        repo.getConstructorStandings()
        advanceUntilIdle()
        assertEquals(1, api.count("GetDashboard"))
    }

    @Test
    fun `dashboard offline on a cold cache is a Network failure`() = test {
        api.failures["GetDashboard"] = FakeApi.Failure.Offline
        assertEquals(DataResult.Failure(DataError.Network), repo.getDriverStandings())
    }

    // ── Race detail ─────────────────────────────────────────────────────────

    @Test
    fun `race detail maps sessions and track facts`() = test {
        api.responses["GetRaceDetail"] = """
            {"data":{"race":{"grandPrix":"Canadian Grand Prix","circuit":"Gilles Villeneuve",
              "trackFacts":{"laps":70,"distanceKm":305.27,"corners":14},
              "sessions":[{"label":"Practice 1","dateTime":"2026-05-22T16:30"}]}}}
        """.trimIndent()
        val detail = (repo.getRaceDetail("canadian-grand-prix") as DataResult.Success).data
        assertEquals(70, detail.trackFacts?.laps)
        assertEquals("Practice 1", detail.sessions.single().label)
    }

    @Test
    fun `race detail GraphQL error is a Server failure`() = test {
        api.responses["GetRaceDetail"] = """{"data":null,"errors":[{"message":"not found"}]}"""
        assertEquals(DataResult.Failure(DataError.Server), repo.getRaceDetail("nope"))
    }
}
