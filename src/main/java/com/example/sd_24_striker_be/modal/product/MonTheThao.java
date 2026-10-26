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
@Table(name = "mon_the_thao")
public class MonTheThao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Nationalized
    @Column(name = "ten_mon_the_thao")
    private String tenMonTheThao;

    @Nationalized
    @Column(name = "ma_mon_the_thao")
    private String maMonTheThao;

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
