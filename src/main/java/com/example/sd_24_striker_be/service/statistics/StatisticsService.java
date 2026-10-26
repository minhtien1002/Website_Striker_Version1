package com.example.sd_24_striker_be.service.statistics;

import com.example.sd_24_striker_be.dto.statistics.DashboardStatsResponse;
import com.example.sd_24_striker_be.dto.statistics.RevenueBreakdownResponse;
import com.example.sd_24_striker_be.dto.statistics.RevenueDataPoint;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    DashboardStatsResponse getDashboardStats();

    DashboardStatsResponse getDashboardStatsByDateRange(String startDate, String endDate);

    List<RevenueDataPoint> getRevenueDataByPeriod(LocalDate startDate, LocalDate endDate, String period);

    /**
     * Bóc tách doanh thu trong khoảng ngày theo kênh bán (Tại quầy / COD / Chuyển khoản)
     * và theo từng phương thức thanh toán cụ thể.
     */
    RevenueBreakdownResponse getRevenueBreakdown(LocalDate startDate, LocalDate endDate);
}
