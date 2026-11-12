package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.LivroModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.LivroRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.LivroResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.service.LivroService;
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
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "Acervo de livros da biblioteca")
public class LivroController {

    private final LivroService service;
    private final LivroModelAssembler assembler;
    private final PagedResourcesAssembler<LivroResponse> pagedAssembler;

    public LivroController(LivroService service, LivroModelAssembler assembler,
            PagedResourcesAssembler<LivroResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar livros",
            description = "Retorna uma página de livros com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=titulo,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de livros retornada com sucesso")
    })
    public PagedModel<EntityModel<LivroResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<LivroResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar livro", description = "Cadastra um livro. Informe o ID de uma categoria existente e os IDs de um ou mais autores existentes. O ISBN deve ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Livro criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ou autor informado não existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um livro com este ISBN",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<LivroResponse>> criar(@Valid @RequestBody LivroRequest request) {
        EntityModel<LivroResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID", description = "Retorna um único livro com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<LivroResponse> buscarPorId(
            @Parameter(description = "ID do livro", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar livro", description = "Substitui os dados do livro informado (inclusive categoria e autores).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Livro, categoria ou autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outro livro com este ISBN",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<LivroResponse> atualizar(
            @Parameter(description = "ID do livro", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody LivroRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir livro", description = "Remove o livro. Só é permitido se ele nunca tiver sido emprestado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "O livro possui empréstimos registrados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do livro", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar livros por título", description = "Busca paginada de livros cujo título contém o texto informado (sem diferenciar maiúsculas de minúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<LivroResponse>> buscar(
            @Parameter(description = "Parte do título do livro", example = "hobbit") @RequestParam("titulo") String titulo,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(titulo, pageable), assembler);
    }

    private Link linkBusca() {
        String base = linkTo(LivroController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?titulo,page,size,sort}", "buscar");
    }
}
