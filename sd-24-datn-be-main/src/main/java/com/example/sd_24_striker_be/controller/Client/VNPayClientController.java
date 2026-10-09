package com.example.sd_24_striker_be.controller.Client;

import com.example.sd_24_striker_be.config.VNPayConfig;
import com.example.sd_24_striker_be.dto.sell.request.ClientPaymentRequest;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.service.VNPayService;
import com.example.sd_24_striker_be.service.sale.Client.impl.BanHangClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/client-payment")
@CrossOrigin(origins = "*")
public class VNPayClientController {

    // Constructor để debug component loading
    public VNPayClientController() {
        System.out.println("=== VNPayClientController LOADED ===");
    }

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private BanHangClientService banHangClientService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Value("${client.frontend.url:http://localhost:5173}")
    private String clientFrontendUrl;

    /**
     * Create VNPay payment URL for client checkout
     */
    @PostMapping("/vnpay/create/{hoaDonId}")
    public ResponseEntity<?> createVNPayPayment(
            HttpServletRequest request, 
            @PathVariable Integer hoaDonId,
            @RequestBody ClientPaymentRequest paymentRequest) {
        try {
            System.out.println("=== CLIENT VNPAY CREATE PAYMENT ENDPOINT HIT ===");
            System.out.println("Creating VNPay payment for client - Invoice ID: " + hoaDonId);
            System.out.println("Payment request: " + paymentRequest);
            System.out.println("Voucher ID from request: " + paymentRequest.getVoucherId());
            System.out.println("Voucher ID type: " + (paymentRequest.getVoucherId() != null ? paymentRequest.getVoucherId().getClass().getSimpleName() : "null"));
            
            // Validate payment request
            if (hoaDonId == null) {
                return ResponseEntity.badRequest().body("Invoice ID is required");
            }

            // Pre-save customer information to invoice before VNPay redirect
            // This ensures customer data is preserved even if VNPay return fails
            // Bước này cũng tính lại tiền hàng / phí ship / tiền giảm ở server; nếu lỗi (vd voucher
            // hết hạn) thì dừng luôn thay vì vẫn tạo link thanh toán như trước.
            try {
                banHangClientService.updateCustomerInfo(hoaDonId, paymentRequest);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            // Số tiền VNPay lấy từ tổng server vừa tính. Trước đây = giỏ hàng + phí ship - tiền
            // giảm do CLIENT gửi lên, nên sửa request là tạo được link thanh toán với số tiền tùy ý.
            BigDecimal totalAmount = hoaDonRepository.findById(hoaDonId)
                    .map(hd -> hd.getTongTienSauGiam())
                    .orElse(null);
            
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Invalid payment amount");
            }

            // Convert amount to long (VNPay expects amount in VND)
            long amount = totalAmount.longValue();
            String orderInfo = "Thanh toan don hang #" + hoaDonId + " - " + paymentRequest.getTenKhachHang();
            String returnUrl = "http://localhost:8080/api/client-payment/vnpay-return";
            String invoiceId = hoaDonId.toString();

            // Create VNPay payment URL
            String paymentUrl = vnPayService.createOrder(request, amount, orderInfo, returnUrl, invoiceId);

            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("invoiceId", invoiceId);
            response.put("amount", amount);
            response.put("orderInfo", orderInfo);

            System.out.println("VNPay payment URL created: " + paymentUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating VNPay payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating VNPay payment: " + e.getMessage());
        }
    }

    /**
     * Handle VNPay return callback for client
     */
    @GetMapping("/vnpay-return")
    public RedirectView vnPayReturn(HttpServletRequest request) {
        try {
            System.out.println("VNPay return callback received for CLIENT");
            
            // Use custom client VNPay processing instead of shared service
            String invoiceId = processClientVNPayReturn(request);

            if (invoiceId != null && !invoiceId.isEmpty()) {
                System.out.println("VNPay payment successful for client invoice: " + invoiceId);
                
                try {
                    // Get order details to determine redirect path
                    var orderDetails = banHangClientService.getOrderDetails(Integer.parseInt(invoiceId));
                    String orderCode = orderDetails.getMaHoaDon();
                    
                    // Check if customer is authenticated (guest orders usually have minimal customer info)
                    // For now, assume all VNPay orders are from guests since we need to redirect to order lookup
                    // TODO: Implement proper authentication check when customer ID field is available
                    boolean isAuthenticated = false; // Force guest flow for VNPay orders
                    
                    if (isAuthenticated) {
                        // Authenticated users: Go to purchase history
                        System.out.println("Authenticated user - redirecting to purchase history");
                        return new RedirectView(clientFrontendUrl + "/account/purchase-history?payment=vnpay&status=success&invoiceId=" + invoiceId);
                    } else {
                        // Guest users: Go to order lookup with auto search
                        System.out.println("Guest user - redirecting to order lookup with code: " + orderCode);
                        return new RedirectView(clientFrontendUrl + "/orders?orderCode=" + orderCode + "&autoSearch=true&payment=vnpay&status=success");
                    }
                } catch (Exception e) {
                    System.err.println("Error getting order details for redirect: " + e.getMessage());
                    // Fallback to order success page
                    return new RedirectView(clientFrontendUrl + "/order-success?payment=vnpay&status=success&orderId=" + invoiceId);
                }
            } else {
                System.out.println("VNPay payment failed for client");
                // Payment failed - redirect to client checkout with error
                return new RedirectView(clientFrontendUrl + "/checkout?payment=vnpay&status=failed");
            }
        } catch (Exception e) {
            System.err.println("Error processing client VNPay return: " + e.getMessage());
            e.printStackTrace();
            // Error occurred - redirect to client checkout with error
            return new RedirectView(clientFrontendUrl + "/checkout?payment=vnpay&status=error");
        }
    }

    /**
     * Process VNPay return specifically for client (separate from admin)
     */
    private String processClientVNPayReturn(HttpServletRequest request) {
        try {
            // Debug: Print all parameters
            System.out.println("=== CLIENT VNPAY RETURN DEBUG ===");
            request.getParameterNames().asIterator().forEachRemaining(paramName -> {
                System.out.println(paramName + " = " + request.getParameter(paramName));
            });

            // Xác thực chữ ký VNPay (vnp_SecureHash) trước khi tin bất kỳ tham số nào từ
            // request - trước đây route này đọc thẳng vnp_TransactionStatus/vnp_ResponseCode
            // mà không kiểm tra chữ ký, cho phép giả mạo callback "thanh toán thành công".
            if (!verifySignature(request)) {
                System.err.println("CLIENT: Invalid VNPay signature - rejecting callback");
                return null;
            }

            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

            System.out.println("Client VNPay - TxnRef: " + vnp_TxnRef + ", Status: " + vnp_TransactionStatus + ", ResponseCode: " + vnp_ResponseCode);

            // Extract invoice ID from TxnRef
            String invoiceId = null;
            if (vnp_TxnRef != null && vnp_TxnRef.contains("_")) {
                invoiceId = vnp_TxnRef.split("_")[0];
            }

            if (invoiceId == null) {
                System.err.println("CLIENT: Could not extract invoiceId from vnp_TxnRef: " + vnp_TxnRef);
                return null;
            }

            // Check if payment is successful
            if ("00".equals(vnp_TransactionStatus) && "00".equals(vnp_ResponseCode)) {
                System.out.println("CLIENT: VNPay payment successful for invoice: " + invoiceId);

                try {
                    Integer hoaDonId = Integer.parseInt(invoiceId);

                    Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
                    Short currentStatus = hoaDonOpt.map(HoaDon::getTrangThai).orElse(null);
                    if (currentStatus != null && currentStatus != 0 && currentStatus != com.example.sd_24_striker_be.common.bill.HoaDonStatus.DA_HUY) {
                        // Hóa đơn đã được xử lý thành công từ trước (VNPay có thể gọi lại
                        // callback này nhiều lần) - coi là thành công, KHÔNG gọi lại
                        // thanhToan() để tránh lỗi "không phải hóa đơn chờ". Không áp dụng
                        // cho đơn đã bị hủy (vd hết hạn giữ giỏ hàng) - phải báo lỗi thật.
                        System.out.println("CLIENT: Invoice " + invoiceId + " already processed (trangThai=" + currentStatus + ") - treating as success");
                        return invoiceId;
                    }

                    // Simply complete the payment - let the service preserve existing customer data
                    // The thanhToan method will use existing HoaDon data and only update payment method
                    ClientPaymentRequest vnpayRequest = ClientPaymentRequest.builder()
                            .phuongThucThanhToan("VNPAY")
                            .ghiChu("Thanh toán qua VNPay")
                            .build();

                    // Complete the payment
                    banHangClientService.thanhToan(hoaDonId, vnpayRequest);
                    System.out.println("CLIENT: VNPay payment completed successfully for invoice: " + invoiceId);

                    return invoiceId;
                } catch (Exception e) {
                    // QUAN TRỌNG: không được coi đây là thành công. Trước đây lỗi ở bước
                    // này (giỏ hàng trống, hóa đơn đã bị hủy, lỗi DB...) vẫn bị nuốt và trả
                    // về invoiceId như thể thanh toán xong -> khách bị chuyển tới trang
                    // "thành công" trong khi ĐƠN HÀNG THỰC SỰ CHƯA ĐƯỢC TẠO trong hệ thống,
                    // khiến đơn "biến mất" và không lên thống kê dù VNPay đã báo thu tiền.
                    System.err.println("CLIENT: Error completing VNPay payment for invoice " + invoiceId + ": " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.out.println("CLIENT: VNPay payment failed - Status: " + vnp_TransactionStatus + ", ResponseCode: " + vnp_ResponseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("CLIENT: Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xác thực vnp_SecureHash theo đúng thuật toán VNPay yêu cầu (giống VNPayService.orderReturn).
     */
    private boolean verifySignature(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = fields.get("vnp_SecureHash");
            if (vnp_SecureHash == null) {
                return false;
            }
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = fields.get(fieldName);
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }

            String computedHash = VNPayConfig.hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());
            return computedHash.equalsIgnoreCase(vnp_SecureHash);
        } catch (Exception e) {
            System.err.println("CLIENT: Error verifying VNPay signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get VNPay payment status
     */
    @GetMapping("/vnpay/status/{hoaDonId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Integer hoaDonId) {
        try {
            // Check invoice status in database
            var cartResponse = banHangClientService.layGioHang(hoaDonId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", hoaDonId);
            
            // Determine status based on invoice data
            if (cartResponse != null) {
                // If cart still exists, payment is pending
                response.put("status", "pending");
                response.put("message", "Payment is still pending");
            } else {
                // If cart is cleared, payment was successful
                response.put("status", "success");
                response.put("message", "Payment completed successfully");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error getting payment status: " + e.getMessage());
            
            // If cart not found, assume payment was completed
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", hoaDonId);
            response.put("status", "success");
            response.put("message", "Payment status unknown, assuming completed");
            
            return ResponseEntity.ok(response);
        }
    }
}
