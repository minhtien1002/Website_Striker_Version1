package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends ThuocTinhRepository<MauSac> {

    @Override
    @Query("SELECT x FROM MauSac x WHERE (:deleted IS NULL OR x.deleted = :deleted) AND (:keyword = '' " +
           "OR LOWER(x.tenMauSac) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.maMauSac) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.moTa) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MauSac> search(@Param("keyword") String keyword, @Param("deleted") Boolean deleted, Pageable pageable);

    @Override
    @Query("SELECT COUNT(x) FROM MauSac x WHERE LOWER(TRIM(x.tenMauSac)) = LOWER(:ten) AND (:excludeId IS NULL OR x.id <> :excludeId)")
    long countTrungTen(@Param("ten") String ten, @Param("excludeId") Integer excludeId);

    @Override
    @Query("SELECT COUNT(DISTINCT ct.idSanPham.id) FROM ChiTietSanPham ct WHERE ct.idMauSac.id = :id")
    long countSanPhamSuDung(@Param("id") Integer id);
}