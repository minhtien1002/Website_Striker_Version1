package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.KichCo;
import com.example.sd_24_striker_be.service.product.KichCoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kich-co")
public class KichCoController extends ThuocTinhController<KichCo> {

    public KichCoController(KichCoService service) {
        super(service);
    }
}