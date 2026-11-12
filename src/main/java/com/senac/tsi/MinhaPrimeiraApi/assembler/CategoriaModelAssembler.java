package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.CategoriaController;
import com.senac.tsi.MinhaPrimeiraApi.dto.CategoriaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Categoria. */
@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<CategoriaResponse, EntityModel<CategoriaResponse>> {

    @Override
    public EntityModel<CategoriaResponse> toModel(CategoriaResponse categoria) {
        EntityModel<CategoriaResponse> model = EntityModel.of(categoria,
                linkTo(CategoriaController.class).slash(categoria.id()).withSelfRel(),
                linkTo(CategoriaController.class).withRel("categorias"),
                linkTo(CategoriaController.class).slash(categoria.id()).withRel("update"),
                linkTo(CategoriaController.class).slash(categoria.id()).withRel("delete"));
        return model;
    }
}
