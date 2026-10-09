package com.example.sd_24_striker_be.common.bill;

import com.example.sd_24_striker_be.dto.bill.response.HoaDonDetailResponse;
import com.example.sd_24_striker_be.modal.bill.HoaDonChiTiet;
import com.example.sd_24_striker_be.modal.bill.LichSuHoaDon;
import com.example.sd_24_striker_be.modal.sell.HinhThucThanhToan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface HoaDonDetailMapper {
    HoaDonDetailMapper INSTANCE = Mappers.getMapper(HoaDonDetailMapper.class);
    
    // Updated after entity relationship changes

    @Mapping(source = "id", target = "hoaDonChiTietId")
    @Mapping(source = "idChiTietSp.id", target = "chiTietSanPhamId")
    @Mapping(source = "idHoaDon.id", target = "idHoaDon")
    @Mapping(source = "idChiTietSp.ma", target = "maChiTietSanPham")
    @Mapping(source = "idChiTietSp.idSanPham.id", target = "idSanPham")
    @Mapping(source = "idChiTietSp.idSanPham.ma",
            target = "maSanPham")
    @Mapping(source = "idChiTietSp.idSanPham.tenSanPham", target = "tenSanPham")
    @Mapping(source = "gia", target = "giaBan")
    @Mapping(source = "ghiChu", target = "ghiChu")
    @Mapping(source = "idChiTietSp.idMauSac.tenMauSac", target = "mauSac")
    @Mapping(source = "idChiTietSp.idKichCo.tenKichCo", target = "kichCo")
    @Mapping(source = "idChiTietSp.idSanPham.idChatLieu.tenChatLieu", target = "chatLieu")
    @Mapping(source = "soLuong", target = "soLuong")
    @Mapping(source = "idChiTietSp.moTaChiTiet", target = "moTaChiTiet")
    @Mapping(source = "idChiTietSp.idSanPham.idThuongHieu.tenThuongHieu", target = "thuongHieu")
    @Mapping(source = "idChiTietSp.idSanPham.idDanhMuc.tenDanhMuc", target = "theLoai")
    // Ảnh riêng của biến thể nếu có, không thì ảnh đại diện của sản phẩm
    @Mapping(target = "duongDan", expression = "java(hoaDonChiTiet.getIdChiTietSp() == null ? null : "
            + "hoaDonChiTiet.getIdChiTietSp().getIdAnhSanPham() != null ? hoaDonChiTiet.getIdChiTietSp().getIdAnhSanPham().getUrlAnh() : "
            + "hoaDonChiTiet.getIdChiTietSp().getIdSanPham() != null && hoaDonChiTiet.getIdChiTietSp().getIdSanPham().getIdAnhSanPham() != null "
            + "? hoaDonChiTiet.getIdChiTietSp().getIdSanPham().getIdAnhSanPham().getUrlAnh() : null)")
    HoaDonDetailResponse.SanPhamChiTietInfo mapToSanPhamChiTietInfo(HoaDonChiTiet hoaDonChiTiet);

    @Mapping(source = "idPhuongThucThanhToan.ma", target = "maHinhThucThanhToan")
    @Mapping(source = "idPhuongThucThanhToan.kieuThanhToan", target = "kieuThanhToan")
    @Mapping(source = "tienChuyenKhoan", target = "tienChuyenKhoan")
    @Mapping(source = "tienMat", target = "tienMat")
    HoaDonDetailResponse.ThanhToanInfo mapToThanhToanInfo(HinhThucThanhToan hinhThucThanhToan);

    @Mapping(source = "ma", target = "ma")
    @Mapping(source = "hanhDong", target = "hanhDong")
    @Mapping(source = "thoiGian", target = "thoiGian")
    @Mapping(source = "idNhanVien.tenNhanVien", target = "tenNhanVien")
    @Mapping(source = "idNhanVien.ma", target = "maNhanVien")
    @Mapping(source = "idHoaDon.id", target = "idHoaDon")
    // Trạng thái của đơn NGAY SAU thao tác này (cột deleted của lịch sử), không phải trạng thái hiện tại
    @Mapping(source = "deleted", target = "trangThai")
    @Mapping(source = "ghiChu", target = "ghiChu")
    HoaDonDetailResponse.LichSuHoaDonInfo mapToLichSuHoaDonInfo(LichSuHoaDon lichSuHoaDon);
}
