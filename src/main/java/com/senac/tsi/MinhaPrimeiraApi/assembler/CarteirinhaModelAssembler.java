package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.LeitorController;
import com.senac.tsi.MinhaPrimeiraApi.controller.CarteirinhaController;
import com.senac.tsi.MinhaPrimeiraApi.dto.CarteirinhaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Carteirinha. */
@Component
public class CarteirinhaModelAssembler implements RepresentationModelAssembler<CarteirinhaResponse, EntityModel<CarteirinhaResponse>> {

    @Override
    public EntityModel<CarteirinhaResponse> toModel(CarteirinhaResponse carteirinha) {
        EntityModel<CarteirinhaResponse> model = EntityModel.of(carteirinha,
                linkTo(CarteirinhaController.class).slash(carteirinha.id()).withSelfRel(),
                linkTo(CarteirinhaController.class).withRel("carteirinhas"),
                linkTo(CarteirinhaController.class).slash(carteirinha.id()).withRel("update"),
                linkTo(CarteirinhaController.class).slash(carteirinha.id()).withRel("delete"));
        model.add(linkTo(LeitorController.class).slash(carteirinha.leitorId()).withRel("leitor"));
        return model;
    }
}
