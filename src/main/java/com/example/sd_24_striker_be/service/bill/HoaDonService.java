package com.example.sd_24_striker_be.service.bill;

import java.math.BigDecimal;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.dto.bill.request.UpdateCustomerRequest;
import com.example.sd_24_striker_be.dto.sell.request.PaymentRequest;
import com.example.sd_24_striker_be.dto.sell.response.PaymentResponse;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface HoaDonService {
    Page<HoaDonDTOResponse> getHoaDonAndFilters(String keyword, Long minAmount, Long maxAmount,
                                                Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon, Pageable pageable);

    HoaDonDetailResponse getHoaDonDetail(Integer id);

    Map<String, Long> getStatusCounts();

    Map<String, Long> getPriceRange();

    BigDecimal getCompletedRevenue();

    HoaDonDTOResponse updateHoaDonStatus(Integer id, Short trangThai, Integer idNhanVien);

    HoaDonDTOResponse updateHoaDonStatusWithNote(Integer id, Short trangThai, Integer idNhanVien, String ghiChu);

    HoaDonDTOResponse getHoaDonByMa(String maHoaDon);

    // Sales counter specific methods
    List<HoaDon> getPendingInvoicesForSales();
    
    HoaDon createPendingInvoice();
    
    HoaDon updatePendingInvoiceCustomer(Integer id, UpdateCustomerRequest request);
    
    void deletePendingInvoice(Integer id);
    
    // Payment processing method
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    
    // VNPay specific methods
    void updateInvoiceStatusAfterPayment(Integer invoiceId, Integer status);
    
    void updateInvoiceStatusAfterVNPayPayment(Integer invoiceId, Integer status, Long vnpayAmount);
    
    void processCashPayment(Integer invoiceId, java.math.BigDecimal cashAmount);

    // Hủy đơn: idNhanVien khác null khi admin thao tác, idKhachHangActor khác null khi khách tự hủy.
    HoaDonDTOResponse cancelOrder(Integer id, Integer idNhanVien, Integer idKhachHangActor, String lyDo);

    // Khách hàng yêu cầu hoàn hàng cho một đơn đã hoàn thành.
    HoaDonDTOResponse requestReturn(Integer id, Integer idKhachHangActor, String lyDo);

    // Admin xác nhận đã nhận hàng hoàn về kho -> cộng lại tồn kho.
    HoaDonDTOResponse confirmReturn(Integer id, Integer idNhanVien, String ghiChu);

    // Admin từ chối yêu cầu hoàn hàng, đơn quay lại trạng thái Đã hoàn thành.
    HoaDonDTOResponse rejectReturn(Integer id, Integer idNhanVien, String lyDo);

    // Xuất Excel danh sách hóa đơn theo cùng bộ lọc với trang danh sách admin.
    byte[] exportHoaDonExcel(String keyword, Long minAmount, Long maxAmount,
                              Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon);

}
