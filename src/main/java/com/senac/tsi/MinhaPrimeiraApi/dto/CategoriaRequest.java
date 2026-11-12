package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para criar ou atualizar uma categoria")
public record CategoriaRequest(
        @Schema(description = "Nome único da categoria", example = "Fantasia")
        @NotBlank @Size(min = 2, max = 80) String nome,
        @Schema(description = "Descrição opcional", example = "Livros de fantasia e aventura")
        @Size(max = 300) String descricao) {
}
