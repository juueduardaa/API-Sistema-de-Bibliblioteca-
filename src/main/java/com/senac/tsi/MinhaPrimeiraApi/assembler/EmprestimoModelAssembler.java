package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.LeitorController;
import com.senac.tsi.MinhaPrimeiraApi.controller.LivroController;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import com.senac.tsi.MinhaPrimeiraApi.controller.EmprestimoController;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Emprestimo. */
@Component
public class EmprestimoModelAssembler implements RepresentationModelAssembler<EmprestimoResponse, EntityModel<EmprestimoResponse>> {

    @Override
    public EntityModel<EmprestimoResponse> toModel(EmprestimoResponse emprestimo) {
        EntityModel<EmprestimoResponse> model = EntityModel.of(emprestimo,
                linkTo(EmprestimoController.class).slash(emprestimo.id()).withSelfRel(),
                linkTo(EmprestimoController.class).withRel("emprestimos"),
                linkTo(EmprestimoController.class).slash(emprestimo.id()).withRel("update"),
                linkTo(EmprestimoController.class).slash(emprestimo.id()).withRel("delete"));
        model.add(linkTo(LeitorController.class).slash(emprestimo.leitorId()).withRel("leitor"));
        model.add(linkTo(LivroController.class).slash(emprestimo.livroId()).withRel("livro"));
        if (emprestimo.status() != StatusEmprestimo.DEVOLVIDO) {
            model.add(linkTo(EmprestimoController.class).slash(emprestimo.id()).slash("devolucao").withRel("devolver"));
        }
        return model;
    }
}
