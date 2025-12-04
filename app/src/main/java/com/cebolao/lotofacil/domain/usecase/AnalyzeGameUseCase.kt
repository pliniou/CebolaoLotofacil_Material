package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnalyzeGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): Result<GameAnalysisResult> = withContext(dispatcher) {
        runCatching {
            val checkResult = checkGameUseCase(game.numbers).first().getOrThrow()

            val simpleStats = getGameSimpleStatsUseCase(game)

            GameAnalysisResult(
                game = game,
                simpleStats = simpleStats,
                checkResult = checkResult
            )
        }
    }
}