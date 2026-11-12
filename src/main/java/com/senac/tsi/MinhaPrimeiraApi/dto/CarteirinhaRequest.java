package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "Dados para criar ou atualizar uma carteirinha")
public record CarteirinhaRequest(
        @Schema(description = "Código único da carteirinha", example = "CAR-0001")
        @NotBlank @Size(min = 4, max = 30) String codigo,
        @Schema(description = "Data de emissão (AAAA-MM-DD)", example = "2026-10-01")
        @NotNull LocalDate dataEmissao,
        @Schema(description = "Se a carteirinha está ativa", example = "true")
        @NotNull Boolean ativa,
        @Schema(description = "ID de um leitor existente que ainda não possui carteirinha", example = "1")
        @NotNull Long leitorId) {
}
