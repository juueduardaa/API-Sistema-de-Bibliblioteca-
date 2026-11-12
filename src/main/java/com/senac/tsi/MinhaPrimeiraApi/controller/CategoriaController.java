package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.CategoriaModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.CategoriaRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.CategoriaResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Categorias (gêneros) dos livros do acervo")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaModelAssembler assembler;
    private final PagedResourcesAssembler<CategoriaResponse> pagedAssembler;

    public CategoriaController(CategoriaService service, CategoriaModelAssembler assembler,
            PagedResourcesAssembler<CategoriaResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar categorias",
            description = "Retorna uma página de categorias com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=nome,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de categorias retornada com sucesso")
    })
    public PagedModel<EntityModel<CategoriaResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<CategoriaResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria", description = "Cria uma categoria. O nome deve ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Categoria criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com este nome",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<CategoriaResponse>> criar(@Valid @RequestBody CategoriaRequest request) {
        EntityModel<CategoriaResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID", description = "Retorna um único categoria com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<CategoriaResponse> buscarPorId(
            @Parameter(description = "ID do categoria", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Substitui os dados da categoria informada. O nome deve continuar único.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outra categoria com este nome",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<CategoriaResponse> atualizar(
            @Parameter(description = "ID do categoria", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria", description = "Remove a categoria. Só é permitido se ela não possuir livros.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "A categoria possui livros cadastrados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do categoria", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar categorias por nome", description = "Busca paginada de categorias cujo nome contém o texto informado (sem diferenciar maiúsculas de minúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<CategoriaResponse>> buscar(
            @Parameter(description = "Parte do nome da categoria", example = "fantasia") @RequestParam("nome") String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(nome, pageable), assembler);
    }

    private Link linkBusca() {
        String base = linkTo(CategoriaController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?nome,page,size,sort}", "buscar");
    }
}
