package com.example.sd_24_striker_be.dto.account.request.KhachHang;

/**
 * Body dùng chung cho các thao tác khách hàng thực hiện trên đơn hàng của chính mình
 * (hủy đơn, yêu cầu hoàn hàng) - hiện chỉ cần lý do.
 */
public class OrderActionRequest {
    private String lyDo;

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
}
