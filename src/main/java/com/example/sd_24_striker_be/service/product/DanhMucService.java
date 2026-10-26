package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.DanhMuc;
import com.example.sd_24_striker_be.repository.product.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;

    public Page<DanhMuc> getAllWithPagination(String keyword, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return danhMucRepository.findAllWithKeyword(keyword, pageable);
    }

    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll();
    }

    public Optional<DanhMuc> findById(Integer id) {
        return danhMucRepository.findById(id);
    }

    public DanhMuc save(DanhMuc danhMuc) {
        if (danhMuc.getId() == null) {
            danhMuc.setNgayTao(Instant.now());
            danhMuc.setDeleted(false);
        }
        danhMuc.setNgayCapNhat(Instant.now());
        return danhMucRepository.save(danhMuc);
    }

    public DanhMuc update(Integer id, DanhMuc danhMuc) {
        Optional<DanhMuc> existingDanhMuc = danhMucRepository.findById(id);
        if (existingDanhMuc.isPresent()) {
            DanhMuc updated = existingDanhMuc.get();
            updated.setTenDanhMuc(danhMuc.getTenDanhMuc());
            updated.setNgayCapNhat(Instant.now());
            return danhMucRepository.save(updated);
        }
        return null;
    }

    public DanhMuc toggleStatus(Integer id) {
        Optional<DanhMuc> existingDanhMuc = danhMucRepository.findById(id);
        if (existingDanhMuc.isPresent()) {
            DanhMuc danhMuc = existingDanhMuc.get();
            danhMuc.setDeleted(!danhMuc.getDeleted());
            danhMuc.setNgayCapNhat(Instant.now());
            return danhMucRepository.save(danhMuc);
        }
        return null;
    }

    /**
     * Xóa mềm danh mục (đặt deleted=true) thay vì xóa hẳn khỏi DB, tránh vỡ khóa
     * ngoại với các sản phẩm đang tham chiếu tới danh mục này.
     */
    public boolean delete(Integer id) {
        Optional<DanhMuc> existing = danhMucRepository.findById(id);
        if (existing.isPresent()) {
            DanhMuc danhMuc = existing.get();
            danhMuc.setDeleted(true);
            danhMuc.setNgayCapNhat(Instant.now());
            danhMucRepository.save(danhMuc);
            return true;
        }
        return false;
    }

    public boolean checkNameExists(String tenDanhMuc, Integer excludeId) {
        if (excludeId != null) {
            return danhMucRepository.existsByTenDanhMucAndIdNot(tenDanhMuc, excludeId);
        }
        return danhMucRepository.existsByTenDanhMuc(tenDanhMuc);
    }
}
