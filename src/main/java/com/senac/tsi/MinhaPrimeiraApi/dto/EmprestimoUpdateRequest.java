package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import java.time.LocalDate;

@Schema(description = "Dados que podem ser alterados em um empréstimo existente")
public record EmprestimoUpdateRequest(
        @Schema(description = "Nova data prevista de devolução", example = "2026-11-01")
        @NotNull LocalDate dataPrevistaDevolucao,
        @Schema(description = "Novo status do empréstimo", example = "ATIVO")
        @NotNull StatusEmprestimo status,
        @Schema(description = "Data da devolução (usada quando status = DEVOLVIDO; se omitida, assume a data de hoje)", example = "2026-10-20")
        LocalDate dataDevolucao) {
}
