package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Carteirinha;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDate;

@Relation(collectionRelation = "carteirinhas", itemRelation = "carteirinha")
@Schema(description = "Carteirinha de um leitor")
public record CarteirinhaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "CAR-0001") String codigo,
        @Schema(example = "2026-10-01") LocalDate dataEmissao,
        @Schema(example = "true") Boolean ativa,
        @Schema(example = "1") Long leitorId,
        @Schema(example = "Ana Souza") String leitorNome) {

    public static CarteirinhaResponse from(Carteirinha c) {
        return new CarteirinhaResponse(c.getId(), c.getCodigo(), c.getDataEmissao(), c.getAtiva(),
                c.getLeitor().getId(), c.getLeitor().getNome());
    }
}
