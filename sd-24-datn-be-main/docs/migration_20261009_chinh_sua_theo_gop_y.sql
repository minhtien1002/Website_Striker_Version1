/*
  Migration 2026-10-09 – chỉnh sửa theo góp ý
  Backup trước khi chạy: MSSQL\Backup\Database_STRIKER_Final_truoc_sua_20261009.bak
  Các bước đều có kiểm tra tồn tại nên chạy lại nhiều lần không lỗi.

  Quy ước trạng thái chung (mọi bảng thuộc tính / sản phẩm): cột deleted
    0 = Hoạt động, 1 = Ngừng hoạt động. Không xóa cứng bản ghi.
*/
USE Database_STRIKER_Final;
GO

/* 1. Cột chú thích (mo_ta) cho các bảng thuộc tính */
IF COL_LENGTH('thuong_hieu','mo_ta') IS NULL ALTER TABLE thuong_hieu ADD mo_ta NVARCHAR(500) NULL;
IF COL_LENGTH('danh_muc','mo_ta')    IS NULL ALTER TABLE danh_muc    ADD mo_ta NVARCHAR(500) NULL;
IF COL_LENGTH('chat_lieu','mo_ta')   IS NULL ALTER TABLE chat_lieu   ADD mo_ta NVARCHAR(500) NULL;
IF COL_LENGTH('kich_co','mo_ta')     IS NULL ALTER TABLE kich_co     ADD mo_ta NVARCHAR(500) NULL;
IF COL_LENGTH('mau_sac','mo_ta')     IS NULL ALTER TABLE mau_sac     ADD mo_ta NVARCHAR(500) NULL;
GO

/* 2. Gộp công nghệ vải vào chất liệu */
IF OBJECT_ID('cong_nghe_vai') IS NOT NULL
BEGIN
    INSERT INTO chat_lieu (ten_chat_lieu, ma_chat_lieu, mo_ta, ngay_tao, ngay_cap_nhat, deleted)
    SELECT c.ten_cong_nghe, NULL, c.mo_ta, GETDATE(), GETDATE(), c.deleted
    FROM cong_nghe_vai c
    WHERE NOT EXISTS (SELECT 1 FROM chat_lieu l WHERE l.ten_chat_lieu = LTRIM(RTRIM(c.ten_cong_nghe)));

    UPDATE chat_lieu SET ma_chat_lieu = 'CL' + RIGHT('000' + CAST(id AS VARCHAR(10)), 3) WHERE ma_chat_lieu IS NULL;

    IF OBJECT_ID('FK_SanPham_CongNgheVai') IS NOT NULL ALTER TABLE san_pham DROP CONSTRAINT FK_SanPham_CongNgheVai;
    IF COL_LENGTH('san_pham','id_cong_nghe_vai') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_cong_nghe_vai;
    DROP TABLE cong_nghe_vai;
END
GO

/* 3. Bỏ thuộc tính môn thể thao */
IF OBJECT_ID('FK_SanPham_MonTheThao') IS NOT NULL ALTER TABLE san_pham DROP CONSTRAINT FK_SanPham_MonTheThao;
IF COL_LENGTH('san_pham','id_mon_the_thao') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_mon_the_thao;
IF OBJECT_ID('mon_the_thao') IS NOT NULL DROP TABLE mon_the_thao;
GO

/* 4. Bỏ quốc gia sản xuất, giá nhập, mã màu hex */
IF COL_LENGTH('san_pham','quoc_gia_san_xuat') IS NOT NULL ALTER TABLE san_pham DROP COLUMN quoc_gia_san_xuat;
IF COL_LENGTH('chi_tiet_san_pham','gia_nhap') IS NOT NULL ALTER TABLE chi_tiet_san_pham DROP COLUMN gia_nhap;
IF COL_LENGTH('mau_sac','hex') IS NOT NULL ALTER TABLE mau_sac DROP COLUMN hex;
GO

/* 5. Số lượng đã bán của từng chi tiết sản phẩm */
IF COL_LENGTH('chi_tiet_san_pham','so_luong_da_ban') IS NULL
    ALTER TABLE chi_tiet_san_pham ADD so_luong_da_ban INT NOT NULL CONSTRAINT DF_ctsp_so_luong_da_ban DEFAULT 0;
GO
-- Đã bán = hàng đã xuất kho (2 đã xác nhận, 3 chờ vận chuyển, 4 đang vận chuyển, 5 hoàn thành, 7 yêu cầu hoàn)
UPDATE ct SET so_luong_da_ban = ISNULL(x.sl, 0)
FROM chi_tiet_san_pham ct
LEFT JOIN (
    SELECT c.id_chi_tiet_sp, SUM(c.so_luong) sl
    FROM hoa_don_chi_tiet c JOIN hoa_don h ON h.id = c.id_hoa_don
    WHERE h.trang_thai IN (2, 3, 4, 5, 7) AND c.deleted = 0
    GROUP BY c.id_chi_tiet_sp
) x ON x.id_chi_tiet_sp = ct.id;
GO

/* 6. Lý do (giao thất bại / hủy / hoàn kho) cho lịch sử hóa đơn */
IF COL_LENGTH('lich_su_hoa_don','ghi_chu') IS NULL ALTER TABLE lich_su_hoa_don ADD ghi_chu NVARCHAR(500) NULL;
GO

/* 7. Không cho trùng tên thuộc tính (collation CI nên "adidas" = "Adidas", "m" = "M") */
UPDATE thuong_hieu SET ten_thuong_hieu = LTRIM(RTRIM(ten_thuong_hieu));
UPDATE danh_muc    SET ten_danh_muc    = LTRIM(RTRIM(ten_danh_muc));
UPDATE chat_lieu   SET ten_chat_lieu   = LTRIM(RTRIM(ten_chat_lieu));
UPDATE kich_co     SET ten_kich_co     = LTRIM(RTRIM(ten_kich_co));
UPDATE mau_sac     SET ten_mau_sac     = LTRIM(RTRIM(ten_mau_sac));
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_thuong_hieu_ten') CREATE UNIQUE INDEX UX_thuong_hieu_ten ON thuong_hieu(ten_thuong_hieu);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_danh_muc_ten')    CREATE UNIQUE INDEX UX_danh_muc_ten    ON danh_muc(ten_danh_muc);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_chat_lieu_ten')   CREATE UNIQUE INDEX UX_chat_lieu_ten   ON chat_lieu(ten_chat_lieu);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_kich_co_ten')     CREATE UNIQUE INDEX UX_kich_co_ten     ON kich_co(ten_kich_co);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_mau_sac_ten')     CREATE UNIQUE INDEX UX_mau_sac_ten     ON mau_sac(ten_mau_sac);
GO

-- 8. Sửa ảnh sản phẩm: các file trong thư mục images/ao-the-thao không tồn tại.
--    Trỏ sang ảnh thật đã chép vào uploads/images (backend phục vụ qua WebConfig).
--    Ảnh được ghép theo loại áo, cần thay ảnh đúng sản phẩm qua màn sửa sản phẩm.
DECLARE @base NVARCHAR(100) = N'http://localhost:8080/uploads/images/';
UPDATE anh_san_pham SET url_anh = @base + CASE
    WHEN url_anh LIKE '%nike-academy23-back%'   THEN N'3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg'
    WHEN url_anh LIKE '%nike-academy23%'        THEN N'11_Ao_Thun_Bong_Da_STRIKER_DriFIT.jpg'
    WHEN url_anh LIKE '%adidas-tiro24-detail%'  THEN N'ao-da-bong-anh.jpg'
    WHEN url_anh LIKE '%adidas-tiro24%'         THEN N'22_Ao_Thun_Nam_Adidas_Climacool_BongDa.jpg'
    WHEN url_anh LIKE '%kamito-aurora%'         THEN N'ao-da-bong-anh.jpg'
    WHEN url_anh LIKE '%jogarbola%'             THEN N'ao-1.jpg'
    WHEN url_anh LIKE '%grandsport-league%'     THEN N'avt-logo-ao-bong-da-lop.jpg'
    WHEN url_anh LIKE '%thu-mon%'               THEN N'4-xanh-coban-ld9248-800x800.jpg'
    WHEN url_anh LIKE '%mizuno-pro-jersey%'     THEN N'3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg'
    WHEN url_anh LIKE '%bong-ro-nike%'          THEN N'8_Ao_Thun_Bong_Ro_STRIKER_DriFIT.jpg'
    WHEN url_anh LIKE '%bong-ro-adidas%'        THEN N'19_Ao_Ba_Lo_Gym_Puma_QuickDry.jpg'
    WHEN url_anh LIKE '%bong-ro-kappa%'         THEN N'7_Ao_Ba_Lo_Tap_Gym_UnderArmour_HeatGear.jpg'
    WHEN url_anh LIKE '%cau-long-yonex%'        THEN N'15_Ao_Polo_Tennis_Puma_Climacool.jpg'
    WHEN url_anh LIKE '%cau-long-lining%'       THEN N'21_Ao_Polo_STRIKER_San_Tennis.jpg'
    WHEN url_anh LIKE '%cau-long-kamito%'       THEN N'3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg'
    WHEN url_anh LIKE '%chay-bo-nike%'          THEN N'13_Ao_Khoac_Gio_Nike_Windrunner.jpg'
    WHEN url_anh LIKE '%chay-bo-ua%'            THEN N'16_Ao_Thun_Yoga_Nu_STRIKER_Cotton.jpg'
    WHEN url_anh LIKE '%chay-bo-adidas%'        THEN N'18_Ao_Khoac_Gio_Adidas_AeroReady_ChayBo.jpg'
    WHEN url_anh LIKE '%tennis-nike%'           THEN N'4_Ao_Polo_Tennis_Nike_Climacool.jpg'
    WHEN url_anh LIKE '%tennis-adidas%'         THEN N'15_Ao_Polo_Tennis_Puma_Climacool.jpg'
    WHEN url_anh LIKE '%bong-chuyen%'           THEN N'ao-1.jpg'
    WHEN url_anh LIKE '%bong-ban%'              THEN N'21_Ao_Polo_STRIKER_San_Tennis.jpg'
    WHEN url_anh LIKE '%gym-ua%'                THEN N'7_Ao_Ba_Lo_Tap_Gym_UnderArmour_HeatGear.jpg'
    WHEN url_anh LIKE '%gym-nike%'              THEN N'19_Ao_Ba_Lo_Gym_Puma_QuickDry.jpg'
    WHEN url_anh LIKE '%gym-puma%'              THEN N'3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg'
    WHEN url_anh LIKE '%boi-%'                  THEN N'16_Ao_Thun_Yoga_Nu_STRIKER_Cotton.jpg'
    WHEN url_anh LIKE '%dap-xe%'                THEN N'6_Ao_Khoac_Gio_Puma_QuickDry.jpg'
    WHEN url_anh LIKE '%polo%'                  THEN N'10_Ao_Polo_Golf_Nike_Climacool.jpg'
    ELSE N'3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg' END
WHERE url_anh LIKE '/images/ao-the-thao/%';
GO

-- 9. Nhân viên / khách hàng: code quy ước deleted = 1 là ĐANG HOẠT ĐỘNG (ngược các bảng khác).
--    Dữ liệu mẫu để 0 khiến mọi nhân viên bị coi là ngừng hoạt động, không đăng nhập được.
UPDATE nhan_vien SET deleted = 1 WHERE deleted = 0 OR deleted IS NULL;
UPDATE khach_hang SET deleted = 1 WHERE deleted = 0 OR deleted IS NULL;
GO

/* 10. Lịch sử hóa đơn phải lưu được mã trạng thái 0-10.
       Cột cũ là BIT nên SQL Server ép mọi trạng thái > 0 thành 1 (Chờ xác nhận). */
IF EXISTS (
    SELECT 1
    FROM sys.columns c
    JOIN sys.types t ON c.user_type_id = t.user_type_id
    WHERE c.object_id = OBJECT_ID(N'dbo.lich_su_hoa_don')
      AND c.name = N'deleted'
      AND t.name = N'bit'
)
    ALTER TABLE dbo.lich_su_hoa_don ALTER COLUMN deleted SMALLINT NULL;
GO

-- Khôi phục các trạng thái có thể xác định chắc chắn từ nội dung lịch sử cũ.
UPDATE dbo.lich_su_hoa_don
SET deleted = CASE
    WHEN hanh_dong LIKE N'%hoàn hàng%' OR hanh_dong LIKE N'%nhận hàng hoàn%' THEN 8
    WHEN hanh_dong LIKE N'%hoàn kho%' OR hanh_dong LIKE N'%về kho%' THEN 10
    WHEN hanh_dong LIKE N'%giao hàng thất bại%' OR hanh_dong LIKE N'%giao thất bại%' THEN 9
    WHEN hanh_dong LIKE N'%yêu cầu hoàn%' THEN 7
    WHEN hanh_dong LIKE N'%hủy%' THEN 6
    WHEN hanh_dong LIKE N'%hoàn thành%' THEN 5
    WHEN hanh_dong LIKE N'%đang vận chuyển%' OR hanh_dong LIKE N'%đang giao%' THEN 4
    WHEN hanh_dong LIKE N'%chờ vận chuyển%' OR hanh_dong LIKE N'%chờ giao%' THEN 3
    WHEN hanh_dong LIKE N'%đã xác nhận%' THEN 2
    WHEN hanh_dong LIKE N'%chờ xác nhận%' OR hanh_dong LIKE N'%đặt hàng%' THEN 1
    ELSE deleted
END;
GO
