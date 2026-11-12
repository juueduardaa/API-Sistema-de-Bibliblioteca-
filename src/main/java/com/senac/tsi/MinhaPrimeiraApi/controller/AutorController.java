package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.AutorModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.AutorRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.AutorResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.service.AutorService;
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
@RequestMapping("/api/autores")
@Tag(name = "Autores", description = "Autores dos livros (relação muitos-para-muitos com Livro)")
public class AutorController {

    private final AutorService service;
    private final AutorModelAssembler assembler;
    private final PagedResourcesAssembler<AutorResponse> pagedAssembler;

    public AutorController(AutorService service, AutorModelAssembler assembler,
            PagedResourcesAssembler<AutorResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar autores",
            description = "Retorna uma página de autores com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=nome,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de autores retornada com sucesso")
    })
    public PagedModel<EntityModel<AutorResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<AutorResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar autor", description = "Cadastra um novo autor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Autor criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<AutorResponse>> criar(@Valid @RequestBody AutorRequest request) {
        EntityModel<AutorResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar autor por ID", description = "Retorna um único autor com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor encontrado"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<AutorResponse> buscarPorId(
            @Parameter(description = "ID do autor", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor", description = "Substitui os dados do autor informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<AutorResponse> atualizar(
            @Parameter(description = "ID do autor", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody AutorRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir autor", description = "Remove o autor. Só é permitido se ele não estiver associado a livros.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Autor excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "O autor está associado a livros",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do autor", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar autores por nome", description = "Busca paginada de autores cujo nome contém o texto informado (sem diferenciar maiúsculas de minúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<AutorResponse>> buscar(
            @Parameter(description = "Parte do nome do autor", example = "tolkien") @RequestParam("nome") String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(nome, pageable), assembler);
    }

    private Link linkBusca() {
        String base = linkTo(AutorController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?nome,page,size,sort}", "buscar");
    }
}
