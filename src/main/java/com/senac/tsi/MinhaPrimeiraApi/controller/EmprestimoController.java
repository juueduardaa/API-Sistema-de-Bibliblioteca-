package com.senac.tsi.MinhaPrimeiraApi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.senac.tsi.MinhaPrimeiraApi.assembler.EmprestimoModelAssembler;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoUpdateRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoResponse;
import com.senac.tsi.MinhaPrimeiraApi.exception.ApiError;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import com.senac.tsi.MinhaPrimeiraApi.service.EmprestimoService;
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
@RequestMapping("/api/emprestimos")
@Tag(name = "Empréstimos", description = "Empréstimos de livros a leitores (usa o enum StatusEmprestimo)")
public class EmprestimoController {

    private final EmprestimoService service;
    private final EmprestimoModelAssembler assembler;
    private final PagedResourcesAssembler<EmprestimoResponse> pagedAssembler;

    public EmprestimoController(EmprestimoService service, EmprestimoModelAssembler assembler,
            PagedResourcesAssembler<EmprestimoResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar empréstimos",
            description = "Retorna uma página de empréstimos com links HATEOAS. "
                    + "Use os parâmetros page (começa em 0), size e sort (ex.: sort=dataEmprestimo,asc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de empréstimos retornada com sucesso")
    })
    public PagedModel<EntityModel<EmprestimoResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PagedModel<EntityModel<EmprestimoResponse>> model = pagedAssembler.toModel(service.listar(pageable), assembler);
        model.add(linkBusca());
        return model;
    }

    @PostMapping
    @Operation(summary = "Cadastrar empréstimo", description = "Registra um empréstimo (status inicial ATIVO). O leitor precisa ter carteirinha ativa e o livro precisa estar disponível; o livro passa a ficar indisponível.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Empréstimo criado com sucesso. O cabeçalho Location aponta para o novo recurso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leitor ou livro informado não existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Leitor sem carteirinha ativa ou livro indisponível",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<EmprestimoResponse>> criar(@Valid @RequestBody EmprestimoRequest request) {
        EntityModel<EmprestimoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empréstimo por ID", description = "Retorna um único empréstimo com links para os recursos relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo encontrado"),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<EmprestimoResponse> buscarPorId(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar empréstimo", description = "Altera a data prevista, o status e a data de devolução. Ao marcar como DEVOLVIDO o livro volta a ficar disponível.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (falha na validação)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Não é possível reabrir: o livro já foi emprestado a outro leitor",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<EmprestimoResponse> atualizar(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody EmprestimoUpdateRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir empréstimo", description = "Remove o empréstimo. Se ainda estiver em aberto, o livro volta a ficar disponível.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empréstimo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar empréstimos por status", description = "Busca paginada de empréstimos pelo status (ATIVO, DEVOLVIDO ou ATRASADO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<EmprestimoResponse>> buscar(
            @Parameter(description = "Status do empréstimo", example = "ATIVO") @RequestParam("status") StatusEmprestimo status,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return pagedAssembler.toModel(service.buscar(status, pageable), assembler);
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registrar devolução",
            description = "Marca o empréstimo como DEVOLVIDO com a data de hoje e libera o livro para novos empréstimos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução registrada"),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "O empréstimo já foi devolvido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<EmprestimoResponse> devolver(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable("id") Long id) {
        return assembler.toModel(service.devolver(id));
    }

    private Link linkBusca() {
        String base = linkTo(EmprestimoController.class).slash("buscar").toUri().toString();
        return Link.of(base + "{?status,page,size,sort}", "buscar");
    }
}
