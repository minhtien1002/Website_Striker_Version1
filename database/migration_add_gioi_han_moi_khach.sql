/*
   Idempotent schema fix for existing Database_STRIKER_Final installations.
   The entity and the full database script define these fields, but older
   databases may not have received them.
*/
IF COL_LENGTH('dbo.phieu_giam_gia', 'loai_ap_dung') IS NULL
BEGIN
    ALTER TABLE dbo.phieu_giam_gia
        ADD loai_ap_dung NVARCHAR(30) NULL;
END;

IF COL_LENGTH('dbo.phieu_giam_gia', 'so_luong_phat_hanh') IS NULL
BEGIN
    ALTER TABLE dbo.phieu_giam_gia
        ADD so_luong_phat_hanh INT NULL;
END;

IF COL_LENGTH('dbo.phieu_giam_gia', 'so_luot_da_dung') IS NULL
BEGIN
    ALTER TABLE dbo.phieu_giam_gia
        ADD so_luot_da_dung INT NULL;
END;

IF COL_LENGTH('dbo.phieu_giam_gia', 'phan_tram_giam_gia') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD phan_tram_giam_gia FLOAT NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'so_tien_giam_toi_da') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD so_tien_giam_toi_da FLOAT NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'hoa_don_toi_thieu') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD hoa_don_toi_thieu FLOAT NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'so_luong_dung') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD so_luong_dung INT NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'ngay_bat_dau') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD ngay_bat_dau DATETIME2(6) NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'ngay_ket_thuc') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD ngay_ket_thuc DATETIME2(6) NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'trang_thai') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD trang_thai BIT NULL;
IF COL_LENGTH('dbo.phieu_giam_gia', 'rieng_tu') IS NULL
    ALTER TABLE dbo.phieu_giam_gia ADD rieng_tu BIT NULL;

IF COL_LENGTH('dbo.phieu_giam_gia', 'gioi_han_moi_khach') IS NULL
BEGIN
    ALTER TABLE dbo.phieu_giam_gia
        ADD gioi_han_moi_khach INT NULL;
END;
GO
