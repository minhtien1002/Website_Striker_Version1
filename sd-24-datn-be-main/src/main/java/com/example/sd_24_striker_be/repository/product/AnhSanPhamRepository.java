package com.example.sd_24_striker_be.repository.product;

import com.example.sd_24_striker_be.modal.product.AnhSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnhSanPhamRepository extends JpaRepository<AnhSanPham, Integer> {
}
