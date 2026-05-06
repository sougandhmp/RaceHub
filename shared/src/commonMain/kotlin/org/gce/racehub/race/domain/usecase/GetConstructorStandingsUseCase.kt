package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.repository.HomeRepository

class GetConstructorStandingsUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(): List<ConstructorStanding> = repository.getConstructorStandings()
}
