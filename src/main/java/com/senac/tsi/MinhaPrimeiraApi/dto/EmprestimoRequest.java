package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "Dados para registrar um novo empréstimo")
public record EmprestimoRequest(
        @Schema(description = "Data do empréstimo (AAAA-MM-DD)", example = "2026-10-11")
        @NotNull LocalDate dataEmprestimo,
        @Schema(description = "Data prevista para devolução (não pode ser anterior ao empréstimo)", example = "2026-10-25")
        @NotNull LocalDate dataPrevistaDevolucao,
        @Schema(description = "ID de um leitor existente com carteirinha ativa", example = "1")
        @NotNull Long leitorId,
        @Schema(description = "ID de um livro existente e disponível", example = "1")
        @NotNull Long livroId) {
}
