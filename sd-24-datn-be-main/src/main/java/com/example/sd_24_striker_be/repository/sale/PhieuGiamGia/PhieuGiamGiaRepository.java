package com.example.sd_24_striker_be.repository.sale.PhieuGiamGia;

import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    // Tìm phiếu giảm giá theo ID (đã có sẵn từ JpaRepository)
    Optional<PhieuGiamGia> findById(Long id);

    // Tìm phiếu giảm giá theo mã
    Optional<PhieuGiamGia> findByMa(String ma);

    // Tìm phiếu giảm giá chưa bị xóa
    List<PhieuGiamGia> findByDeletedFalse();

    // Tìm phiếu giảm giá theo trạng thái
    List<PhieuGiamGia> findByTrangThaiAndDeletedFalse(Boolean trangThai);

    // Tìm phiếu giảm giá đang hoạt động
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = true AND p.deleted = false AND p.ngayBatDau <= :now AND p.ngayKetThuc >= :now")
    List<PhieuGiamGia> findActiveVouchers(@Param("now") Instant now);

    // Tìm phiếu giảm giá đã hết hạn
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.ngayKetThuc < :now AND p.deleted = false")
    List<PhieuGiamGia> findExpiredVouchers(@Param("now") Instant now);

    // Tìm phiếu giảm giá theo tên (tìm kiếm gần đúng)
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.deleted = false AND (LOWER(p.tenPhieuGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.ma) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<PhieuGiamGia> findByKeyword(@Param("keyword") String keyword);

    // Kiểm tra mã phiếu giảm giá đã tồn tại chưa (trừ ID hiện tại khi update)
    @Query("SELECT COUNT(p) > 0 FROM PhieuGiamGia p WHERE p.ma = :ma AND p.id != :id AND p.deleted = false")
    boolean existsByMaAndIdNot(@Param("ma") String ma, @Param("id") Long id);

    // Kiểm tra mã phiếu giảm giá đã tồn tại chưa (khi tạo mới)
    boolean existsByMaAndDeletedFalse(String ma);

    // Tìm phiếu giảm giá công khai đang hoạt động (cho client API)
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = true AND p.deleted = false " +
           "AND p.ngayBatDau <= :now AND p.ngayKetThuc >= :now " +
           "AND (p.riengTu = false OR p.riengTu IS NULL)")
    List<PhieuGiamGia> findActivePublicVouchers(@Param("now") Instant now);

    /**
     * Trừ 1 lượt dùng NGUYÊN TỬ (chỉ trừ khi còn lượt) - trả về số dòng bị cập nhật (0 = hết
     * lượt). Một câu UPDATE duy nhất nên 2 đơn đặt cùng lúc không thể cùng dùng lượt cuối.
     * soLuongDung = NULL nghĩa là không giới hạn lượt -> không trừ.
     */
    // KHÔNG clearAutomatically: xóa persistence context giữa transaction xác nhận đơn làm
    // các quan hệ lazy của hóa đơn (NhanVien...) lỗi "no session".
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @Query("UPDATE PhieuGiamGia p SET p.soLuongDung = p.soLuongDung - 1, " +
           "p.trangThai = CASE WHEN p.soLuongDung - 1 <= 0 THEN false ELSE p.trangThai END " +
           "WHERE p.id = :id AND p.soLuongDung > 0")
    int truMotLuotSuDung(@Param("id") Integer id);
}
