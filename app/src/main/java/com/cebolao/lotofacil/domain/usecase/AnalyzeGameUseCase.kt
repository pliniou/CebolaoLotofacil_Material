package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orquestra a análise completa de um único jogo.
 */
class AnalyzeGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    @get:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): Result<GameAnalysisResult> = withContext(dispatcher) {
        // Combinamos os dois resultados. Se algum falhar, o Result final é falha.
        runCatching {
            // first() pode lançar exceção se o flow estiver vazio, então runCatching é seguro aqui
            val checkResult = checkGameUseCase(game.numbers).first().getOrThrow()
            val simpleStats = getGameSimpleStatsUseCase(game).first().getOrThrow()

            GameAnalysisResult(
                game = game,
                simpleStats = simpleStats,
                checkResult = checkResult
            )
        }
    }
}