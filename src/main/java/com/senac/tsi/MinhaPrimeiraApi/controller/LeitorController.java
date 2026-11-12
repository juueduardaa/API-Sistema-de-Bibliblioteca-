package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.LeitorModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.LeitorRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.LeitorResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.service.LeitorService;
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
@RequestMapping("/api/leitores")
@Tag(name = "Leitores", description = "Leitores cadastrados na biblioteca")
public class LeitorController {

    private final LeitorService service;
    private final LeitorModelAssembler assembler;
    private final PagedResourcesAssembler<LeitorResponse> pagedAssembler;

    public LeitorController(LeitorService service, LeitorModelAssembler assembler,
            PagedResourcesAssembler<LeitorResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar leitores",
            description = "Retorna uma página de leitores com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=nome,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de leitores retornada com sucesso")
    })
    public PagedModel<EntityModel<LeitorResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<LeitorResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar leitor", description = "Cadastra um leitor. O e-mail deve ser único. A carteirinha é criada depois, em /api/carteirinhas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Leitor criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um leitor com este e-mail",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<LeitorResponse>> criar(@Valid @RequestBody LeitorRequest request) {
        EntityModel<LeitorResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar leitor por ID", description = "Retorna um único leitor com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitor encontrado"),
            @ApiResponse(responseCode = "404", description = "Leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<LeitorResponse> buscarPorId(
            @Parameter(description = "ID do leitor", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar leitor", description = "Substitui os dados do leitor informado. O e-mail deve continuar único.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outro leitor com este e-mail",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<LeitorResponse> atualizar(
            @Parameter(description = "ID do leitor", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody LeitorRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir leitor", description = "Remove o leitor e sua carteirinha (se houver). Só é permitido se ele não tiver empréstimos registrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Leitor excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "O leitor possui empréstimos registrados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do leitor", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar leitores por nome", description = "Busca paginada de leitores cujo nome contém o texto informado (sem diferenciar maiúsculas de minúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<LeitorResponse>> buscar(
            @Parameter(description = "Parte do nome do leitor", example = "ana") @RequestParam("nome") String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(nome, pageable), assembler);
    }

    private Link linkBusca() {
        String base = linkTo(LeitorController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?nome,page,size,sort}", "buscar");
    }
}
