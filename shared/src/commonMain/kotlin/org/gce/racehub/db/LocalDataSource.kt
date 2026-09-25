package org.gce.racehub.db

import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.TrendingThread
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.gce.racehub.util.platformIoDispatcher

/**
 * Reads and writes the cached race data in SQLDelight.
 *
 * `observe…()` Flows re-emit whenever the table changes, which is how screens pick up
 * the result of a refresh. `getAll…()` reads the current rows once; call it off the main thread.
 */
class LocalDataSource(
    driverFactory: DatabaseDriverFactory,
    private val ioDispatcher: CoroutineDispatcher = platformIoDispatcher
) {
    private val database = RaceHubDatabase(driverFactory.createDriver())
    private val dbQuery = database.raceHubDatabaseQueries

    fun observeRaces(): Flow<List<Race>> =
        dbQuery.getAllRaces().asFlow().mapToList(ioDispatcher).mapRows { it.toRace() }

    fun observeDriverStandings(): Flow<List<DriverStanding>> =
        dbQuery.getAllDriverStandings().asFlow().mapToList(ioDispatcher).mapRows { it.toDriverStanding() }

    fun observeConstructorStandings(): Flow<List<ConstructorStanding>> =
        dbQuery.getAllConstructorStandings().asFlow().mapToList(ioDispatcher).mapRows { it.toConstructorStanding() }

    fun observeTrendingThreads(): Flow<List<TrendingThread>> =
        dbQuery.getAllTrendingThreads().asFlow().mapToList(ioDispatcher).mapRows { it.toTrendingThread() }

    fun getAllRaces(): List<Race> = dbQuery.getAllRaces().executeAsList().map { it.toRace() }

    fun saveRaces(races: List<Race>) {
        dbQuery.transaction {
            dbQuery.deleteAllRaces()
            races.forEach {
                dbQuery.insertRace(
                    id = it.id,
                    name = it.name,
                    circuit = it.circuit,
                    country = it.country,
                    city = it.city,
                    dateTime = it.dateTime,
                    round = it.round.toLong(),
                    status = it.status,
                    weather = it.weather
                )
            }
        }
    }

    fun getAllDriverStandings(): List<DriverStanding> =
        dbQuery.getAllDriverStandings().executeAsList().map { it.toDriverStanding() }

    fun saveDriverStandings(standings: List<DriverStanding>) {
        dbQuery.transaction {
            dbQuery.deleteAllDriverStandings()
            standings.forEach {
                dbQuery.insertDriverStanding(
                    position = it.position.toLong(),
                    driverName = it.driverName,
                    team = it.team,
                    points = it.points.toLong(),
                    wins = it.wins.toLong()
                )
            }
        }
    }

    fun getAllConstructorStandings(): List<ConstructorStanding> =
        dbQuery.getAllConstructorStandings().executeAsList().map { it.toConstructorStanding() }

    fun saveConstructorStandings(standings: List<ConstructorStanding>) {
        dbQuery.transaction {
            dbQuery.deleteAllConstructorStandings()
            standings.forEach {
                dbQuery.insertConstructorStanding(
                    position = it.position.toLong(),
                    name = it.name,
                    points = it.points.toLong(),
                    wins = it.wins.toLong()
                )
            }
        }
    }

    fun getAllTrendingThreads(): List<TrendingThread> =
        dbQuery.getAllTrendingThreads().executeAsList().map { it.toTrendingThread() }

    fun saveTrendingThreads(threads: List<TrendingThread>) {
        dbQuery.transaction {
            dbQuery.deleteAllTrendingThreads()
            threads.forEach {
                dbQuery.insertTrendingThread(
                    id = it.id,
                    title = it.title,
                    likes = it.likes.toLong(),
                    createdAt = it.createdAt
                )
            }
        }
    }
}

private inline fun <R, T> Flow<List<R>>.mapRows(crossinline transform: (R) -> T): Flow<List<T>> =
    map { rows -> rows.map(transform) }

private fun RaceEntity.toRace() = Race(
    id = id,
    name = name,
    circuit = circuit,
    country = country,
    city = city,
    dateTime = dateTime,
    round = round.toInt(),
    status = status,
    weather = weather
)

private fun DriverStandingEntity.toDriverStanding() = DriverStanding(
    position = position.toInt(),
    driverName = driverName,
    team = team,
    points = points.toInt(),
    wins = wins.toInt()
)

private fun ConstructorStandingEntity.toConstructorStanding() = ConstructorStanding(
    position = position.toInt(),
    name = name,
    points = points.toInt(),
    wins = wins.toInt()
)

private fun TrendingThreadEntity.toTrendingThread() = TrendingThread(
    id = id,
    title = title,
    likes = likes.toInt(),
    createdAt = createdAt
)
