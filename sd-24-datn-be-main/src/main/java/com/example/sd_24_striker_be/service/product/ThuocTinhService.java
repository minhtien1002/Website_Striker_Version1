package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.exception.BusinessException;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.product.ThuocTinh;
import com.example.sd_24_striker_be.repository.product.ThuocTinhRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Nghiệp vụ chung cho mọi thuộc tính sản phẩm:
 * - Chỉ quản lý bằng trạng thái (Hoạt động / Ngừng hoạt động), không có xóa.
 * - Tên bắt buộc, được chuẩn hóa khoảng trắng, không trùng (không phân biệt hoa thường).
 * - Mã tự sinh theo tiền tố + id (VD TH001), người dùng không phải nhập.
 */
public abstract class ThuocTinhService<T extends ThuocTinh> {

    private static final int MAX_MO_TA = 500;

    protected final ThuocTinhRepository<T> repository;

    protected ThuocTinhService(ThuocTinhRepository<T> repository) {
        this.repository = repository;
    }

    /** Tên hiển thị trong thông báo, VD "thương hiệu". */
    public abstract String tenLoai();

    /** Tiền tố mã, VD "TH". */
    protected abstract String tienToMa();

    protected abstract int doDaiTenToiDa();

    protected abstract T taoMoi();

    public Page<T> search(String keyword, String trangThai, int page, int size) {
        Boolean deleted = switch (trangThai == null ? "" : trangThai) {
            case "active" -> false;
            case "inactive" -> true;
            default -> null;
        };
        String kw = keyword == null ? "" : keyword.trim().replaceAll("\\s+", " ");
        return repository.search(kw, deleted,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 1000), Sort.by("id").descending()));
    }

    public List<T> getAll() {
        return repository.findAll(Sort.by("id").descending());
    }

    public List<T> getActive() {
        return repository.findByDeletedFalseOrderByIdDesc();
    }

    public T getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy " + tenLoai()));
    }

    @Transactional
    public T create(String ten, String moTa) {
        String tenChuan = validateTen(ten, null);
        T entity = taoMoi();
        entity.setTen(tenChuan);
        entity.setMoTa(chuanHoaMoTa(moTa));
        entity.setDeleted(false);
        entity.setNgayTao(Instant.now());
        entity.setNgayCapNhat(Instant.now());
        entity = repository.saveAndFlush(entity);
        entity.setMa(tienToMa() + String.format("%03d", entity.getId()));
        return repository.save(entity);
    }

    @Transactional
    public T update(Integer id, String ten, String moTa) {
        T entity = getById(id);
        entity.setTen(validateTen(ten, id));
        entity.setMoTa(chuanHoaMoTa(moTa));
        entity.setNgayCapNhat(Instant.now());
        return repository.save(entity);
    }

    @Transactional
    public T toggleStatus(Integer id) {
        T entity = getById(id);
        entity.setDeleted(!Boolean.TRUE.equals(entity.getDeleted()));
        entity.setNgayCapNhat(Instant.now());
        return repository.save(entity);
    }

    public long countSanPhamSuDung(Integer id) {
        getById(id);
        return repository.countSanPhamSuDung(id);
    }

    public boolean isTenTrung(String ten, Integer excludeId) {
        if (ten == null || ten.isBlank()) return false;
        return repository.countTrungTen(ten.trim().replaceAll("\\s+", " "), excludeId) > 0;
    }

    private String validateTen(String ten, Integer excludeId) {
        if (ten == null || ten.trim().isEmpty()) {
            throw new BusinessException("Tên " + tenLoai() + " không được để trống");
        }
        String tenChuan = ten.trim().replaceAll("\\s+", " ");
        if (tenChuan.length() > doDaiTenToiDa()) {
            throw new BusinessException("Tên " + tenLoai() + " tối đa " + doDaiTenToiDa() + " ký tự");
        }
        if (!tenChuan.matches("[\\p{L}\\p{N} .,&()'/+%-]+")) {
            throw new BusinessException("Tên " + tenLoai() + " chứa ký tự không hợp lệ");
        }
        if (repository.countTrungTen(tenChuan, excludeId) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Tên " + tenLoai() + " \"" + tenChuan + "\" đã tồn tại (không phân biệt chữ hoa, chữ thường)");
        }
        return tenChuan;
    }

    private String chuanHoaMoTa(String moTa) {
        if (moTa == null || moTa.trim().isEmpty()) return null;
        String s = moTa.trim();
        if (s.length() > MAX_MO_TA) {
            throw new BusinessException("Chú thích tối đa " + MAX_MO_TA + " ký tự");
        }
        return s;
    }
}
