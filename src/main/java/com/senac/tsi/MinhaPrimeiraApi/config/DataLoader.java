package com.senac.tsi.MinhaPrimeiraApi.config;

import com.senac.tsi.MinhaPrimeiraApi.model.Autor;
import com.senac.tsi.MinhaPrimeiraApi.model.Carteirinha;
import com.senac.tsi.MinhaPrimeiraApi.model.Categoria;
import com.senac.tsi.MinhaPrimeiraApi.model.Emprestimo;
import com.senac.tsi.MinhaPrimeiraApi.model.Leitor;
import com.senac.tsi.MinhaPrimeiraApi.model.Livro;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import com.senac.tsi.MinhaPrimeiraApi.repository.AutorRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.CarteirinhaRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.CategoriaRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.EmprestimoRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LeitorRepository;
import com.senac.tsi.MinhaPrimeiraApi.repository.LivroRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Carrega dados de exemplo para facilitar o teste no Swagger. Desative com app.seed.enabled=false. */
@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataLoader {

    @Bean
    CommandLineRunner carregarDadosExemplo(CategoriaRepository categorias, AutorRepository autores,
                                           LeitorRepository leitores, CarteirinhaRepository carteirinhas,
                                           LivroRepository livros, EmprestimoRepository emprestimos) {
        return args -> {
            Categoria fantasia = new Categoria();
            fantasia.setNome("Fantasia");
            fantasia.setDescricao("Livros de fantasia e aventura");
            fantasia = categorias.save(fantasia);

            Categoria romance = new Categoria();
            romance.setNome("Romance");
            romance.setDescricao("Romances clássicos e contemporâneos");
            romance = categorias.save(romance);

            Autor tolkien = new Autor();
            tolkien.setNome("J. R. R. Tolkien");
            tolkien.setNacionalidade("Britânica");
            tolkien = autores.save(tolkien);

            Autor machado = new Autor();
            machado.setNome("Machado de Assis");
            machado.setNacionalidade("Brasileira");
            machado = autores.save(machado);

            Livro hobbit = new Livro();
            hobbit.setTitulo("O Hobbit");
            hobbit.setIsbn("978-0-261-10221-7");
            hobbit.setAnoPublicacao(1937);
            hobbit.setDisponivel(false);
            hobbit.setCategoria(fantasia);
            hobbit.setAutores(new ArrayList<>(List.of(tolkien)));
            hobbit = livros.save(hobbit);

            Livro domCasmurro = new Livro();
            domCasmurro.setTitulo("Dom Casmurro");
            domCasmurro.setIsbn("978-85-359-0277-1");
            domCasmurro.setAnoPublicacao(1899);
            domCasmurro.setDisponivel(true);
            domCasmurro.setCategoria(romance);
            domCasmurro.setAutores(new ArrayList<>(List.of(machado)));
            livros.save(domCasmurro);

            Leitor ana = new Leitor();
            ana.setNome("Ana Souza");
            ana.setEmail("ana.souza@example.com");
            ana.setTelefone("11999999999");
            ana = leitores.save(ana);

            Leitor bruno = new Leitor();
            bruno.setNome("Bruno Lima");
            bruno.setEmail("bruno.lima@example.com");
            bruno.setTelefone("21988887777");
            leitores.save(bruno);

            Carteirinha carteirinhaAna = new Carteirinha();
            carteirinhaAna.setCodigo("CAR-0001");
            carteirinhaAna.setDataEmissao(LocalDate.now().minusMonths(2));
            carteirinhaAna.setAtiva(true);
            carteirinhaAna.setLeitor(ana);
            carteirinhas.save(carteirinhaAna);

            Emprestimo emprestimo = new Emprestimo();
            emprestimo.setDataEmprestimo(LocalDate.now().minusDays(3));
            emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(11));
            emprestimo.setStatus(StatusEmprestimo.ATIVO);
            emprestimo.setLeitor(ana);
            emprestimo.setLivro(hobbit);
            emprestimos.save(emprestimo);
        };
    }
}
