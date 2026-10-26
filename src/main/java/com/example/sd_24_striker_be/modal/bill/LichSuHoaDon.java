package com.example.sd_24_striker_be.modal.bill;

import com.example.sd_24_striker_be.modal.account.NhanVien;
import com.example.sd_24_striker_be.common.TimestampOffsetConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id")
    private NhanVien idNhanVien;

    @Nationalized
    @Column(name = "ma")
    private String ma;

    @Nationalized
    @Column(name = "hanh_dong")
    private String hanhDong;

    // Trước đây là java.sql.Date - luôn cắt giờ về 00:00 dù cột DB (datetimeoffset) lưu
    // được đầy đủ giờ:phút:giây, khiến lịch sử đơn hàng hiện đúng ngày nhưng sai giờ.
    @Column(name = "thoi_gian")
    @Convert(converter = TimestampOffsetConverter.class)
    private Timestamp thoiGian;

    @Column(name = "deleted")
    private Short deleted;

}