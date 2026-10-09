package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.MauSac;
import com.example.sd_24_striker_be.service.product.MauSacService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mau-sac")
public class MauSacController extends ThuocTinhController<MauSac> {

    public MauSacController(MauSacService service) {
        super(service);
    }
}