package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.CongNgheVai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CongNgheVaiRepository extends JpaRepository<CongNgheVai, Integer> {

    @Query("SELECT cnv FROM CongNgheVai cnv WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(REPLACE(cnv.tenCongNghe, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :keyword, '%'), ' ', '')) OR " +
           "LOWER(REPLACE(cnv.maCongNghe, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :keyword, '%'), ' ', '')) OR " +
           "LOWER(cnv.tenCongNghe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cnv.maCongNghe) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CongNgheVai> findAllWithKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByMaCongNghe(String maCongNghe);

    boolean existsByMaCongNgheAndIdNot(String maCongNghe, Integer id);

    boolean existsByTenCongNghe(String tenCongNghe);

    boolean existsByTenCongNgheAndIdNot(String tenCongNghe, Integer id);
}
