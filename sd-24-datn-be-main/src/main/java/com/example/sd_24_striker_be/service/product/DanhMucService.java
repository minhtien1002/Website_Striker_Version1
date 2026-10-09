package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.DanhMuc;
import com.example.sd_24_striker_be.repository.product.DanhMucRepository;
import org.springframework.stereotype.Service;

@Service
public class DanhMucService extends ThuocTinhService<DanhMuc> {

    public DanhMucService(DanhMucRepository repository) {
        super(repository);
    }

    @Override
    public String tenLoai() {
        return "thá»ƒ loáº¡i";
    }

    @Override
    protected String tienToMa() {
        return "TL";
    }

    @Override
    protected int doDaiTenToiDa() {
        return 100;
    }

    @Override
    protected DanhMuc taoMoi() {
        return new DanhMuc();
    }
}