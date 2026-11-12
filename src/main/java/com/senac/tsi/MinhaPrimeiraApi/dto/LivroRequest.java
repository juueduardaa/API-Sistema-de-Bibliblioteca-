package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "Dados para criar ou atualizar um livro")
public record LivroRequest(
        @Schema(description = "Título do livro", example = "O Hobbit")
        @NotBlank @Size(max = 180) String titulo,
        @Schema(description = "ISBN único (10 a 20 caracteres)", example = "978-0-261-10221-7")
        @NotBlank @Size(min = 10, max = 20) String isbn,
        @Schema(description = "Ano de publicação", example = "1937")
        @NotNull @Min(1400) @Max(2100) Integer anoPublicacao,
        @Schema(description = "ID de uma categoria existente", example = "1")
        @NotNull Long categoriaId,
        @Schema(description = "IDs de autores existentes (pelo menos um)", example = "[1]")
        @NotEmpty List<@NotNull Long> autoresIds) {
}
