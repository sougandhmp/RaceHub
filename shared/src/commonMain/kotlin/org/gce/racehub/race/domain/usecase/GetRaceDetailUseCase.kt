package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.repository.RaceRepository

class GetRaceDetailUseCase(private val repository: RaceRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(slug: String): DataResult<RaceDetail> = repository.getRaceDetail(slug)
}
