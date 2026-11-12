package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.CategoriaRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.CategoriaResponse;
import com.senac.tsi.MinhaPrimeiraApi.model.Categoria;
import com.senac.tsi.MinhaPrimeiraApi.repository.CategoriaRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(CategoriaResponse::from);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> buscar(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(CategoriaResponse::from);
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessRuleException("Já existe uma categoria com o nome '" + request.nome() + "'.");
        }
        Categoria categoria = new Categoria();
        aplicar(categoria, request);
        return CategoriaResponse.from(repository.saveAndFlush(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obter(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new BusinessRuleException("Já existe outra categoria com o nome '" + request.nome() + "'.");
        }
        aplicar(categoria, request);
        return CategoriaResponse.from(repository.saveAndFlush(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obter(id);
        if (!categoria.getLivros().isEmpty()) {
            throw new BusinessRuleException("Não é possível excluir: a categoria possui livros cadastrados.");
        }
        repository.delete(categoria);
        repository.flush();
    }

    private Categoria obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID " + id));
    }

    private void aplicar(Categoria categoria, CategoriaRequest request) {
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
    }
}
