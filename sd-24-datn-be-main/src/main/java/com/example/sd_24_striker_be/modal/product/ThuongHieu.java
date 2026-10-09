package com.example.sd_24_striker_be.modal.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "thuong_hieu")
public class ThuongHieu implements ThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "ten_thuong_hieu", nullable = false, length = 100)
    private String tenThuongHieu;

    @Nationalized
    @Column(name = "ma_thuong_hieu")
    private String maThuongHieu;

    /** ChÃº thÃ­ch. */
    @Nationalized
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    /** false = Hoáº¡t Ä‘á»™ng, true = Ngá»«ng hoáº¡t Ä‘á»™ng. */
    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @Override
    @JsonIgnore
    public String getTen() {
        return tenThuongHieu;
    }

    @Override
    public void setTen(String ten) {
        this.tenThuongHieu = ten;
    }

    @Override
    @JsonIgnore
    public String getMa() {
        return maThuongHieu;
    }

    @Override
    public void setMa(String ma) {
        this.maThuongHieu = ma;
    }
}