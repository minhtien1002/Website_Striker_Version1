package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.DanhMuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository extends ThuocTinhRepository<DanhMuc> {

    @Override
    @Query("SELECT x FROM DanhMuc x WHERE (:deleted IS NULL OR x.deleted = :deleted) AND (:keyword = '' " +
           "OR LOWER(x.tenDanhMuc) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.maDanhMuc) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.moTa) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DanhMuc> search(@Param("keyword") String keyword, @Param("deleted") Boolean deleted, Pageable pageable);

    @Override
    @Query("SELECT COUNT(x) FROM DanhMuc x WHERE LOWER(TRIM(x.tenDanhMuc)) = LOWER(:ten) AND (:excludeId IS NULL OR x.id <> :excludeId)")
    long countTrungTen(@Param("ten") String ten, @Param("excludeId") Integer excludeId);

    @Override
    @Query("SELECT COUNT(sp) FROM SanPham sp WHERE sp.idDanhMuc.id = :id")
    long countSanPhamSuDung(@Param("id") Integer id);
}