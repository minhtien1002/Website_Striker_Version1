package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.ChatLieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends ThuocTinhRepository<ChatLieu> {

    @Override
    @Query("SELECT x FROM ChatLieu x WHERE (:deleted IS NULL OR x.deleted = :deleted) AND (:keyword = '' " +
           "OR LOWER(x.tenChatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.maChatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(x.moTa) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ChatLieu> search(@Param("keyword") String keyword, @Param("deleted") Boolean deleted, Pageable pageable);

    @Override
    @Query("SELECT COUNT(x) FROM ChatLieu x WHERE LOWER(TRIM(x.tenChatLieu)) = LOWER(:ten) AND (:excludeId IS NULL OR x.id <> :excludeId)")
    long countTrungTen(@Param("ten") String ten, @Param("excludeId") Integer excludeId);

    @Override
    @Query("SELECT COUNT(sp) FROM SanPham sp WHERE sp.idChatLieu.id = :id")
    long countSanPhamSuDung(@Param("id") Integer id);
}