package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Livro;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;
import java.util.List;

@Relation(collectionRelation = "livros", itemRelation = "livro")
@Schema(description = "Livro do acervo")
public record LivroResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "O Hobbit") String titulo,
        @Schema(example = "978-0-261-10221-7") String isbn,
        @Schema(example = "1937") Integer anoPublicacao,
        @Schema(description = "false enquanto o livro estiver emprestado", example = "true") Boolean disponivel,
        @Schema(example = "1") Long categoriaId,
        @Schema(example = "Fantasia") String categoriaNome,
        List<AutorResumo> autores) {

    @Schema(description = "Resumo de um autor do livro")
    public record AutorResumo(@Schema(example = "1") Long id, @Schema(example = "J. R. R. Tolkien") String nome) {
    }

    public static LivroResponse from(Livro l) {
        List<AutorResumo> autores = l.getAutores().stream()
                .map(a -> new AutorResumo(a.getId(), a.getNome()))
                .toList();
        return new LivroResponse(l.getId(), l.getTitulo(), l.getIsbn(), l.getAnoPublicacao(), l.getDisponivel(),
                l.getCategoria().getId(), l.getCategoria().getNome(), autores);
    }
}
