package com.senac.tsi.MinhaPrimeiraApi.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo padrão devolvido em respostas de erro. */
@Schema(description = "Resposta padrão de erro da API")
public record ApiError(
        @Schema(description = "Momento do erro (ISO-8601)", example = "2026-10-11T14:30:00Z") String timestamp,
        @Schema(description = "Código HTTP", example = "404") int status,
        @Schema(description = "Descrição do código HTTP", example = "Not Found") String error,
        @Schema(description = "Mensagem explicando o problema", example = "Livro não encontrado com ID 99") String message) {
}
