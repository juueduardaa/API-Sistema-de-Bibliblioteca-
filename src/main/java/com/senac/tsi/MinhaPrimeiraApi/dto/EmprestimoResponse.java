package com.senac.tsi.MinhaPrimeiraApi.dto;

import com.senac.tsi.MinhaPrimeiraApi.model.Emprestimo;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDate;

@Relation(collectionRelation = "emprestimos", itemRelation = "emprestimo")
@Schema(description = "Empréstimo de um livro a um leitor")
public record EmprestimoResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "2026-10-11") LocalDate dataEmprestimo,
        @Schema(example = "2026-10-25") LocalDate dataPrevistaDevolucao,
        @Schema(example = "2026-10-20") LocalDate dataDevolucao,
        @Schema(example = "ATIVO") StatusEmprestimo status,
        @Schema(example = "1") Long leitorId,
        @Schema(example = "Ana Souza") String leitorNome,
        @Schema(example = "1") Long livroId,
        @Schema(example = "O Hobbit") String livroTitulo) {

    public static EmprestimoResponse from(Emprestimo e) {
        return new EmprestimoResponse(e.getId(), e.getDataEmprestimo(), e.getDataPrevistaDevolucao(),
                e.getDataDevolucao(), e.getStatus(), e.getLeitor().getId(), e.getLeitor().getNome(),
                e.getLivro().getId(), e.getLivro().getTitulo());
    }
}
