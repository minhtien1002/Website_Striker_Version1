package com.example.sd_24_striker_be.modal.product;

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
@Table(name = "cong_nghe_vai")
public class CongNgheVai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Nationalized
    @Column(name = "ten_cong_nghe")
    private String tenCongNghe;

    @Nationalized
    @Column(name = "ma_cong_nghe")
    private String maCongNghe;

    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;
}
