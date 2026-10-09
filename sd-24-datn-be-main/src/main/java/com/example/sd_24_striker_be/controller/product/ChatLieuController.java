package com.example.sd_24_striker_be.controller.product;

import com.example.sd_24_striker_be.modal.product.ChatLieu;
import com.example.sd_24_striker_be.service.product.ChatLieuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-lieu")
public class ChatLieuController extends ThuocTinhController<ChatLieu> {

    public ChatLieuController(ChatLieuService service) {
        super(service);
    }
}