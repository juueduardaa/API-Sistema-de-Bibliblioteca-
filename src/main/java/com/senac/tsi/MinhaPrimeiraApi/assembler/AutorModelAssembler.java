package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.AutorController;
import com.senac.tsi.MinhaPrimeiraApi.dto.AutorResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Autor. */
@Component
public class AutorModelAssembler implements RepresentationModelAssembler<AutorResponse, EntityModel<AutorResponse>> {

    @Override
    public EntityModel<AutorResponse> toModel(AutorResponse autor) {
        EntityModel<AutorResponse> model = EntityModel.of(autor,
                linkTo(AutorController.class).slash(autor.id()).withSelfRel(),
                linkTo(AutorController.class).withRel("autores"),
                linkTo(AutorController.class).slash(autor.id()).withRel("update"),
                linkTo(AutorController.class).slash(autor.id()).withRel("delete"));
        return model;
    }
}
