package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.DanhMuc;
import com.example.sd_24_striker_be.service.product.DanhMucService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/danh-muc")
public class DanhMucController extends ThuocTinhController<DanhMuc> {

    public DanhMucController(DanhMucService service) {
        super(service);
    }
}