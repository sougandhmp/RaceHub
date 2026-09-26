package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.repository.RaceRepository

internal class GetConstructorStandingsUseCase(private val repository: RaceRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(): DataResult<List<ConstructorStanding>> = repository.getConstructorStandings()
}
