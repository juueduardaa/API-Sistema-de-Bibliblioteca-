package com.senac.tsi.MinhaPrimeiraApi.service;

import com.senac.tsi.MinhaPrimeiraApi.dto.LivroRequest;
import com.senac.tsi.MinhaPrimeiraApi.dto.LivroResponse;
import com.senac.tsi.MinhaPrimeiraApi.model.Autor;
import com.senac.tsi.MinhaPrimeiraApi.model.Categoria;
import com.senac.tsi.MinhaPrimeiraApi.model.Livro;
import com.senac.tsi.MinhaPrimeiraApi.repository.AutorRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.CategoriaRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.EmprestimoRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LivroRepository;
import com.senac.tsi.MinhaPrimeiraApi.exception.BusinessRuleException;
import com.senac.tsi.MinhaPrimeiraApi.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class LivroService {

    private final LivroRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final EmprestimoRepository emprestimoRepository;

    public LivroService(LivroRepository repository, CategoriaRepository categoriaRepository,
                        AutorRepository autorRepository, EmprestimoRepository emprestimoRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public Page<LivroResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(LivroResponse::from);
    }

    @Transactional(readOnly = true)
    public LivroResponse buscarPorId(Long id) {
        return LivroResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<LivroResponse> buscar(String titulo, Pageable pageable) {
        return repository.findByTituloContainingIgnoreCase(titulo, pageable).map(LivroResponse::from);
    }

    @Transactional
    public LivroResponse criar(LivroRequest request) {
        if (repository.existsByIsbn(request.isbn())) {
            throw new BusinessRuleException("Já existe um livro com o ISBN '" + request.isbn() + "'.");
        }
        Livro livro = new Livro();
        livro.setDisponivel(true);
        aplicar(livro, request);
        return LivroResponse.from(repository.saveAndFlush(livro));
    }

    @Transactional
    public LivroResponse atualizar(Long id, LivroRequest request) {
        Livro livro = obter(id);
        if (repository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new BusinessRuleException("Já existe outro livro com o ISBN '" + request.isbn() + "'.");
        }
        aplicar(livro, request);
        return LivroResponse.from(repository.saveAndFlush(livro));
    }

    @Transactional
    public void excluir(Long id) {
        Livro livro = obter(id);
        if (emprestimoRepository.existsByLivroId(id)) {
            throw new BusinessRuleException("Não é possível excluir: o livro possui empréstimos registrados.");
        }
        repository.delete(livro);
        repository.flush();
    }

    private Livro obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com ID " + id));
    }

    private void aplicar(Livro livro, LivroRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria não encontrada com ID " + request.categoriaId()));
        List<Long> ids = request.autoresIds().stream().distinct().toList();
        List<Autor> autores = autorRepository.findAllById(ids);
        if (autores.size() != ids.size()) {
            throw new ResourceNotFoundException("Um ou mais autores informados não existem: " + ids);
        }
        livro.setTitulo(request.titulo());
        livro.setIsbn(request.isbn());
        livro.setAnoPublicacao(request.anoPublicacao());
        livro.setCategoria(categoria);
        livro.setAutores(new ArrayList<>(autores));
    }
}
