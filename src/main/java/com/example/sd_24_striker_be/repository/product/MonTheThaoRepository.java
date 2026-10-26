package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.MonTheThao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonTheThaoRepository extends JpaRepository<MonTheThao, Integer> {

    @Query("SELECT mtt FROM MonTheThao mtt WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(REPLACE(mtt.tenMonTheThao, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :keyword, '%'), ' ', '')) OR " +
           "LOWER(REPLACE(mtt.maMonTheThao, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :keyword, '%'), ' ', '')) OR " +
           "LOWER(mtt.tenMonTheThao) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mtt.maMonTheThao) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MonTheThao> findAllWithKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByMaMonTheThao(String maMonTheThao);

    boolean existsByMaMonTheThaoAndIdNot(String maMonTheThao, Integer id);

    boolean existsByTenMonTheThao(String tenMonTheThao);

    boolean existsByTenMonTheThaoAndIdNot(String tenMonTheThao, Integer id);
}
