package com.example.sd_24_striker_be.service.product;

import com.example.sd_24_striker_be.modal.product.ChatLieu;
import com.example.sd_24_striker_be.repository.product.ChatLieuRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatLieuService extends ThuocTinhService<ChatLieu> {

    public ChatLieuService(ChatLieuRepository repository) {
        super(repository);
    }

    @Override
    public String tenLoai() {
        return "cháº¥t liá»‡u";
    }

    @Override
    protected String tienToMa() {
        return "CL";
    }

    @Override
    protected int doDaiTenToiDa() {
        return 100;
    }

    @Override
    protected ChatLieu taoMoi() {
        return new ChatLieu();
    }
}