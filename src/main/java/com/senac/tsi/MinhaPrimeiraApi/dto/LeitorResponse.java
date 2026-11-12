package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Leitor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "leitores", itemRelation = "leitor")
@Schema(description = "Leitor da biblioteca")
public record LeitorResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Ana Souza") String nome,
        @Schema(example = "ana.souza@example.com") String email,
        @Schema(example = "11999999999") String telefone,
        @Schema(description = "ID da carteirinha do leitor (nulo se ainda não possui)", example = "1") Long carteirinhaId) {

    public static LeitorResponse from(Leitor l) {
        Long carteirinhaId = l.getCarteirinha() != null ? l.getCarteirinha().getId() : null;
        return new LeitorResponse(l.getId(), l.getNome(), l.getEmail(), l.getTelefone(), carteirinhaId);
    }
}
