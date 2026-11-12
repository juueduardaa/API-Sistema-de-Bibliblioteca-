package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.LeitorRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.LeitorResponse;
import com.senac.tsi.MinhaPrimeiraApi.model.Leitor;
import com.senac.tsi.MinhaPrimeiraApi.repository.CarteirinhaRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.EmprestimoRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LeitorRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeitorService {

    private final LeitorRepository repository;
    private final CarteirinhaRepository carteirinhaRepository;
    private final EmprestimoRepository emprestimoRepository;

    public LeitorService(LeitorRepository repository, CarteirinhaRepository carteirinhaRepository,
                         EmprestimoRepository emprestimoRepository) {
        this.repository = repository;
        this.carteirinhaRepository = carteirinhaRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public Page<LeitorResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(LeitorResponse::from);
    }

    @Transactional(readOnly = true)
    public LeitorResponse buscarPorId(Long id) {
        return LeitorResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<LeitorResponse> buscar(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(LeitorResponse::from);
    }

    @Transactional
    public LeitorResponse criar(LeitorRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("Já existe um leitor com o e-mail '" + request.email() + "'.");
        }
        Leitor leitor = new Leitor();
        aplicar(leitor, request);
        return LeitorResponse.from(repository.saveAndFlush(leitor));
    }

    @Transactional
    public LeitorResponse atualizar(Long id, LeitorRequest request) {
        Leitor leitor = obter(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new BusinessRuleException("Já existe outro leitor com o e-mail '" + request.email() + "'.");
        }
        aplicar(leitor, request);
        return LeitorResponse.from(repository.saveAndFlush(leitor));
    }

    @Transactional
    public void excluir(Long id) {
        Leitor leitor = obter(id);
        if (emprestimoRepository.existsByLeitorId(id)) {
            throw new BusinessRuleException("Não é possível excluir: o leitor possui empréstimos registrados.");
        }
        if (leitor.getCarteirinha() != null) {
            carteirinhaRepository.delete(leitor.getCarteirinha());
            carteirinhaRepository.flush();
        }
        repository.delete(leitor);
        repository.flush();
    }

    private Leitor obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leitor não encontrado com ID " + id));
    }

    private void aplicar(Leitor leitor, LeitorRequest request) {
        leitor.setNome(request.nome());
        leitor.setEmail(request.email());
        leitor.setTelefone(request.telefone());
    }
}
