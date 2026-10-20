package com.example.sd_24_striker_be.controller.Client;

import com.example.sd_24_striker_be.config.VNPayConfig;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.sell.HinhThucThanhToanRepository;
import com.example.sd_24_striker_be.service.VNPayService;
import com.example.sd_24_striker_be.service.sale.Client.impl.BanHangClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VNPayClientControllerTest {

    @Mock
    private VNPayService vnPayService;
    @Mock
    private VNPayConfig vnPayConfig;
    @Mock
    private BanHangClientService banHangClientService;
    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private HinhThucThanhToanRepository hinhThucThanhToanRepository;

    @InjectMocks
    private VNPayClientController controller;

    @Test
    void paymentAmountMustMatchInvoiceTotalExactly() {
        assertTrue(VNPayClientController.paymentAmountMatches(new BigDecimal("505100.00"), "50510000"));
        assertFalse(VNPayClientController.paymentAmountMatches(new BigDecimal("505100.00"), "50509900"));
        assertFalse(VNPayClientController.paymentAmountMatches(new BigDecimal("505100.00"), "invalid"));
        assertFalse(VNPayClientController.paymentAmountMatches(null, "50510000"));
    }

    @Test
    void missingInvoiceIsNotReportedAsSuccessfulPayment() {
        when(hoaDonRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getPaymentStatus(99);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not_found", body(response).get("status"));
        verifyNoInteractions(hinhThucThanhToanRepository);
    }

    @Test
    void pendingInvoiceRemainsPending() {
        HoaDon invoice = invoice((short) 0, "120000");
        when(hoaDonRepository.findById(7)).thenReturn(Optional.of(invoice));

        ResponseEntity<?> response = controller.getPaymentStatus(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pending", body(response).get("status"));
        verifyNoInteractions(hinhThucThanhToanRepository);
    }

    @Test
    void completedInvoiceNeedsMatchingVNPayRecord() {
        HoaDon invoice = invoice((short) 1, "120000");
        when(hoaDonRepository.findById(7)).thenReturn(Optional.of(invoice));
        when(hinhThucThanhToanRepository.existsSuccessfulVNPayPayment(7, new BigDecimal("120000")))
                .thenReturn(true);

        ResponseEntity<?> response = controller.getPaymentStatus(7);

        assertEquals("success", body(response).get("status"));
    }

    @Test
    void lookupFailureIsReportedAsErrorNeverSuccess() {
        when(hoaDonRepository.findById(7)).thenThrow(new RuntimeException("database unavailable"));

        ResponseEntity<?> response = controller.getPaymentStatus(7);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", body(response).get("status"));
    }

    private static HoaDon invoice(short status, String total) {
        HoaDon invoice = new HoaDon();
        invoice.setTrangThai(status);
        invoice.setTongTienSauGiam(new BigDecimal(total));
        return invoice;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }
}
