package com.example.sd_24_striker_be.repository.account.KhachHang;

import com.example.sd_24_striker_be.dto.account.response.KhachHang.KhachHangDTOResponse;
import com.example.sd_24_striker_be.modal.account.KhachHang;
import com.example.sd_24_striker_be.modal.account.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    
    @Query("SELECT kh FROM KhachHang kh WHERE kh.ma <> 'KH001'")
    List<KhachHang> findAllExceptKH001();
    
    @Query("""
            SELECT new com.example.sd_24_striker_be.dto.account.response.KhachHang.KhachHangDTOResponse(
            kh.id,
            kh.ma,
            kh.ten,
            kh.taiKhoan.email,
            kh.taiKhoan.soDienThoai,
            kh.gioiTinh,
            kh.ngaySinh,
            kh.createdAt,
            kh.updatedAt,
            kh.deleted
            ) 
            FROM KhachHang kh
            WHERE (
            :keyword IS NULL
            OR kh.ma LIKE %:keyword%
            OR kh.ten LIKE %:keyword%
            OR kh.taiKhoan.soDienThoai LIKE %:keyword%
            OR kh.taiKhoan.email LIKE %:keyword%
            OR kh.cccd LIKE %:keyword%
            OR (:keywordAsDate IS NOT NULL AND kh.ngaySinh = :keywordAsDate)
            )
            AND (:gioiTinh IS NULL OR kh.gioiTinh = :gioiTinh)
            AND (:trangThai IS NULL OR kh.deleted = :trangThai)
            """)
    Page<KhachHangDTOResponse> getAllKH(
            @Param("keyword") String keyWord,
            @Param("keywordAsDate") Instant keywordAsDate,
            @Param("gioiTinh") Short gioiTinh,
            @Param("trangThai") Boolean trangThai,
            Pageable pageable);
    
    /**
     * Get active customers for sales counter with phone numbers from account table
     */
    @Query("""
            SELECT new com.example.sd_24_striker_be.dto.account.response.KhachHang.KhachHangDTOResponse(
            kh.id,
            kh.ma,
            kh.ten,
            kh.taiKhoan.email,
            kh.taiKhoan.soDienThoai,
            kh.gioiTinh,
            kh.ngaySinh,
            kh.createdAt,
            kh.updatedAt,
            kh.taiKhoan.deleted
            ) 
            FROM KhachHang kh
            JOIN kh.taiKhoan tk
            WHERE (kh.deleted = true OR kh.deleted IS NULL)
            AND (:keyword IS NULL OR :keyword = '' OR
                 LOWER(kh.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(kh.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(tk.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<KhachHangDTOResponse> findActiveCustomersForSales(Pageable pageable, @Param("keyword") String keyword);

    /**
     * Get all active customers without pagination for dropdown lists
     */
    @Query("""
            SELECT new com.example.sd_24_striker_be.dto.account.response.KhachHang.KhachHangDTOResponse(
            kh.id,
            kh.ma,
            kh.ten,
            kh.taiKhoan.email,
            kh.taiKhoan.soDienThoai,
            kh.gioiTinh,
            kh.ngaySinh,
            kh.createdAt,
            kh.updatedAt,
            kh.taiKhoan.deleted
            ) 
            FROM KhachHang kh
            JOIN kh.taiKhoan tk
            WHERE (kh.deleted = true OR kh.deleted IS NULL)
            ORDER BY kh.ten ASC
            """)
    List<KhachHangDTOResponse> findAllActiveKhachHang();

    // Statistics query (không tính bản ghi "Khách lẻ" dùng chung cho đơn vãng lai)
    @Query("SELECT COUNT(kh) FROM KhachHang kh WHERE (kh.deleted = true OR kh.deleted IS NULL) AND (kh.ma IS NULL OR kh.ma <> 'KHACH_LE')")
    Long getTotalActiveCustomers();

    /**
     * Mã của bản ghi "Khách lẻ" dùng chung cho đơn tại quầy / khách vãng lai. Không có tài
     * khoản nên không xuất hiện ở các danh sách khách hàng (đều JOIN taiKhoan). Trước đây code
     * dùng tạm khách ID=1 - một khách thật - nên khách đó thấy đơn của người khác.
     */
    String MA_KHACH_LE = "KHACH_LE";

    Optional<KhachHang> findFirstByMa(String ma);

    // Find customer by TaiKhoan for authentication
    Optional<KhachHang> findByTaiKhoan(TaiKhoan taiKhoan);
    
    // Find customer by TaiKhoan ID directly
    @Query("SELECT kh FROM KhachHang kh WHERE kh.taiKhoan.id = :taiKhoanId")
    Optional<KhachHang> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);
    
    // Find customer by phone number (from TaiKhoan)
    @Query("SELECT kh FROM KhachHang kh WHERE kh.taiKhoan.soDienThoai = :soDienThoai")
    Optional<KhachHang> findBySoDienThoai(@Param("soDienThoai") String soDienThoai);
}