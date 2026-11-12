package com.senac.tsi.MinhaPrimeiraApi.repository;

import com.senac.tsi.MinhaPrimeiraApi.model.Carteirinha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarteirinhaRepository extends JpaRepository<Carteirinha, Long> {

    Page<Carteirinha> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByLeitorId(Long leitorId);

    boolean existsByLeitorIdAndIdNot(Long leitorId, Long id);
}
