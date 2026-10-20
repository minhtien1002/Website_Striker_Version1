/*
  Voucher: tách rõ số lượng phát hành, số lượt đã dùng, phạm vi giảm
  (đơn hàng / phí vận chuyển) và giới hạn theo khách.
  Có thể chạy nhiều lần trên SQL Server.
*/
IF COL_LENGTH('dbo.phieu_giam_gia', 'loai_ap_dung') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD loai_ap_dung nvarchar(30) NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'so_luong_phat_hanh') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD so_luong_phat_hanh int NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'so_luot_da_dung') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD so_luot_da_dung int NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'gioi_han_moi_khach') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD gioi_han_moi_khach int NULL;
GO

/* Backfill an toàn cho dữ liệu cũ. FREESHIP được nhận diện từ mô tả/mã sẵn có. */
UPDATE dbo.phieu_giam_gia
SET loai_ap_dung = CASE
    WHEN UPPER(ISNULL(ma, '')) LIKE '%FREESHIP%'
      OR UPPER(ISNULL(ten_phieu_giam_gia, '')) LIKE '%PHÍ VẬN CHUYỂN%'
      OR UPPER(ISNULL(mo_ta, '')) LIKE '%PHÍ VẬN CHUYỂN%'
    THEN N'PHI_VAN_CHUYEN' ELSE N'DON_HANG' END
WHERE loai_ap_dung IS NULL;

UPDATE dbo.phieu_giam_gia
SET so_luong_phat_hanh = so_luong_dung,
    so_luot_da_dung = 0
WHERE so_luong_phat_hanh IS NULL OR so_luot_da_dung IS NULL;
GO
