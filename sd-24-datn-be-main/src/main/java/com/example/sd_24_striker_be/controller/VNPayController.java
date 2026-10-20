package com.example.sd_24_striker_be.controller;

import com.example.sd_24_striker_be.dto.request.PaymentRequest;
import com.example.sd_24_striker_be.service.VNPayService;
import com.example.sd_24_striker_be.service.bill.impl.HoaDonServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private HoaDonServiceImpl hoaDonService;

    @Value("${admin.frontend.url}")
    private String adminFrontendUrl;

    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createVNPayPayment(HttpServletRequest request, @RequestBody PaymentRequest paymentRequest) {
        try {
            // Validate payment request
            if (paymentRequest.getInvoiceId() == null) {
                return ResponseEntity.badRequest().body("Invoice ID is required");
            }

            // Không tin số tiền từ frontend. Tổng được tính lại từ giỏ hàng và voucher
            // trong DB; callback cũng kiểm tra lại đúng số tiền này.
            long amount = hoaDonService.calculatePendingInvoiceTotal(paymentRequest.getInvoiceId()).longValueExact();
            String orderInfo = "Thanh toan hoa don " + paymentRequest.getInvoiceId();
            String returnUrl = "http://localhost:8080/api/payment/vnpay-return";
            String invoiceId = paymentRequest.getInvoiceId().toString();

            // Create VNPay payment URL
            String paymentUrl = vnPayService.createOrder(request, amount, orderInfo, returnUrl, invoiceId);

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("invoiceId", invoiceId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating VNPay payment: " + e.getMessage());
        }
    }

    @GetMapping("/vnpay-return")
    public RedirectView vnPayReturn(HttpServletRequest request) {
        try {
            String invoiceId = vnPayService.orderReturn(request);

            if (invoiceId != null && !invoiceId.isEmpty()) {
                // Payment successful - redirect to sales counter with success message
                return new RedirectView(adminFrontendUrl + "/ban-tai-quay?payment=success&invoiceId=" + invoiceId);
            } else {
                // Payment failed - redirect to sales counter with error message
                return new RedirectView(adminFrontendUrl + "/ban-tai-quay?payment=failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Error occurred - redirect to sales counter with error message
            return new RedirectView(adminFrontendUrl + "/ban-tai-quay?payment=error");
        }
    }

    @PostMapping("/vnpay/process-combined")
    public ResponseEntity<?> processCombinedPayment(HttpServletRequest request, @RequestBody PaymentRequest paymentRequest) {
        // Luồng cũ ghi tiền mặt trước khi VNPay thành công rồi callback lại ghi toàn bộ
        // thành chuyển khoản, làm dữ liệu thu tiền bị trùng. Chặn đến khi có giao dịch
        // pending và cơ chế commit/rollback hai pha đúng nghĩa.
        return ResponseEntity.badRequest()
                .body("Thanh toán kết hợp tiền mặt + VNPay chưa được hỗ trợ an toàn");
    }
}
