package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.CongNgheVai;
import com.example.sd_24_striker_be.repository.product.CongNgheVaiRepository;
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
public class CongNgheVaiService {

    @Autowired
    private CongNgheVaiRepository congNgheVaiRepository;

    public Page<CongNgheVai> getAllWithPagination(String keyword, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return congNgheVaiRepository.findAllWithKeyword(keyword, pageable);
    }

    public List<CongNgheVai> getAll() {
        return congNgheVaiRepository.findAll();
    }

    public Optional<CongNgheVai> findById(Integer id) {
        return congNgheVaiRepository.findById(id);
    }

    public CongNgheVai save(CongNgheVai congNgheVai) {
        if (congNgheVai.getId() == null) {
            congNgheVai.setNgayTao(Instant.now());
            congNgheVai.setDeleted(false);
        }
        congNgheVai.setNgayCapNhat(Instant.now());
        return congNgheVaiRepository.save(congNgheVai);
    }

    public CongNgheVai update(Integer id, CongNgheVai congNgheVai) {
        Optional<CongNgheVai> existing = congNgheVaiRepository.findById(id);
        if (existing.isPresent()) {
            CongNgheVai updated = existing.get();
            updated.setTenCongNghe(congNgheVai.getTenCongNghe());
            updated.setMaCongNghe(congNgheVai.getMaCongNghe());
            updated.setMoTa(congNgheVai.getMoTa());
            updated.setNgayCapNhat(Instant.now());
            return congNgheVaiRepository.save(updated);
        }
        return null;
    }

    public CongNgheVai toggleStatus(Integer id) {
        Optional<CongNgheVai> existing = congNgheVaiRepository.findById(id);
        if (existing.isPresent()) {
            CongNgheVai congNgheVai = existing.get();
            congNgheVai.setDeleted(!congNgheVai.getDeleted());
            congNgheVai.setNgayCapNhat(Instant.now());
            return congNgheVaiRepository.save(congNgheVai);
        }
        return null;
    }

    public boolean delete(Integer id) {
        Optional<CongNgheVai> existing = congNgheVaiRepository.findById(id);
        if (existing.isPresent()) {
            CongNgheVai congNgheVai = existing.get();
            congNgheVai.setDeleted(true);
            congNgheVai.setNgayCapNhat(Instant.now());
            congNgheVaiRepository.save(congNgheVai);
            return true;
        }
        return false;
    }

    public boolean checkNameExists(String tenCongNghe, Integer excludeId) {
        if (excludeId != null) {
            return congNgheVaiRepository.existsByTenCongNgheAndIdNot(tenCongNghe, excludeId);
        }
        return congNgheVaiRepository.existsByTenCongNghe(tenCongNghe);
    }
}
