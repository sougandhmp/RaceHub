package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.repository.HomeRepository

class GetRaceDetailUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(slug: String): RaceDetail = repository.getRaceDetail(slug)
}
