package com.example.sd_24_striker_be.config;

import com.example.sd_24_striker_be.service.bill.HoaDonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tự động xóa toàn bộ hóa đơn chờ tại quầy mỗi ngày. Lịch chạy lấy từ cấu hình
 * {@code app.hoa-don-cho.xoa-cron} (mặc định 0h00 hằng ngày, giờ Việt Nam) nên đổi giờ không cần sửa code.
 * Số lượng hóa đơn chờ không bị giới hạn.
 */
@Configuration
@EnableScheduling
public class HoaDonChoScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoaDonChoScheduler.class);

    // ObjectProvider: lấy service lúc chạy job, không bắt buộc có khi khởi tạo context
    private final org.springframework.beans.factory.ObjectProvider<HoaDonService> hoaDonService;

    public HoaDonChoScheduler(org.springframework.beans.factory.ObjectProvider<HoaDonService> hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @Scheduled(cron = "${app.hoa-don-cho.xoa-cron:0 0 0 * * *}", zone = "${app.hoa-don-cho.zone:Asia/Ho_Chi_Minh}")
    public void xoaHoaDonCho() {
        try {
            int n = hoaDonService.getObject().deleteAllPendingInvoices();
            log.info("Đã tự động xóa {} hóa đơn chờ", n);
        } catch (Exception e) {
            log.error("Lỗi khi tự động xóa hóa đơn chờ: {}", e.getMessage(), e);
        }
    }
}
