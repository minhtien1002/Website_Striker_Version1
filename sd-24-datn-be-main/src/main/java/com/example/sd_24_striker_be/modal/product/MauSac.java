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
@Table(name = "mau_sac")
public class MauSac implements ThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "ten_mau_sac", nullable = false, length = 100)
    private String tenMauSac;

    @Nationalized
    @Column(name = "ma_mau_sac")
    private String maMauSac;

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
        return tenMauSac;
    }

    @Override
    public void setTen(String ten) {
        this.tenMauSac = ten;
    }

    @Override
    @JsonIgnore
    public String getMa() {
        return maMauSac;
    }

    @Override
    public void setMa(String ma) {
        this.maMauSac = ma;
    }
}