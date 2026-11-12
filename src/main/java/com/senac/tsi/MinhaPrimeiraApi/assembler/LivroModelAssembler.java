package com.senac.tsi.MinhaPrimeiraApi.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.controller.AutorController;
import com.senac.tsi.MinhaPrimeiraApi.controller.CategoriaController;
import com.senac.tsi.MinhaPrimeiraApi.controller.LivroController;
import com.senac.tsi.MinhaPrimeiraApi.dto.LivroResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/** Monta o recurso HATEOAS (dados + links) de Livro. */
@Component
public class LivroModelAssembler implements RepresentationModelAssembler<LivroResponse, EntityModel<LivroResponse>> {

    @Override
    public EntityModel<LivroResponse> toModel(LivroResponse livro) {
        EntityModel<LivroResponse> model = EntityModel.of(livro,
                linkTo(LivroController.class).slash(livro.id()).withSelfRel(),
                linkTo(LivroController.class).withRel("livros"),
                linkTo(LivroController.class).slash(livro.id()).withRel("update"),
                linkTo(LivroController.class).slash(livro.id()).withRel("delete"));
        model.add(linkTo(CategoriaController.class).slash(livro.categoriaId()).withRel("categoria"));
        livro.autores().forEach(autor ->
                model.add(linkTo(AutorController.class).slash(autor.id()).withRel("autores")));
        return model;
    }
}
