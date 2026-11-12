package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.CarteirinhaController;
import com.senac.tsi.MinhaPrimeiraApi.controller.LeitorController;
import com.senac.tsi.MinhaPrimeiraApi.dto.LeitorResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Leitor. */
@Component
public class LeitorModelAssembler implements RepresentationModelAssembler<LeitorResponse, EntityModel<LeitorResponse>> {

    @Override
    public EntityModel<LeitorResponse> toModel(LeitorResponse leitor) {
        EntityModel<LeitorResponse> model = EntityModel.of(leitor,
                linkTo(LeitorController.class).slash(leitor.id()).withSelfRel(),
                linkTo(LeitorController.class).withRel("leitores"),
                linkTo(LeitorController.class).slash(leitor.id()).withRel("update"),
                linkTo(LeitorController.class).slash(leitor.id()).withRel("delete"));
        if (leitor.carteirinhaId() != null) {
            model.add(linkTo(CarteirinhaController.class).slash(leitor.carteirinhaId()).withRel("carteirinha"));
        }
        return model;
    }
}
