package com.example.sd_24_striker_be.repository.bill;

import com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query("""
            SELECT new com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse(
            h.id,
            h.ma,
            h.idNhanVien.id,
            h.idNhanVien.ma,
            h.tenKhachHang,
            h.soDienThoaiKhachHang,
            h.loaiDon,
            h.phiVanChuyen,
            h.ngayTao,
            h.tongTienSauGiam,
            h.trangThai,
            h.deleted
            ) 
            FROM HoaDon h
            WHERE (
                   :keyword IS NULL 
                   OR h.ma LIKE %:keyword%
                   OR h.tenKhachHang LIKE %:keyword%
                   OR h.soDienThoaiKhachHang LIKE %:keyword%
                   OR h.idNhanVien.ma LIKE %:keyword%
                    )
                   AND (:minAmount IS NULL OR h.tongTienSauGiam >= :minAmount)
                   AND (:maxAmount IS NULL OR h.tongTienSauGiam <= :maxAmount)
                   AND (:startDate IS NULL OR h.ngayTao >= :startDate)
                   AND (:endDate IS NULL OR h.ngayTao <= :endDate)
                   AND (:trangThai IS NULL OR h.trangThai = :trangThai)
                   AND (:deleted IS NULL OR h.deleted = :deleted)
                   AND (:loaiDon IS NULL OR h.loaiDon = :loaiDon OR (:loaiDon = 'Tại quầy' AND h.loaiDon = 'TAI_QUAY') OR (:loaiDon = 'Online' AND h.loaiDon = 'ONLINE'))
            """)
    Page<HoaDonDTOResponse> getAllHoaDon(@Param("keyword") String keyword,
                                         @Param("minAmount") Long minAmount,
                                         @Param("maxAmount") Long maxAmount,
                                         @Param("startDate") Timestamp startDate,
                                         @Param("endDate") Timestamp endDate,
                                         @Param("trangThai") Short trangThai,
                                         @Param("deleted") Boolean deleted,
                                         @Param("loaiDon") String loaiDon,
                                         Pageable pageable);


    //Detail HDCT...
    // Only one collection (chiTietHoaDon) is eagerly joined here. Fetching more than one
    // @OneToMany collection in a single JOIN FETCH query produces a SQL cartesian product,
    // which Hibernate then surfaces as duplicated elements in the *other* collections
    // (e.g. lichSuHoaDon repeated once per chiTietHoaDon row). lichSuHoaDon and
    // hinhThucThanhToan are left lazy and are loaded on demand by the service layer
    // (safe because spring.jpa.open-in-view keeps the persistence context open for the request).
    @Query("""
            SELECT hd FROM HoaDon hd
            LEFT JOIN FETCH hd.idKhachHang
            LEFT JOIN FETCH hd.idNhanVien
            LEFT JOIN FETCH hd.idPhieuGiamGia
            LEFT JOIN FETCH hd.chiTietHoaDon cthd
            LEFT JOIN FETCH cthd.idChiTietSp ctsp
            LEFT JOIN FETCH ctsp.idSanPham
            LEFT JOIN FETCH ctsp.idMauSac
            LEFT JOIN FETCH ctsp.idKichCo
            WHERE hd.id = :id
            """)
    Optional<HoaDon> findHoaDonDetailById(@Param("id") Integer id);

    @Query("SELECT MIN(h.tongTienSauGiam) FROM HoaDon h WHERE h.deleted = false")
    Long findMinPrice();

    @Query("SELECT MAX(h.tongTienSauGiam) FROM HoaDon h WHERE h.deleted = false")
    Long findMaxPrice();

    // Doanh thu = tổng tiền các hóa đơn đã hoàn thành (trạng thái 5)
    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0) FROM HoaDon h WHERE h.trangThai = 5 AND h.deleted = false")
    BigDecimal sumCompletedRevenue();

    // Tìm hóa đơn theo mã
    @Query("SELECT hd FROM HoaDon hd WHERE hd.ma = :ma")
    Optional<HoaDon> findByMa(@Param("ma") String ma);
    
    // Tìm hóa đơn theo mã và khách hàng
    @Query("SELECT hd FROM HoaDon hd WHERE hd.ma = :ma AND hd.idKhachHang = :khachHang")
    Optional<HoaDon> findByMaAndIdKhachHang(@Param("ma") String ma, @Param("khachHang") com.example.sd_24_striker_be.modal.account.KhachHang khachHang);
    
    // Get pending invoices for sales counter (status = 0, deleted = true)
    @Query("SELECT hd FROM HoaDon hd LEFT JOIN FETCH hd.idKhachHang WHERE hd.trangThai = 0 AND hd.deleted = true")
    List<HoaDon> findPendingInvoicesForSales();

    // Statistics queries - Include all orders that still count as real revenue:
    // exclude 0 (Hóa đơn chờ - giỏ hàng nháp, chưa phải đơn thật), 6 (Đã hủy) và 8 (Đã hoàn hàng).
    // Trước đây điều kiện là "trangThai != 3" (Chờ vận chuyển) - sai, khiến các đơn đang
    // chờ vận chuyển bị loại khỏi doanh thu một cách vô lý.
    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0) FROM HoaDon h WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate")
    Long getTotalOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0) FROM HoaDon h WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false AND CAST(h.ngayTao AS DATE) = :date")
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false AND CAST(h.ngayTao AS DATE) = :date")
    Long getTotalOrdersByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT CAST(h.ngayTao AS DATE), COALESCE(SUM(h.tongTienSauGiam), 0), COUNT(h)
        FROM HoaDon h 
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false 
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY CAST(h.ngayTao AS DATE)
        ORDER BY CAST(h.ngayTao AS DATE)
        """)
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT DATEPART(YEAR, h.ngayTao), DATEPART(WEEK, h.ngayTao), COALESCE(SUM(h.tongTienSauGiam), 0), COUNT(h)
        FROM HoaDon h 
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false 
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY DATEPART(YEAR, h.ngayTao), DATEPART(WEEK, h.ngayTao)
        ORDER BY DATEPART(YEAR, h.ngayTao), DATEPART(WEEK, h.ngayTao)
        """)
    List<Object[]> getWeeklyRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT DATEPART(YEAR, h.ngayTao), DATEPART(MONTH, h.ngayTao), COALESCE(SUM(h.tongTienSauGiam), 0), COUNT(h)
        FROM HoaDon h 
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false 
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY DATEPART(YEAR, h.ngayTao), DATEPART(MONTH, h.ngayTao)
        ORDER BY DATEPART(YEAR, h.ngayTao), DATEPART(MONTH, h.ngayTao)
        """)
    List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT DATEPART(YEAR, h.ngayTao), COALESCE(SUM(h.tongTienSauGiam), 0), COUNT(h)
        FROM HoaDon h 
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false 
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY DATEPART(YEAR, h.ngayTao)
        ORDER BY DATEPART(YEAR, h.ngayTao)
        """)
    List<Object[]> getYearlyRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT dm.tenDanhMuc, COALESCE(SUM(hct.gia * hct.soLuong), 0), COUNT(DISTINCT h.id), COUNT(DISTINCT sp.id)
        FROM HoaDon h
        JOIN HoaDonChiTiet hct ON h.id = hct.idHoaDon.id
        JOIN ChiTietSanPham ctsp ON hct.idChiTietSp.id = ctsp.id
        JOIN SanPham sp ON ctsp.idSanPham.id = sp.id
        JOIN DanhMuc dm ON sp.idDanhMuc.id = dm.id
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false 
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY dm.id, dm.tenDanhMuc
        ORDER BY SUM(hct.gia * hct.soLuong) DESC
        """)
    List<Object[]> getCategoryPerformance(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT sp.id, sp.tenSanPham, sp.ma, dm.tenDanhMuc,
        MAX(asp.id),
        SUM(hct.soLuong), SUM(hct.gia * hct.soLuong)
        FROM HoaDon h
        JOIN HoaDonChiTiet hct ON h.id = hct.idHoaDon.id AND hct.deleted = false
        JOIN ChiTietSanPham ctsp ON hct.idChiTietSp.id = ctsp.id
        JOIN SanPham sp ON ctsp.idSanPham.id = sp.id
        JOIN DanhMuc dm ON sp.idDanhMuc.id = dm.id
        LEFT JOIN AnhSanPham asp ON ctsp.idAnhSanPham.id = asp.id AND asp.deleted = false
        WHERE h.trangThai NOT IN (0, 6, 8) AND h.deleted = false
        AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY sp.id, sp.tenSanPham, sp.ma, dm.tenDanhMuc
        ORDER BY SUM(hct.soLuong) DESC, SUM(hct.gia * hct.soLuong) DESC
        """)
    List<Object[]> getTopProducts(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT h.id, h.ma, h.tenKhachHang, 
        (SELECT COUNT(hct2) FROM HoaDonChiTiet hct2 WHERE hct2.idHoaDon.id = h.id),
        h.tongTienSauGiam, h.trangThai, h.createdAt
        FROM HoaDon h
        WHERE h.deleted = false
        ORDER BY h.createdAt DESC
        """)
    List<Object[]> getRecentOrders();

    // Customer order history queries - Include all orders (both deleted and non-deleted for customer view)
    @Query("SELECT h FROM HoaDon h WHERE h.idKhachHang = :khachHang ORDER BY h.createdAt DESC")
    Page<HoaDon> findByIdKhachHangOrderByCreatedAtDesc(@Param("khachHang") com.example.sd_24_striker_be.modal.account.KhachHang khachHang, Pageable pageable);

    @Query("SELECT h FROM HoaDon h WHERE h.idKhachHang = :khachHang")
    List<HoaDon> findByIdKhachHang(@Param("khachHang") com.example.sd_24_striker_be.modal.account.KhachHang khachHang);
    
    // Debug query to check all orders for customer including deleted ones
    @Query("SELECT h FROM HoaDon h WHERE h.idKhachHang = :khachHang ORDER BY h.createdAt DESC")
    List<HoaDon> findAllByIdKhachHangIncludingDeleted(@Param("khachHang") com.example.sd_24_striker_be.modal.account.KhachHang khachHang);

    @Query("SELECT h FROM HoaDon h WHERE h.idKhachHang.id = :khachHangId AND h.trangThai = :trangThai")
    List<HoaDon> findByIdKhachHangAndTrangThai(@Param("khachHangId") Integer khachHangId, @Param("trangThai") short trangThai);

    /**
     * Dữ liệu thô để bóc tách doanh thu theo kênh bán / phương thức thanh toán.
     * Mỗi hóa đơn hợp lệ trả về ĐÚNG MỘT dòng: {@code [loaiDon, kieuThanhToan, tongTienSauGiam]}.
     * <p>
     * Cố tình KHÔNG join thẳng sang hinh_thuc_thanh_toan: một hóa đơn có thể có nhiều bản
     * ghi thanh toán (trả góp / kết hợp tiền mặt + chuyển khoản), join thẳng sẽ nhân đôi
     * dòng hóa đơn và thổi phồng doanh thu. Dùng truy vấn con lấy một phương thức đại diện
     * để tổng các kênh luôn khớp đúng bằng {@link #getTotalRevenueByDateRange}.
     * <p>
     * Điều kiện lọc trạng thái giữ y hệt các query doanh thu khác (loại 0, 6, 8).
     */
    @Query("""
        SELECT h.loaiDon,
               (SELECT MIN(pt.kieuThanhToan)
                  FROM HinhThucThanhToan ht
                  JOIN ht.idPhuongThucThanhToan pt
                 WHERE ht.idHoaDon.id = h.id AND ht.deleted = false),
               COALESCE(h.tongTienSauGiam, 0)
        FROM HoaDon h
        WHERE h.trangThai NOT IN (0, 6, 8)
          AND h.deleted = false
          AND CAST(h.ngayTao AS DATE) BETWEEN :startDate AND :endDate
        """)
    List<Object[]> getRevenueBreakdownRows(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
