package org.gce.racehub.race.data.repository

import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.repository.HomeRepository

/**
 * Fake in-memory implementation of [HomeRepository].
 *
 * Provides a realistic 2025 F1 season dataset without any network I/O.
 * Replace this with a real Ktor implementation when the API is ready —
 * nothing outside this class needs to change because the rest of the code
 * depends on the [HomeRepository] interface, not this concrete class.
 */
class HomeRepositoryImpl : HomeRepository {

    private val races: List<Race> = listOf(
        Race("1",  "Australian Grand Prix",      "Albert Park Circuit",              "Australia",   "🇦🇺", "Mar 16, 2025", round = 1,  isCompleted = true),
        Race("2",  "Chinese Grand Prix",         "Shanghai International Circuit",   "China",       "🇨🇳", "Mar 23, 2025", round = 2,  isCompleted = true),
        Race("3",  "Japanese Grand Prix",        "Suzuka Circuit",                   "Japan",       "🇯🇵", "Apr 6, 2025",  round = 3,  isCompleted = true),
        Race("4",  "Bahrain Grand Prix",         "Bahrain International Circuit",    "Bahrain",     "🇧🇭", "Apr 13, 2025", round = 4,  isCompleted = false),
        Race("5",  "Saudi Arabian Grand Prix",   "Jeddah Corniche Circuit",          "Saudi Arabia","🇸🇦", "Apr 20, 2025", round = 5,  isCompleted = false),
        Race("6",  "Miami Grand Prix",           "Miami International Autodrome",    "USA",         "🇺🇸", "May 4, 2025",  round = 6,  isCompleted = false),
        Race("7",  "Emilia Romagna Grand Prix",  "Autodromo Enzo e Dino Ferrari",    "Italy",       "🇮🇹", "May 18, 2025", round = 7,  isCompleted = false),
        Race("8",  "Monaco Grand Prix",          "Circuit de Monaco",                "Monaco",      "🇲🇨", "May 25, 2025", round = 8,  isCompleted = false),
        Race("9",  "Spanish Grand Prix",         "Circuit de Barcelona-Catalunya",   "Spain",       "🇪🇸", "Jun 1, 2025",  round = 9,  isCompleted = false),
        Race("10", "Canadian Grand Prix",        "Circuit Gilles Villeneuve",        "Canada",      "🇨🇦", "Jun 15, 2025", round = 10, isCompleted = false),
        Race("11", "Austrian Grand Prix",        "Red Bull Ring",                    "Austria",     "🇦🇹", "Jun 29, 2025", round = 11, isCompleted = false),
        Race("12", "British Grand Prix",         "Silverstone Circuit",              "UK",          "🇬🇧", "Jul 6, 2025",  round = 12, isCompleted = false),
    )

    private val standings: List<DriverStanding> = listOf(
        DriverStanding(position = 1,  driverName = "Max Verstappen",   team = "Red Bull Racing", points = 77,  flag = "🇳🇱"),
        DriverStanding(position = 2,  driverName = "Lando Norris",     team = "McLaren",         points = 72,  flag = "🇬🇧"),
        DriverStanding(position = 3,  driverName = "George Russell",   team = "Mercedes",        points = 60,  flag = "🇬🇧"),
        DriverStanding(position = 4,  driverName = "Charles Leclerc",  team = "Ferrari",         points = 55,  flag = "🇲🇨"),
        DriverStanding(position = 5,  driverName = "Carlos Sainz",     team = "Williams",        points = 50,  flag = "🇪🇸"),
        DriverStanding(position = 6,  driverName = "Oscar Piastri",    team = "McLaren",         points = 45,  flag = "🇦🇺"),
        DriverStanding(position = 7,  driverName = "Lewis Hamilton",   team = "Ferrari",         points = 40,  flag = "🇬🇧"),
        DriverStanding(position = 8,  driverName = "Kimi Antonelli",   team = "Mercedes",        points = 32,  flag = "🇮🇹"),
        DriverStanding(position = 9,  driverName = "Fernando Alonso",  team = "Aston Martin",    points = 24,  flag = "🇪🇸"),
        DriverStanding(position = 10, driverName = "Lance Stroll",     team = "Aston Martin",    points = 18,  flag = "🇨🇦"),
    )

    override fun getRaceSchedule(): List<Race> = races
    override fun getDriverStandings(): List<DriverStanding> = standings
    override fun getRaceCount(): Int = races.size
    override fun getRace(index: Int): Race = races[index]
    override fun getStandingCount(): Int = standings.size
    override fun getStanding(index: Int): DriverStanding = standings[index]
}
