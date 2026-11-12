package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.CarteirinhaModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.CarteirinhaRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.CarteirinhaResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.service.CarteirinhaService;
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
@RequestMapping("/api/carteirinhas")
@Tag(name = "Carteirinhas", description = "Carteirinhas de leitores (relação um-para-um com Leitor)")
public class CarteirinhaController {

    private final CarteirinhaService service;
    private final CarteirinhaModelAssembler assembler;
    private final PagedResourcesAssembler<CarteirinhaResponse> pagedAssembler;

    public CarteirinhaController(CarteirinhaService service, CarteirinhaModelAssembler assembler,
            PagedResourcesAssembler<CarteirinhaResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar carteirinhas",
            description = "Retorna uma página de carteirinhas com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=codigo,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de carteirinhas retornada com sucesso")
    })
    public PagedModel<EntityModel<CarteirinhaResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<CarteirinhaResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar carteirinha", description = "Emite a carteirinha de um leitor existente. Cada leitor pode ter apenas uma e o código deve ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Carteirinha criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leitor informado não existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código já utilizado ou leitor já possui carteirinha",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<CarteirinhaResponse>> criar(@Valid @RequestBody CarteirinhaRequest request) {
        EntityModel<CarteirinhaResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar carteirinha por ID", description = "Retorna um único carteirinha com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carteirinha encontrado"),
            @ApiResponse(responseCode = "404", description = "Carteirinha não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<CarteirinhaResponse> buscarPorId(
            @Parameter(description = "ID do carteirinha", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar carteirinha", description = "Substitui os dados da carteirinha informada (código, emissão, situação e leitor).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carteirinha atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Carteirinha ou leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código já utilizado ou leitor já possui outra carteirinha",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<CarteirinhaResponse> atualizar(
            @Parameter(description = "ID do carteirinha", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody CarteirinhaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir carteirinha", description = "Remove a carteirinha. O leitor continua cadastrado, mas não poderá fazer novos empréstimos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carteirinha excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteirinha não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do carteirinha", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar carteirinhas por código", description = "Busca paginada de carteirinhas cujo código contém o texto informado (sem diferenciar maiúsculas de minúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<CarteirinhaResponse>> buscar(
            @Parameter(description = "Parte do código da carteirinha", example = "CAR") @RequestParam("codigo") String codigo,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(codigo, pageable), assembler);
    }

    private Link linkBusca() {
        String base = linkTo(CarteirinhaController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?codigo,page,size,sort}", "buscar");
    }
}
