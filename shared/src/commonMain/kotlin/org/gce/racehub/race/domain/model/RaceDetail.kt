package org.gce.racehub.race.domain.model

data class RaceDetail(
    val grandPrix: String,
    val circuit: String,
    val overview: String?,
    val trackFacts: TrackFacts?,
    val sessions: List<RaceSession>,
    val results: List<RaceResult>,
    val fastestLap: FastestLap?
)

data class TrackFacts(
    val laps: Int,
    val lapRecord: String?,
    val distanceKm: Double,
    val corners: Int
)

data class RaceSession(
    val label: String,
    val dateTime: String
)

data class RaceResult(
    val position: Int,
    val driver: String,
    val team: String,
    val points: Int,
    val time: String?
)

data class FastestLap(
    val driver: String,
    val time: String
)
