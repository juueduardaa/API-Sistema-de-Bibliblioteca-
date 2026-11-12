package com.senac.tsi.MinhaPrimeiraApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para criar ou atualizar um leitor")
public record LeitorRequest(
        @Schema(description = "Nome completo", example = "Ana Souza")
        @NotBlank @Size(min = 2, max = 100) String nome,
        @Schema(description = "E-mail único", example = "ana.souza@example.com")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Telefone (opcional)", example = "11999999999")
        @Size(max = 20)
        @Pattern(regexp = "^\\+?[0-9()\\- ]{8,20}$", message = "telefone deve conter de 8 a 20 dígitos, espaços, parênteses ou hífen")
        String telefone) {
}
