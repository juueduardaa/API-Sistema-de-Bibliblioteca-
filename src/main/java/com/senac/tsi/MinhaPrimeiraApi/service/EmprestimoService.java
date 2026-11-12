package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoResponse;
import com.senac.tsi.MinhaPrimeiraApi.dto.EmprestimoUpdateRequest;
import com.senac.tsi.MinhaPrimeiraApi.model.Carteirinha;
import com.senac.tsi.MinhaPrimeiraApi.model.Emprestimo;
import com.senac.tsi.MinhaPrimeiraApi.model.Leitor;
import com.senac.tsi.MinhaPrimeiraApi.model.Livro;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import com.senac.tsi.MinhaPrimeiraApi.repository.EmprestimoRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LeitorRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LivroRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;
    private final LeitorRepository leitorRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(EmprestimoRepository repository, LeitorRepository leitorRepository,
                             LivroRepository livroRepository) {
        this.repository = repository;
        this.leitorRepository = leitorRepository;
        this.livroRepository = livroRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmprestimoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(EmprestimoResponse::from);
    }

    @Transactional(readOnly = true)
    public EmprestimoResponse buscarPorId(Long id) {
        return EmprestimoResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<EmprestimoResponse> buscar(StatusEmprestimo status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(EmprestimoResponse::from);
    }

    /** Regras: leitor precisa de carteirinha ativa e o livro precisa estar disponível. */
    @Transactional
    public EmprestimoResponse criar(EmprestimoRequest request) {
        validarDatas(request.dataEmprestimo(), request.dataPrevistaDevolucao());
        Leitor leitor = leitorRepository.findById(request.leitorId())
                .orElseThrow(() -> new ResourceNotFoundException("Leitor não encontrado com ID " + request.leitorId()));
        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com ID " + request.livroId()));

        Carteirinha carteirinha = leitor.getCarteirinha();
        if (carteirinha == null || !Boolean.TRUE.equals(carteirinha.getAtiva())) {
            throw new BusinessRuleException("O leitor precisa ter uma carteirinha ativa para pegar livros emprestados.");
        }
        if (!Boolean.TRUE.equals(livro.getDisponivel())) {
            throw new BusinessRuleException("O livro '" + livro.getTitulo() + "' não está disponível no momento.");
        }

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataEmprestimo(request.dataEmprestimo());
        emprestimo.setDataPrevistaDevolucao(request.dataPrevistaDevolucao());
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        emprestimo.setLeitor(leitor);
        emprestimo.setLivro(livro);
        livro.setDisponivel(false);
        return EmprestimoResponse.from(repository.saveAndFlush(emprestimo));
    }

    @Transactional
    public EmprestimoResponse atualizar(Long id, EmprestimoUpdateRequest request) {
        Emprestimo emprestimo = obter(id);
        validarDatas(emprestimo.getDataEmprestimo(), request.dataPrevistaDevolucao());
        Livro livro = emprestimo.getLivro();
        boolean estavaDevolvido = emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO;

        emprestimo.setDataPrevistaDevolucao(request.dataPrevistaDevolucao());
        if (request.status() == StatusEmprestimo.DEVOLVIDO) {
            LocalDate devolucao = request.dataDevolucao() != null ? request.dataDevolucao() : LocalDate.now();
            if (devolucao.isBefore(emprestimo.getDataEmprestimo())) {
                throw new BusinessRuleException(HttpStatus.BAD_REQUEST,
                        "A data de devolução não pode ser anterior à data do empréstimo.");
            }
            emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
            emprestimo.setDataDevolucao(devolucao);
            livro.setDisponivel(true);
        } else {
            if (estavaDevolvido && !Boolean.TRUE.equals(livro.getDisponivel())) {
                throw new BusinessRuleException("Não é possível reabrir o empréstimo: o livro já foi emprestado a outro leitor.");
            }
            emprestimo.setStatus(request.status());
            emprestimo.setDataDevolucao(null);
            livro.setDisponivel(false);
        }
        return EmprestimoResponse.from(repository.saveAndFlush(emprestimo));
    }

    /** Atalho de negócio: registra a devolução com a data de hoje. */
    @Transactional
    public EmprestimoResponse devolver(Long id) {
        Emprestimo emprestimo = obter(id);
        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new BusinessRuleException("Este empréstimo já foi devolvido.");
        }
        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimo.setDataDevolucao(LocalDate.now());
        emprestimo.getLivro().setDisponivel(true);
        return EmprestimoResponse.from(repository.saveAndFlush(emprestimo));
    }

    @Transactional
    public void excluir(Long id) {
        Emprestimo emprestimo = obter(id);
        if (emprestimo.getStatus() != StatusEmprestimo.DEVOLVIDO) {
            emprestimo.getLivro().setDisponivel(true);
        }
        repository.delete(emprestimo);
        repository.flush();
    }

    private Emprestimo obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado com ID " + id));
    }

    private void validarDatas(LocalDate emprestimo, LocalDate prevista) {
        if (prevista.isBefore(emprestimo)) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST,
                    "A data prevista de devolução não pode ser anterior à data do empréstimo.");
        }
    }
}
