package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.ThuongHieu;
import com.example.sd_24_striker_be.repository.product.ThuongHieuRepository;
import org.springframework.stereotype.Service;

@Service
public class ThuongHieuService extends ThuocTinhService<ThuongHieu> {

    public ThuongHieuService(ThuongHieuRepository repository) {
        super(repository);
    }

    @Override
    public String tenLoai() {
        return "thÆ°Æ¡ng hiá»‡u";
    }

    @Override
    protected String tienToMa() {
        return "TH";
    }

    @Override
    protected int doDaiTenToiDa() {
        return 100;
    }

    @Override
    protected ThuongHieu taoMoi() {
        return new ThuongHieu();
    }
}