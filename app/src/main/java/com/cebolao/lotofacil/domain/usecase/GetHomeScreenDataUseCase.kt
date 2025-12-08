package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.util.Formatters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

private const val TAG = "GetHomeScreenDataUC"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val checkGameUseCase: CheckGameUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        val history = historyRepository.getHistory()
        if (history.isEmpty()) throw IllegalStateException("Nenhum histórico encontrado.")

        val lastDraw = history.first()
        val stats = getAnalyzedStatsUseCase(timeWindow = 0).getOrThrow()
        val (nextDraw, winners) = processApiResult(historyRepository.getLatestApiResult())
        
        val lastDrawCheckResult = checkGameUseCase(lastDraw.numbers).first().getOrNull()

        emit(Result.success(
            HomeScreenData(
                lastDraw = lastDraw,
                initialStats = stats,
                nextDrawInfo = nextDraw,
                winnerData = winners,
                lastDrawCheckResult = lastDrawCheckResult
            )
        ))
    }.catch { e ->
        Log.e(TAG, "Error generating home data", e)
        emit(Result.failure(e))
    }.flowOn(defaultDispatcher)

    private fun processApiResult(apiResult: LotofacilApiResult?): Pair<NextDrawInfo?, List<WinnerData>> {
        if (apiResult == null) return null to emptyList()

        val nextDrawInfo = if (apiResult.numero > 0) {
            NextDrawInfo(
                contestNumber = apiResult.numero + 1,
                formattedDate = apiResult.dataProximoConcurso ?: DEFAULT_PLACEHOLDER,
                formattedPrize = Formatters.formatCurrency(apiResult.valorEstimadoProximoConcurso),
                formattedPrizeFinalFive = Formatters.formatCurrency(apiResult.valorAcumuladoConcurso05)
            )
        } else null

        val winnerData = apiResult.listaRateioPremio.mapNotNull { rateio ->
            val hits = rateio.descricaoFaixa.filter { it.isDigit() }.toIntOrNull()
            hits?.let {
                WinnerData(
                    hits = it,
                    description = rateio.descricaoFaixa,
                    winnerCount = rateio.numeroDeGanhadores,
                    prize = rateio.valorPremio
                )
            }
        }
        return nextDrawInfo to winnerData
    }
}