package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.GenerationProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Encapsula a lógica de negócio para gerar jogos com base em filtros.
 * Atua como um intermediário entre a ViewModel e o serviço GameGenerator,
 * seguindo os princípios da Clean Architecture.
 */
class GenerateGamesUseCase @Inject constructor(
    private val gameGenerator: GameGenerator
) {
    /**
     * Inicia a geração de jogos.
     * @param quantity A quantidade de jogos a serem gerados.
     * @param filters A lista de filtros a serem aplicados.
     * @return Um Flow que emite o progresso da geração.
     */
    operator fun invoke(quantity: Int, filters: List<FilterState>): Flow<GenerationProgress> {
        return gameGenerator.generate(quantity, filters)
    }
}