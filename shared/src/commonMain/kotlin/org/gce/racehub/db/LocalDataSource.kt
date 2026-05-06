package org.gce.racehub.db

import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.TrendingThread

class LocalDataSource(driverFactory: DatabaseDriverFactory) {
    private val database = RaceHubDatabase(driverFactory.createDriver())
    private val dbQuery = database.raceHubDatabaseQueries

    fun getAllRaces(): List<Race> {
        return dbQuery.getAllRaces().executeAsList().map {
            Race(
                id = it.id,
                name = it.name,
                circuit = it.circuit,
                country = it.country,
                countryFlag = it.countryFlag,
                date = it.date,
                round = it.round.toInt(),
                isCompleted = it.isCompleted == 1L,
                daysRemaining = it.daysRemaining?.toInt()
            )
        }
    }

    fun saveRaces(races: List<Race>) {
        dbQuery.transaction {
            dbQuery.deleteAllRaces()
            races.forEach {
                dbQuery.insertRace(
                    id = it.id,
                    name = it.name,
                    circuit = it.circuit,
                    country = it.country,
                    countryFlag = it.countryFlag,
                    date = it.date,
                    round = it.round.toLong(),
                    isCompleted = if (it.isCompleted) 1L else 0L,
                    daysRemaining = it.daysRemaining?.toLong()
                )
            }
        }
    }

    fun getAllDriverStandings(): List<DriverStanding> {
        return dbQuery.getAllDriverStandings().executeAsList().map {
            DriverStanding(
                position = it.position.toInt(),
                driverName = it.driverName,
                team = it.team,
                points = it.points.toInt(),
                wins = it.wins.toInt()
            )
        }
    }

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

    fun getAllConstructorStandings(): List<ConstructorStanding> {
        return dbQuery.getAllConstructorStandings().executeAsList().map {
            ConstructorStanding(
                position = it.position.toInt(),
                name = it.name,
                points = it.points.toInt(),
                wins = it.wins.toInt()
            )
        }
    }

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

    fun getAllTrendingThreads(): List<TrendingThread> {
        return dbQuery.getAllTrendingThreads().executeAsList().map {
            TrendingThread(
                id = it.id,
                title = it.title,
                likes = it.likes.toInt(),
                createdAt = it.createdAt
            )
        }
    }

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
