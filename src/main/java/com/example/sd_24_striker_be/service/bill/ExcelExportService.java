package com.example.sd_24_striker_be.service.bill;

import java.sql.Timestamp;

public interface ExcelExportService {

    /**
     * Xuất danh sách hóa đơn (.xlsx) theo đúng bộ lọc đang dùng ở trang danh sách hóa đơn
     * admin ({@link HoaDonService#getHoaDonAndFilters}), không phân trang.
     */
    byte[] exportHoaDonList(String keyword, Long minAmount, Long maxAmount,
                             Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon);
}
