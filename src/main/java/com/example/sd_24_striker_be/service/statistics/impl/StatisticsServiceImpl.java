package com.example.sd_24_striker_be.service.statistics.impl;

import com.example.sd_24_striker_be.dto.statistics.*;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonChiTietRepository;
import com.example.sd_24_striker_be.repository.account.KhachHang.KhachHangRepository;
import com.example.sd_24_striker_be.repository.product.SanPhamRepository;
import com.example.sd_24_striker_be.service.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final com.example.sd_24_striker_be.repository.product.AnhSanPhamRepository anhSanPhamRepository;

    private static final String ANH_MAC_DINH =
        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=100&h=100&fit=crop&crop=center";

    @Override
    public DashboardStatsResponse getDashboardStats() {
        // Get stats for last 30 days by default
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        return getDashboardStatsByDateRange(
            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }

    @Override
    public DashboardStatsResponse getDashboardStatsByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate today = LocalDate.now();

        // Main stats - mọi đơn KHÔNG bị loại khỏi doanh thu (loại trừ đơn nháp=0, đã hủy=6,
        // đã hoàn hàng=8 - xem HoaDonRepository và HoaDonStatus.isExcludedFromRevenue).
        // (Chú thích cũ ở đây ghi nhầm "trangThai = 2 nghĩa là hoàn thành" - không đúng,
        // trangThai=2 thực ra là "Chờ xử lý"; bản thân câu query bên dưới vẫn đúng.)
        BigDecimal totalRevenue = hoaDonRepository.getTotalRevenueByDateRange(start, end);
        Long totalOrders = hoaDonRepository.getTotalOrdersByDateRange(start, end);
        Long totalCustomers = khachHangRepository.getTotalActiveCustomers();
        Long totalProducts = sanPhamRepository.getTotalActiveProducts();

        // Today's stats for breadcrumb
        BigDecimal todayRevenue = hoaDonRepository.getTotalRevenueByDate(today);
        Long todayOrders = hoaDonRepository.getTotalOrdersByDate(today);
        // CHƯA có hệ thống theo dõi lượt truy cập thật - trước đây trả về hằng số giả
        // (1234) trông như dữ liệu thật. Trả về 0 để không đánh lừa người xem thống kê;
        // hiển thị "N/A"/ẩn số liệu này ở FE cho tới khi có tracking thực sự.
        Long todayVisitors = 0L;

        // Revenue trend data (last 15 days)
        List<RevenueDataPoint> revenueData = getRevenueDataPoints(start, end);

        // Category performance
        List<CategoryPerformance> categoryData = getCategoryPerformance(start, end);

        // Sản phẩm bán chạy: luôn tính trong 1 tháng gần nhất (không theo khoảng ngày đang lọc)
        List<TopProductStats> topProducts = getTopProductsLastMonth(today);

        // Recent orders
        List<RecentOrderStats> recentOrders = getRecentOrders();

        return DashboardStatsResponse.builder()
            .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .totalOrders(totalOrders != null ? totalOrders : 0L)
            .totalCustomers(totalCustomers != null ? totalCustomers : 0L)
            .totalProducts(totalProducts != null ? totalProducts : 0L)
            .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
            .todayOrders(todayOrders != null ? todayOrders : 0L)
            .todayVisitors(todayVisitors)
            .revenueData(revenueData)
            .categoryData(categoryData)
            .topProducts(topProducts)
            .recentOrders(recentOrders)
            .build();
    }

    private List<RevenueDataPoint> getRevenueDataPoints(LocalDate startDate, LocalDate endDate) {
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        
        // Get daily revenue for the date range
        List<Object[]> dailyRevenue = hoaDonRepository.getDailyRevenue(startDate, endDate);
        
        for (Object[] row : dailyRevenue) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            
            dataPoints.add(RevenueDataPoint.builder()
                .date(date.format(DateTimeFormatter.ofPattern("d/M")))
                .revenue(revenue)
                .orderCount(orderCount)
                .build());
        }
        
        return dataPoints;
    }

    @Override
    public List<RevenueDataPoint> getRevenueDataByPeriod(LocalDate startDate, LocalDate endDate, String period) {
        switch (period.toLowerCase()) {
            case "weekly":
                return getWeeklyRevenueData(startDate, endDate);
            case "monthly":
                return getMonthlyRevenueData(startDate, endDate);
            case "yearly":
                return getYearlyRevenueData(startDate, endDate);
            default:
                return getDailyRevenueData(startDate, endDate);
        }
    }
    
    private List<RevenueDataPoint> getDailyRevenueData(LocalDate startDate, LocalDate endDate) {
        return getRevenueDataPoints(startDate, endDate);
    }
    
    private List<RevenueDataPoint> getWeeklyRevenueData(LocalDate startDate, LocalDate endDate) {
        List<Object[]> weeklyRevenue = hoaDonRepository.getWeeklyRevenue(startDate, endDate);
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        
        for (Object[] row : weeklyRevenue) {
            Integer year = ((Number) row[0]).intValue();
            Integer week = ((Number) row[1]).intValue();
            BigDecimal revenue = (BigDecimal) row[2];
            Long orderCount = ((Number) row[3]).longValue();
            
            // Calculate the first day of the week for the given year and week
            // Handle edge cases where week calculation might fail
            LocalDate firstDayOfWeek;
            try {
                firstDayOfWeek = LocalDate.of(year, 1, 1)
                    .with(java.time.temporal.WeekFields.ISO.weekOfYear(), week)
                    .with(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1);
            } catch (Exception e) {
                // Fallback: use the first day of the year if week calculation fails
                firstDayOfWeek = LocalDate.of(year, 1, 1);
            }
            
            dataPoints.add(RevenueDataPoint.builder()
                .date(firstDayOfWeek.toString())
                .revenue(revenue)
                .orderCount(orderCount)
                .build());
        }
        
        return dataPoints;
    }
    
    private List<RevenueDataPoint> getMonthlyRevenueData(LocalDate startDate, LocalDate endDate) {
        List<Object[]> monthlyRevenue = hoaDonRepository.getMonthlyRevenue(startDate, endDate);
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        
        for (Object[] row : monthlyRevenue) {
            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();
            BigDecimal revenue = (BigDecimal) row[2];
            Long orderCount = ((Number) row[3]).longValue();
            
            // Create the first day of the month with validation
            LocalDate firstDayOfMonth;
            try {
                firstDayOfMonth = LocalDate.of(year, month, 1);
            } catch (Exception e) {
                // Fallback: use January 1st if month is invalid
                firstDayOfMonth = LocalDate.of(year, 1, 1);
            }
            
            dataPoints.add(RevenueDataPoint.builder()
                .date(firstDayOfMonth.toString())
                .revenue(revenue)
                .orderCount(orderCount)
                .build());
        }
        
        return dataPoints;
    }
    
    private List<RevenueDataPoint> getYearlyRevenueData(LocalDate startDate, LocalDate endDate) {
        List<Object[]> yearlyRevenue = hoaDonRepository.getYearlyRevenue(startDate, endDate);
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        
        for (Object[] row : yearlyRevenue) {
            Integer year = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            
            // Create January 1st of the year with validation
            LocalDate firstDayOfYear;
            try {
                firstDayOfYear = LocalDate.of(year, 1, 1);
            } catch (Exception e) {
                // Fallback: use current year if year is invalid
                firstDayOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            }
            
            dataPoints.add(RevenueDataPoint.builder()
                .date(firstDayOfYear.toString())
                .revenue(revenue)
                .orderCount(orderCount)
                .build());
        }
        
        return dataPoints;
    }

    private List<CategoryPerformance> getCategoryPerformance(LocalDate startDate, LocalDate endDate) {
        List<Object[]> categoryStats = hoaDonRepository.getCategoryPerformance(startDate, endDate);
        BigDecimal totalRevenue = categoryStats.stream()
            .map(row -> (BigDecimal) row[1])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryStats.stream()
            .map(row -> {
                String categoryName = (String) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                Long orderCount = ((Number) row[2]).longValue();
                Integer productCount = ((Number) row[3]).intValue();
                
                Double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 
                    ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

                return CategoryPerformance.builder()
                    .categoryName(categoryName)
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .productCount(productCount)
                    .percentage(percentage)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Top 5 sản phẩm bán chạy trong 1 tháng gần nhất (tính theo số lượng đã bán, gồm cả
     * hôm nay). Tăng trưởng = so với 1 tháng liền trước đó; null nếu tháng trước chưa bán
     * được cái nào (FE hiển thị "Mới"). Trước đây tăng trưởng là số NGẪU NHIÊN (Math.random).
     */
    private List<TopProductStats> getTopProductsLastMonth(LocalDate today) {
        LocalDate startDate = today.minusMonths(1).plusDays(1);
        LocalDate prevEnd = startDate.minusDays(1);
        LocalDate prevStart = prevEnd.minusMonths(1).plusDays(1);

        java.util.Map<Integer, Long> soldPrevMonth = new java.util.HashMap<>();
        for (Object[] row : hoaDonRepository.getTopProducts(prevStart, prevEnd)) {
            soldPrevMonth.put((Integer) row[0], ((Number) row[5]).longValue());
        }

        List<Object[]> topProductsData = hoaDonRepository.getTopProducts(startDate, today);

        return topProductsData.stream()
            .limit(5) // Top 5 products
            .map(row -> {
                Integer productId = (Integer) row[0];
                String productName = (String) row[1];
                String productCode = (String) row[2];
                String categoryName = (String) row[3];
                // row[4] là id ảnh (không MAX() trực tiếp được trên cột urlAnh kiểu NCLOB)
                String imageUrl = row[4] == null ? ANH_MAC_DINH
                    : anhSanPhamRepository.findById(((Number) row[4]).intValue())
                        .map(a -> a.getUrlAnh())
                        .filter(url -> url != null && !url.isBlank())
                        .orElse(ANH_MAC_DINH);
                Long totalSold = ((Number) row[5]).longValue();
                BigDecimal totalRevenue = (BigDecimal) row[6];

                Long prevSold = soldPrevMonth.get(productId);
                Double growthPercentage = (prevSold == null || prevSold == 0)
                    ? null
                    : BigDecimal.valueOf((totalSold - prevSold) * 100.0 / prevSold)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();

                return TopProductStats.builder()
                    .productId(productId)
                    .productName(productName)
                    .productCode(productCode)
                    .categoryName(categoryName)
                    .imageUrl(imageUrl)
                    .totalSold(totalSold)
                    .totalRevenue(totalRevenue)
                    .growthPercentage(growthPercentage)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<RecentOrderStats> getRecentOrders() {
        List<Object[]> recentOrdersData = hoaDonRepository.getRecentOrders();
        
        return recentOrdersData.stream()
            .limit(5) // Last 5 orders
            .map(row -> {
                Integer orderId = (Integer) row[0];
                String orderCode = (String) row[1];
                String customerName = (String) row[2];
                Integer productCount = ((Number) row[3]).intValue();
                BigDecimal totalAmount = (BigDecimal) row[4];
                Short trangThai = (Short) row[5];
                Object createdAtObj = row[6];
                java.time.OffsetDateTime createdAt;
                if (createdAtObj instanceof java.time.Instant) {
                    createdAt = ((java.time.Instant) createdAtObj).atOffset(java.time.ZoneOffset.UTC);
                } else if (createdAtObj instanceof java.sql.Timestamp) {
                    createdAt = ((java.sql.Timestamp) createdAtObj).toInstant().atOffset(java.time.ZoneOffset.UTC);
                } else {
                    createdAt = java.time.OffsetDateTime.now();
                }
                
                String status = getStatusText(trangThai);
                String customerAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face";

                return RecentOrderStats.builder()
                    .orderId(orderId)
                    .orderCode(orderCode)
                    .customerName(customerName)
                    .customerAvatar(customerAvatar)
                    .productCount(productCount)
                    .totalAmount(totalAmount)
                    .status(status)
                    .createdAt(createdAt)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private String getStatusText(Short trangThai) {
        // Dùng chung bảng mã trạng thái với HoaDonServiceImpl/HoaDonStatus - trước đây
        // lớp này tự định nghĩa một bảng mã khác (0=Chờ xử lý,...,5=Trả hàng) không khớp
        // với phần còn lại của hệ thống, khiến widget "đơn hàng gần đây" hiển thị sai nhãn.
        return com.example.sd_24_striker_be.common.bill.HoaDonStatus.toLabel(trangThai);
    }

    // ==================== Bóc tách doanh thu theo kênh bán / phương thức thanh toán ====================

    // Chỉ còn 2 kênh theo cách thu tiền: COD (thu tiền mặt - gồm cả tiền mặt tại quầy) và
    // Chuyển khoản (chuyển khoản / VNPay / Momo / ZaloPay / thẻ). Trước đây có thêm "Tại quầy"
    // và "Khác", cộng bảng chi tiết từng phương thức nên màn thống kê hiện tới 7 mục.
    private static final String KENH_COD = "COD";
    private static final String KENH_CHUYEN_KHOAN = "CHUYEN_KHOAN";

    private static final Map<String, String> TEN_KENH = Map.of(
            KENH_COD, "COD",
            KENH_CHUYEN_KHOAN, "Chuyển khoản"
    );

    /** Thứ tự cố định để FE luôn vẽ biểu đồ theo cùng một thứ tự cột. */
    private static final List<String> THU_TU_KENH = List.of(KENH_COD, KENH_CHUYEN_KHOAN);

    @Override
    public RevenueBreakdownResponse getRevenueBreakdown(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = hoaDonRepository.getRevenueBreakdownRows(startDate, endDate);

        Map<String, BigDecimal> doanhThuTheoKenh = new LinkedHashMap<>();
        Map<String, Long> soDonTheoKenh = new LinkedHashMap<>();
        for (String kenh : THU_TU_KENH) {
            doanhThuTheoKenh.put(kenh, BigDecimal.ZERO);
            soDonTheoKenh.put(kenh, 0L);
        }

        // Gom theo từng kieu_thanh_toan cụ thể để hiển thị bảng chi tiết bên dưới biểu đồ.
        Map<String, BigDecimal> doanhThuTheoPhuongThuc = new LinkedHashMap<>();
        Map<String, Long> soDonTheoPhuongThuc = new LinkedHashMap<>();
        Map<String, String> kenhCuaPhuongThuc = new LinkedHashMap<>();

        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        long tongSoHoaDon = 0L;

        for (Object[] row : rows) {
            String loaiDon = (String) row[0];
            String kieuThanhToan = (String) row[1];
            BigDecimal tien = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            String kenh = phanLoaiKenh(loaiDon, kieuThanhToan);
            doanhThuTheoKenh.merge(kenh, tien, BigDecimal::add);
            soDonTheoKenh.merge(kenh, 1L, Long::sum);

            String tenPhuongThuc = (kieuThanhToan == null || kieuThanhToan.isBlank())
                    ? "Chưa ghi nhận" : kieuThanhToan;
            doanhThuTheoPhuongThuc.merge(tenPhuongThuc, tien, BigDecimal::add);
            soDonTheoPhuongThuc.merge(tenPhuongThuc, 1L, Long::sum);
            kenhCuaPhuongThuc.putIfAbsent(tenPhuongThuc, kenh);

            tongDoanhThu = tongDoanhThu.add(tien);
            tongSoHoaDon++;
        }

        final BigDecimal tong = tongDoanhThu;
        List<PaymentChannelRevenue> theoKenh = THU_TU_KENH.stream()
                .map(kenh -> PaymentChannelRevenue.builder()
                        .maKenh(kenh)
                        .tenKenh(TEN_KENH.get(kenh))
                        .doanhThu(doanhThuTheoKenh.get(kenh))
                        .soHoaDon(soDonTheoKenh.get(kenh))
                        .tiTrong(tinhTiTrong(doanhThuTheoKenh.get(kenh), tong))
                        .build())
                .collect(Collectors.toList());

        List<PaymentMethodRevenue> theoPhuongThuc = doanhThuTheoPhuongThuc.entrySet().stream()
                .map(e -> PaymentMethodRevenue.builder()
                        .phuongThuc(e.getKey())
                        .maKenh(kenhCuaPhuongThuc.get(e.getKey()))
                        .doanhThu(e.getValue())
                        .soHoaDon(soDonTheoPhuongThuc.get(e.getKey()))
                        .build())
                .sorted((a, b) -> b.getDoanhThu().compareTo(a.getDoanhThu()))
                .collect(Collectors.toList());

        return RevenueBreakdownResponse.builder()
                .tuNgay(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .denNgay(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .tongDoanhThu(tongDoanhThu)
                .tongSoHoaDon(tongSoHoaDon)
                .theoKenh(theoKenh)
                .theoPhuongThuc(theoPhuongThuc)
                .build();
    }

    /**
     * Xếp một hóa đơn vào đúng MỘT trong 2 kênh theo cách thu tiền: thanh toán điện tử
     * (chuyển khoản, VNPay, Momo, ZaloPay, thẻ) -> "Chuyển khoản"; còn lại (COD, tiền mặt tại
     * quầy / khi nhận hàng, chưa ghi nhận) -> "COD". So khớp nới lỏng vì cột
     * {@code phuong_thuc_thanh_toan.kieu_thanh_toan} chứa lẫn nhãn tiếng Việt ("Tiền mặt",
     * "COD - Thanh toán khi nhận hàng") và mã do code sinh ("COD", "VNPAY", "TIEN_MAT").
     */
    private String phanLoaiKenh(String loaiDon, String kieuThanhToan) {
        String kieu = chuanHoa(kieuThanhToan);
        if (kieu.contains("CHUYEN KHOAN") || kieu.contains("VNPAY") || kieu.contains("MOMO")
                || kieu.contains("ZALOPAY") || kieu.contains("BANKING") || kieu.contains("THE")) {
            return KENH_CHUYEN_KHOAN;
        }
        return KENH_COD;
    }

    /** Bỏ dấu tiếng Việt, chuyển hoa, gộp dấu gạch dưới/nối thành khoảng trắng để so khớp nới lỏng. */
    private String chuanHoa(String value) {
        if (value == null) return "";
        String khongDau = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return khongDau.toUpperCase().replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ").trim();
    }

    private BigDecimal tinhTiTrong(BigDecimal phan, BigDecimal tong) {
        if (tong == null || tong.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return phan.multiply(BigDecimal.valueOf(100)).divide(tong, 2, RoundingMode.HALF_UP);
    }
}
