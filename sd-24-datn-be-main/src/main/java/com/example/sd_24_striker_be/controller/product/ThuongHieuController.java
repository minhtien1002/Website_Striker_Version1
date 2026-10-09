package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.ThuongHieu;
import com.example.sd_24_striker_be.service.product.ThuongHieuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thuong-hieu")
public class ThuongHieuController extends ThuocTinhController<ThuongHieu> {

    public ThuongHieuController(ThuongHieuService service) {
        super(service);
    }
}