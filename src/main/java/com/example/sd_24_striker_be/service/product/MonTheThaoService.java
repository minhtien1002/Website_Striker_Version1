package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.MonTheThao;
import com.example.sd_24_striker_be.repository.product.MonTheThaoRepository;
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
public class MonTheThaoService {

    @Autowired
    private MonTheThaoRepository monTheThaoRepository;

    public Page<MonTheThao> getAllWithPagination(String keyword, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return monTheThaoRepository.findAllWithKeyword(keyword, pageable);
    }

    public List<MonTheThao> getAll() {
        return monTheThaoRepository.findAll();
    }

    public Optional<MonTheThao> findById(Integer id) {
        return monTheThaoRepository.findById(id);
    }

    public MonTheThao save(MonTheThao monTheThao) {
        if (monTheThao.getId() == null) {
            monTheThao.setNgayTao(Instant.now());
            monTheThao.setDeleted(false);
        }
        monTheThao.setNgayCapNhat(Instant.now());
        return monTheThaoRepository.save(monTheThao);
    }

    public MonTheThao update(Integer id, MonTheThao monTheThao) {
        Optional<MonTheThao> existing = monTheThaoRepository.findById(id);
        if (existing.isPresent()) {
            MonTheThao updated = existing.get();
            updated.setTenMonTheThao(monTheThao.getTenMonTheThao());
            updated.setMaMonTheThao(monTheThao.getMaMonTheThao());
            updated.setNgayCapNhat(Instant.now());
            return monTheThaoRepository.save(updated);
        }
        return null;
    }

    public MonTheThao toggleStatus(Integer id) {
        Optional<MonTheThao> existing = monTheThaoRepository.findById(id);
        if (existing.isPresent()) {
            MonTheThao monTheThao = existing.get();
            monTheThao.setDeleted(!monTheThao.getDeleted());
            monTheThao.setNgayCapNhat(Instant.now());
            return monTheThaoRepository.save(monTheThao);
        }
        return null;
    }

    public boolean delete(Integer id) {
        Optional<MonTheThao> existing = monTheThaoRepository.findById(id);
        if (existing.isPresent()) {
            MonTheThao monTheThao = existing.get();
            monTheThao.setDeleted(true);
            monTheThao.setNgayCapNhat(Instant.now());
            monTheThaoRepository.save(monTheThao);
            return true;
        }
        return false;
    }

    public boolean checkNameExists(String tenMonTheThao, Integer excludeId) {
        if (excludeId != null) {
            return monTheThaoRepository.existsByTenMonTheThaoAndIdNot(tenMonTheThao, excludeId);
        }
        return monTheThaoRepository.existsByTenMonTheThao(tenMonTheThao);
    }
}
