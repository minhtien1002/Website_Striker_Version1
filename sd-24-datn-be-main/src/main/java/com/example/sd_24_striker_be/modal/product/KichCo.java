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
@Table(name = "kich_co")
public class KichCo implements ThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "ten_kich_co", nullable = false, length = 50)
    private String tenKichCo;

    @Nationalized
    @Column(name = "ma_kich_co")
    private String maKichCo;

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
        return tenKichCo;
    }

    @Override
    public void setTen(String ten) {
        this.tenKichCo = ten;
    }

    @Override
    @JsonIgnore
    public String getMa() {
        return maKichCo;
    }

    @Override
    public void setMa(String ma) {
        this.maKichCo = ma;
    }
}