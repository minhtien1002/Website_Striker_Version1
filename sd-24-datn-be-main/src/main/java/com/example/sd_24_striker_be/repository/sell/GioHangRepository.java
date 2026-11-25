package com.example.sd_24_striker_be.repository.sell;

import com.example.sd_24_striker_be.modal.sell.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    /**
     * Find cart by invoice ID
     */
    @Query("SELECT gh FROM GioHang gh WHERE gh.idHoaDon.id = :hoaDonId")
    Optional<GioHang> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Find cart by customer ID and invoice ID
     */
    @Query("SELECT gh FROM GioHang gh WHERE gh.idKhachHang.id = :khachHangId AND gh.idHoaDon.id = :hoaDonId")
    Optional<GioHang> findByKhachHangIdAndHoaDonId(@Param("khachHangId") Integer khachHangId, @Param("hoaDonId") Integer hoaDonId);

    /**
     * Check if cart exists for invoice
     */
    @Query("SELECT COUNT(gh) > 0 FROM GioHang gh WHERE gh.idHoaDon.id = :hoaDonId")
    boolean existsByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Find active cart by customer ID (BỔ SUNG ĐỂ SỬA LỖI)
     * Tìm giỏ hàng hiện tại của khách hàng (giỏ hàng chưa gắn với hóa đơn nào)
     */
    @Query("SELECT gh FROM GioHang gh WHERE gh.idKhachHang.id = :idKhachHang AND gh.idHoaDon IS NULL")
    Optional<GioHang> findByIdKhachHang(@Param("idKhachHang") Integer idKhachHang);
}