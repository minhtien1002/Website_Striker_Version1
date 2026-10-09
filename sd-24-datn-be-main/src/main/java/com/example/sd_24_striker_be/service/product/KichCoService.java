package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.KichCo;
import com.example.sd_24_striker_be.repository.product.KichCoRepository;
import org.springframework.stereotype.Service;

@Service
public class KichCoService extends ThuocTinhService<KichCo> {

    public KichCoService(KichCoRepository repository) {
        super(repository);
    }

    @Override
    public String tenLoai() {
        return "kÃ­ch cá»¡";
    }

    @Override
    protected String tienToMa() {
        return "KC";
    }

    @Override
    protected int doDaiTenToiDa() {
        return 50;
    }

    @Override
    protected KichCo taoMoi() {
        return new KichCo();
    }
}