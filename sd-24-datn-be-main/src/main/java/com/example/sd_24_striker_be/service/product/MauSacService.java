package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.MauSac;
import com.example.sd_24_striker_be.repository.product.MauSacRepository;
import org.springframework.stereotype.Service;

@Service
public class MauSacService extends ThuocTinhService<MauSac> {

    public MauSacService(MauSacRepository repository) {
        super(repository);
    }

    @Override
    public String tenLoai() {
        return "mÃ u sáº¯c";
    }

    @Override
    protected String tienToMa() {
        return "MS";
    }

    @Override
    protected int doDaiTenToiDa() {
        return 100;
    }

    @Override
    protected MauSac taoMoi() {
        return new MauSac();
    }
}