package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.CarteirinhaRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.CarteirinhaResponse;
import com.senac.tsi.MinhaPrimeiraApi.model.Carteirinha;
import com.senac.tsi.MinhaPrimeiraApi.model.Leitor;
import com.senac.tsi.MinhaPrimeiraApi.repository.CarteirinhaRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LeitorRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarteirinhaService {

    private final CarteirinhaRepository repository;
    private final LeitorRepository leitorRepository;

    public CarteirinhaService(CarteirinhaRepository repository, LeitorRepository leitorRepository) {
        this.repository = repository;
        this.leitorRepository = leitorRepository;
    }

    @Transactional(readOnly = true)
    public Page<CarteirinhaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(CarteirinhaResponse::from);
    }

    @Transactional(readOnly = true)
    public CarteirinhaResponse buscarPorId(Long id) {
        return CarteirinhaResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<CarteirinhaResponse> buscar(String codigo, Pageable pageable) {
        return repository.findByCodigoContainingIgnoreCase(codigo, pageable).map(CarteirinhaResponse::from);
    }

    @Transactional
    public CarteirinhaResponse criar(CarteirinhaRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new BusinessRuleException("Já existe uma carteirinha com o código '" + request.codigo() + "'.");
        }
        Leitor leitor = obterLeitor(request.leitorId());
        if (repository.existsByLeitorId(leitor.getId())) {
            throw new BusinessRuleException("O leitor " + leitor.getId() + " já possui uma carteirinha (relação 1 para 1).");
        }
        Carteirinha carteirinha = new Carteirinha();
        aplicar(carteirinha, request, leitor);
        return CarteirinhaResponse.from(repository.saveAndFlush(carteirinha));
    }

    @Transactional
    public CarteirinhaResponse atualizar(Long id, CarteirinhaRequest request) {
        Carteirinha carteirinha = obter(id);
        if (repository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new BusinessRuleException("Já existe outra carteirinha com o código '" + request.codigo() + "'.");
        }
        Leitor leitor = obterLeitor(request.leitorId());
        if (repository.existsByLeitorIdAndIdNot(leitor.getId(), id)) {
            throw new BusinessRuleException("O leitor " + leitor.getId() + " já possui outra carteirinha (relação 1 para 1).");
        }
        aplicar(carteirinha, request, leitor);
        return CarteirinhaResponse.from(repository.saveAndFlush(carteirinha));
    }

    @Transactional
    public void excluir(Long id) {
        Carteirinha carteirinha = obter(id);
        repository.delete(carteirinha);
        repository.flush();
    }

    private Carteirinha obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteirinha não encontrada com ID " + id));
    }

    private Leitor obterLeitor(Long leitorId) {
        return leitorRepository.findById(leitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Leitor não encontrado com ID " + leitorId));
    }

    private void aplicar(Carteirinha carteirinha, CarteirinhaRequest request, Leitor leitor) {
        carteirinha.setCodigo(request.codigo());
        carteirinha.setDataEmissao(request.dataEmissao());
        carteirinha.setAtiva(request.ativa());
        carteirinha.setLeitor(leitor);
    }
}
