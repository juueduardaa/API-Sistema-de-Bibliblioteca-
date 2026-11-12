package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "categorias", itemRelation = "categoria")
@Schema(description = "Categoria de livros")
public record CategoriaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Fantasia") String nome,
        @Schema(example = "Livros de fantasia e aventura") String descricao,
        @Schema(description = "Quantidade de livros nesta categoria", example = "3") int quantidadeLivros) {

    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao(), c.getLivros().size());
    }
}
