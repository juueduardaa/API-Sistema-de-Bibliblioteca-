package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ponto de entrada HATEOAS: a partir dele é possível navegar até todos os recursos. */
@RestController
@RequestMapping("/api")
@Tag(name = "Raiz", description = "Ponto de entrada da API com links para todos os recursos")
public class ApiRootController {

    @GetMapping
    @Operation(summary = "Índice da API", description = "Devolve links HATEOAS para as coleções de cada recurso.")
    @ApiResponse(responseCode = "200", description = "Links disponíveis")
    public RepresentationModel<?> raiz() {
        RepresentationModel<?> root = new RepresentationModel<>();
        root.add(linkTo(ApiRootController.class).withSelfRel());
        root.add(linkTo(LeitorController.class).withRel("leitores"));
        root.add(linkTo(CarteirinhaController.class).withRel("carteirinhas"));
        root.add(linkTo(LivroController.class).withRel("livros"));
        root.add(linkTo(AutorController.class).withRel("autores"));
        root.add(linkTo(CategoriaController.class).withRel("categorias"));
        root.add(linkTo(EmprestimoController.class).withRel("emprestimos"));
        return root;
    }
}
