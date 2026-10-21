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
@Builder
@ToString
@Table(name = "danh_muc")
public class DanhMuc implements ThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String tenDanhMuc;

    @Nationalized
    @Column(name = "ma_danh_muc")
    private String maDanhMuc;

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

    /** Constructors are explicit because the IDE's incremental Lombok compiler once
     * generated a Java-25-only all-args constructor into the Java-17 target classes. */
    public DanhMuc() {
    }

    public DanhMuc(Integer id, String tenDanhMuc, String maDanhMuc, String moTa,
                   Instant ngayTao, Instant ngayCapNhat, Boolean deleted) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.maDanhMuc = maDanhMuc;
        this.moTa = moTa;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
        this.deleted = deleted;
    }

    @Override
    @JsonIgnore
    public String getTen() {
        return tenDanhMuc;
    }

    @Override
    public void setTen(String ten) {
        this.tenDanhMuc = ten;
    }

    @Override
    @JsonIgnore
    public String getMa() {
        return maDanhMuc;
    }

    @Override
    public void setMa(String ma) {
        this.maDanhMuc = ma;
    }
}
