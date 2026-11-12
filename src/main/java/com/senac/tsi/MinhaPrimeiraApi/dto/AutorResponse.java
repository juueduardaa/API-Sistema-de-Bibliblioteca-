package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Autor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "autores", itemRelation = "autor")
@Schema(description = "Autor de livros")
public record AutorResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "J. R. R. Tolkien") String nome,
        @Schema(example = "Britânica") String nacionalidade) {

    public static AutorResponse from(Autor a) {
        return new AutorResponse(a.getId(), a.getNome(), a.getNacionalidade());
    }
}
