package com.example.sd_24_striker_be.service.bill.impl;

import com.example.sd_24_striker_be.common.bill.HoaDonStatus;
import com.example.sd_24_striker_be.dto.bill.response.HoaDonDTOResponse;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.service.bill.ExcelExportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    private static final String[] HEADERS = {
            "STT", "Mã đơn hàng", "Nhân viên phụ trách", "Khách hàng", "Số điện thoại",
            "Loại đơn", "Ngày tạo", "Phí vận chuyển", "Tổng tiền sau giảm", "Trạng thái"
    };

    @Override
    public byte[] exportHoaDonList(String keyword, Long minAmount, Long maxAmount,
                                    Timestamp startDate, Timestamp endDate, Short trangThai, String loaiDon) {
        // Dùng lại đúng câu query đang phục vụ trang danh sách hóa đơn admin
        // (HoaDonRepository.getAllHoaDon) để file Excel luôn khớp với bộ lọc/tìm kiếm
        // admin đang xem trên màn hình - chỉ khác là lấy hết một lần, không phân trang.
        Pageable allRows = PageRequest.of(0, 100_000, Sort.by(Sort.Direction.DESC, "id"));
        Page<HoaDonDTOResponse> page = hoaDonRepository.getAllHoaDon(
                keyword, minAmount, maxAmount, startDate, endDate, trangThai, false, loaiDon, allRows);
        List<HoaDonDTOResponse> rows = page.getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sach don hang");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            int rowIndex = 1;
            for (HoaDonDTOResponse hd : rows) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(rowIndex);
                row.createCell(1).setCellValue(nullToEmpty(hd.getMa()));
                row.createCell(2).setCellValue(nullToEmpty(hd.getMaNhanVien()));
                row.createCell(3).setCellValue(nullToEmpty(hd.getTenKhachHang()));
                row.createCell(4).setCellValue(nullToEmpty(hd.getSoDienThoaiKhachHang()));
                row.createCell(5).setCellValue(nullToEmpty(hd.getLoaiDon()));
                row.createCell(6).setCellValue(hd.getNgayTao() != null ? dateFormat.format(hd.getNgayTao()) : "");

                Cell shippingCell = row.createCell(7);
                shippingCell.setCellValue(hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen().doubleValue() : 0d);
                shippingCell.setCellStyle(currencyStyle);

                Cell totalCell = row.createCell(8);
                totalCell.setCellValue(hd.getTongTienSauGiam() != null ? hd.getTongTienSauGiam().doubleValue() : 0d);
                totalCell.setCellStyle(currencyStyle);

                row.createCell(9).setCellValue(HoaDonStatus.toLabel(hd.getTrangThai()));
                rowIndex++;
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Lỗi khi tạo file Excel danh sách đơn hàng", e);
        }
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
