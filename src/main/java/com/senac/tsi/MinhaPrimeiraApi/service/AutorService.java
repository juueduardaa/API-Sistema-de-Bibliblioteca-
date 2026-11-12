package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.AutorRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.AutorResponse;
import com.senac.tsi.MinhaPrimeiraApi.model.Autor;
import com.senac.tsi.MinhaPrimeiraApi.repository.AutorRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutorService {

    private final AutorRepository repository;

    public AutorService(AutorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<AutorResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(AutorResponse::from);
    }

    @Transactional(readOnly = true)
    public AutorResponse buscarPorId(Long id) {
        return AutorResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<AutorResponse> buscar(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(AutorResponse::from);
    }

    @Transactional
    public AutorResponse criar(AutorRequest request) {
        Autor autor = new Autor();
        aplicar(autor, request);
        return AutorResponse.from(repository.saveAndFlush(autor));
    }

    @Transactional
    public AutorResponse atualizar(Long id, AutorRequest request) {
        Autor autor = obter(id);
        aplicar(autor, request);
        return AutorResponse.from(repository.saveAndFlush(autor));
    }

    @Transactional
    public void excluir(Long id) {
        Autor autor = obter(id);
        if (!autor.getLivros().isEmpty()) {
            throw new BusinessRuleException("Não é possível excluir: o autor está associado a livros.");
        }
        repository.delete(autor);
        repository.flush();
    }

    private Autor obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado com ID " + id));
    }

    private void aplicar(Autor autor, AutorRequest request) {
        autor.setNome(request.nome());
        autor.setNacionalidade(request.nacionalidade());
    }
}
