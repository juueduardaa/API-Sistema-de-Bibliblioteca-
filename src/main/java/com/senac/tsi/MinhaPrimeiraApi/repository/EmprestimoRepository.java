package com.senac.tsi.MinhaPrimeiraApi.repository;

import com.senac.tsi.MinhaPrimeiraApi.model.Emprestimo;
import com.senac.tsi.MinhaPrimeiraApi.model.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);

    boolean existsByLeitorId(Long leitorId);

    boolean existsByLivroId(Long livroId);
}
