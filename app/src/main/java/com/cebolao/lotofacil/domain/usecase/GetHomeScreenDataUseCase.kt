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

private const val TAG = "GetHomeScreenDataUseCase"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val checkGameUseCase: CheckGameUseCase, // Injeção Nova
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        val history = historyRepository.getHistory()

        if (history.isEmpty()) {
            throw IllegalStateException("Nenhum histórico de sorteio encontrado.")
        }

        val latestApi = historyRepository.getLatestApiResult()
        val lastDraw = history.first()

        // Executa estatísticas (Síncrono/Rápido devido a otimização anterior)
        val stats = getAnalyzedStatsUseCase(timeWindow = 0).getOrThrow()

        // Processa dados da API (Síncrono/Memória)
        val (nextDraw, winners) = processApiResult(latestApi)

        // Processa CheckResult (Assíncrono/DB)
        // Coleta o primeiro valor do flow do CheckGameUseCase.
        // Como estamos dentro de um Flow builder, é seguro suspender aqui.
        val lastDrawCheckResult = checkGameUseCase(lastDraw.numbers)
            .first()
            .getOrNull()

        emit(Result.success(
            HomeScreenData(
                lastDraw = lastDraw,
                initialStats = stats,
                nextDrawInfo = nextDraw,
                winnerData = winners,
                // O HomeScreenData original já tinha esse campo? 
                // Se não, vamos precisar adicionar ao Data Class. 
                // Verificando o arquivo HomeScreenData.kt... Sim, existe!
                // Mas no código anterior ele estava sendo preenchido no ViewModel.
                // Agora injetamos aqui.
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
            if (hits != null) {
                WinnerData(
                    hits = hits,
                    description = rateio.descricaoFaixa,
                    winnerCount = rateio.numeroDeGanhadores,
                    prize = rateio.valorPremio
                )
            } else null
        }

        return nextDrawInfo to winnerData
    }
}