package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.KichCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KichCoRepository extends ThuocTinhRepository<KichCo> {

    @Override
    @Query("SELECT x FROM KichCo x WHERE (:deleted IS NULL OR x.deleted = :deleted) AND (:keyword = '' " +
           "OR LOWER(x.tenKichCo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.maKichCo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.moTa) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KichCo> search(@Param("keyword") String keyword, @Param("deleted") Boolean deleted, Pageable pageable);

    @Override
    @Query("SELECT COUNT(x) FROM KichCo x WHERE LOWER(TRIM(x.tenKichCo)) = LOWER(:ten) AND (:excludeId IS NULL OR x.id <> :excludeId)")
    long countTrungTen(@Param("ten") String ten, @Param("excludeId") Integer excludeId);

    @Override
    @Query("SELECT COUNT(DISTINCT ct.idSanPham.id) FROM ChiTietSanPham ct WHERE ct.idKichCo.id = :id")
    long countSanPhamSuDung(@Param("id") Integer id);
}