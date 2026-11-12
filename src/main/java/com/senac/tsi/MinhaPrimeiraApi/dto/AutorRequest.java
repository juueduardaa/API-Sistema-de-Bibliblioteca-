package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para criar ou atualizar um autor")
public record AutorRequest(
        @Schema(description = "Nome do autor", example = "J. R. R. Tolkien")
        @NotBlank @Size(min = 2, max = 100) String nome,
        @Schema(description = "Nacionalidade (opcional)", example = "Britânica")
        @Size(max = 80) String nacionalidade) {
}
