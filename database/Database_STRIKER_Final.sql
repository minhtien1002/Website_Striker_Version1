/* =========================================================================
   Database_STRIKER_Final - FILE DATABASE MOI (SCHEMA + DU LIEU)
   Xuat luc: 2026-10-09 11:21:12
   Cach chay: trong SSMS chon database 'master', tat ung dung Spring Boot, roi F5.
   ========================================================================= */
USE [master]
GO
IF DB_ID(N'Database_STRIKER_Final') IS NOT NULL
BEGIN
    ALTER DATABASE [Database_STRIKER_Final] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [Database_STRIKER_Final];
END
GO
CREATE DATABASE [Database_STRIKER_Final]
GO
USE [Database_STRIKER_Final]
GO

/* ============================ TABLES ============================ */
CREATE TABLE [dbo].[anh_san_pham] (
    [id] int IDENTITY(1,1) NOT NULL,
    [url_anh] nvarchar(max) NULL,
    [la_anh_dai_dien] bit NULL CONSTRAINT [DF__anh_san_p__la_an__6754599E] DEFAULT ((0)),
    [ngay_tao] datetime NULL CONSTRAINT [DF__anh_san_p__ngay___68487DD7] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__anh_san_p__ngay___693CA210] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__anh_san_p__delet__6A30C649] DEFAULT ((0)),
    CONSTRAINT [PK__anh_san___3213E83F3275A2D9] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[chat_lieu] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ten_chat_lieu] nvarchar(100) NOT NULL,
    [ma_chat_lieu] nvarchar(255) NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__chat_lieu__ngay___6B24EA82] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__chat_lieu__ngay___6C190EBB] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__chat_lieu__delet__6D0D32F4] DEFAULT ((0)),
    [mo_ta] nvarchar(500) NULL,
    CONSTRAINT [PK__chat_lie__3213E83FAC800801] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[chi_tiet_dot_giam_gia] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_dot_giam_gia] int NOT NULL,
    [id_chi_tiet_sp] int NOT NULL,
    [ma] nvarchar(255) NULL,
    [gia_ban_dau] decimal(18, 2) NOT NULL,
    [gia_sau_khi_giam] decimal(18, 2) NOT NULL,
    [deleted] bit NOT NULL,
    CONSTRAINT [PK__chi_tiet__3213E83F288AF11B] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[chi_tiet_san_pham] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_mau_sac] int NULL,
    [id_kich_co] int NULL,
    [id_san_pham] int NULL,
    [ma] nvarchar(255) NULL,
    [so_luong_ton_kho] int NOT NULL CONSTRAINT [DF__chi_tiet___so_lu__6E01572D] DEFAULT ((0)),
    [mo_ta_chi_tiet] nvarchar(500) NULL,
    [gia_ban] decimal(19, 2) NOT NULL,
    [ngay_nhap] datetime NULL CONSTRAINT [DF__chi_tiet___ngay___6EF57B66] DEFAULT (getdate()),
    [ngay_tao] datetime NULL CONSTRAINT [DF__chi_tiet___ngay___6FE99F9F] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__chi_tiet___ngay___70DDC3D8] DEFAULT (getdate()),
    [deleted] bit NULL,
    [id_anh_san_pham] int NULL,
    [so_luong_da_ban] int NOT NULL CONSTRAINT [DF_ctsp_so_luong_da_ban] DEFAULT ((0)),
    CONSTRAINT [PK__chi_tiet__3213E83F6327DE02] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[danh_muc] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ten_danh_muc] nvarchar(100) NOT NULL,
    [ma_danh_muc] nvarchar(255) NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__danh_muc__ngay_t__74AE54BC] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__danh_muc__ngay_c__75A278F5] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__danh_muc__delete__76969D2E] DEFAULT ((0)),
    [mo_ta] nvarchar(500) NULL,
    CONSTRAINT [PK__danh_muc__3213E83F57A79133] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[dia_chi_khach_hang] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_khach_hang] int NOT NULL,
    [ma] nvarchar(255) NULL,
    [thanh_pho] nvarchar(255) NOT NULL,
    [quan] nvarchar(255) NOT NULL,
    [phuong] nvarchar(255) NOT NULL,
    [dia_chi_cu_the] nvarchar(255) NOT NULL,
    [mac_dinh] bit NOT NULL CONSTRAINT [DF__dia_chi_k__mac_d__778AC167] DEFAULT ((0)),
    [deleted] bit NOT NULL,
    CONSTRAINT [PK__dia_chi___3213E83FC1D35FAB] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[dot_giam_gia] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ma] nvarchar(255) NULL,
    [ten_dot_giam_gia] nvarchar(255) NOT NULL,
    [loai_giam_gia_ap_dung] nvarchar(255) NOT NULL,
    [gia_tri_giam_gia] decimal(18, 2) NOT NULL,
    [so_tien_giam_toi_da] decimal(18, 2) NOT NULL,
    [ngay_bat_dau] date NULL,
    [ngay_ket_thuc] date NULL,
    [trang_thai] bit NOT NULL CONSTRAINT [DF__dot_giam___trang__787EE5A0] DEFAULT ((1)),
    [deleted] bit NOT NULL,
    CONSTRAINT [PK__dot_giam__3213E83FD5FC5A40] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[gio_hang] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_khach_hang] int NULL,
    [id_hoa_don] int NULL,
    [tong_tien] decimal(18, 2) NOT NULL CONSTRAINT [DF__gio_hang__tong_t__797309D9] DEFAULT ((0)),
    [created_at] datetime NULL CONSTRAINT [DF__gio_hang__create__7A672E12] DEFAULT (getdate()),
    [updated_at] datetime NULL CONSTRAINT [DF__gio_hang__update__7B5B524B] DEFAULT (getdate()),
    CONSTRAINT [PK__gio_hang__3213E83F613C7627] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[gio_hang_chi_tiet] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_gio_hang] int NOT NULL,
    [id_chi_tiet_sp] int NOT NULL,
    [so_luong] int NOT NULL CONSTRAINT [DF__gio_hang___so_lu__7C4F7684] DEFAULT ((1)),
    [gia] decimal(18, 2) NOT NULL,
    [thanh_tien] decimal(18, 2) NOT NULL,
    [created_at] datetime NULL CONSTRAINT [DF__gio_hang___creat__7D439ABD] DEFAULT (getdate()),
    CONSTRAINT [PK__gio_hang__3213E83FAF41F3F0] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[hinh_thuc_thanh_toan] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_hoa_don] int NOT NULL,
    [id_phuong_thuc_thanh_toan] int NOT NULL,
    [tien_chuyen_khoan] decimal(18, 2) NOT NULL CONSTRAINT [DF__hinh_thuc__tien___7E37BEF6] DEFAULT ((0)),
    [tien_mat] decimal(18, 2) NOT NULL CONSTRAINT [DF__hinh_thuc__tien___7F2BE32F] DEFAULT ((0)),
    [ma] nvarchar(255) NULL,
    [deleted] bit NOT NULL,
    CONSTRAINT [PK__hinh_thu__3213E83FE3D3A583] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[hoa_don] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_khach_hang] int NOT NULL,
    [id_nhan_vien] int NOT NULL,
    [id_phieu_giam_gia] int NULL,
    [ma] nvarchar(255) NULL,
    [tien_san_pham] decimal(18, 2) NOT NULL,
    [loai_don] nvarchar(255) NOT NULL,
    [phi_van_chuyen] decimal(18, 2) NOT NULL CONSTRAINT [DF__hoa_don__phi_van__00200768] DEFAULT ((0)),
    [tong_tien] numeric(38, 2) NULL,
    [tong_tien_sau_giam] numeric(38, 2) NULL,
    [ghi_chu] nvarchar(255) NULL,
    [ten_khach_hang] nvarchar(255) NOT NULL,
    [dia_chi_khach_hang] nvarchar(255) NOT NULL,
    [so_dien_thoai_khach_hang] varchar(255) NULL,
    [email] nvarchar(255) NULL,
    [ngay_tao] datetime NOT NULL CONSTRAINT [DF__hoa_don__ngay_ta__01142BA1] DEFAULT (getdate()),
    [ngay_thanh_toan] datetimeoffset(6) NULL,
    [trang_thai] tinyint NOT NULL CONSTRAINT [DF__hoa_don__trang_t__02084FDA] DEFAULT ((1)),
    [deleted] bit NOT NULL,
    [created_at] datetime NOT NULL CONSTRAINT [DF__hoa_don__created__02FC7413] DEFAULT (getdate()),
    [created_by] int NULL,
    [updated_at] datetime2(6) NULL,
    [updated_by] int NULL,
    CONSTRAINT [PK__hoa_don__3213E83F336813A2] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[hoa_don_chi_tiet] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_hoa_don] int NOT NULL,
    [id_chi_tiet_sp] int NOT NULL,
    [ma] nvarchar(255) NULL,
    [gia] decimal(18, 2) NOT NULL,
    [trang_thai] tinyint NOT NULL CONSTRAINT [DF__hoa_don_c__trang__03F0984C] DEFAULT ((1)),
    [ghi_chu] nvarchar(255) NULL,
    [deleted] bit NOT NULL,
    [so_luong] int NOT NULL CONSTRAINT [DF__hoa_don_c__so_lu__04E4BC85] DEFAULT ((1)),
    CONSTRAINT [PK__hoa_don___3213E83F6F0007B3] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[khach_hang] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_tai_khoan] int NULL,
    [ma] nvarchar(255) NULL,
    [ten] nvarchar(255) NULL,
    [gioi_tinh] tinyint NULL,
    [ngay_sinh] datetime2(6) NULL,
    [deleted] bit NULL,
    [created_at] datetime2(6) NULL,
    [created_by] int NULL,
    [updated_at] datetime2(6) NULL,
    [updated_by] int NULL,
    [cccd] nvarchar(255) NULL,
    [id_dia_chi_khach_hang] int NULL,
    CONSTRAINT [PK__khach_ha__3213E83F04C7D5E8] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[kich_co] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ten_kich_co] nvarchar(50) NOT NULL,
    [ma_kich_co] nvarchar(255) NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__kich_co__ngay_ta__05D8E0BE] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__kich_co__ngay_ca__06CD04F7] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__kich_co__deleted__07C12930] DEFAULT ((0)),
    [mo_ta] nvarchar(500) NULL,
    CONSTRAINT [PK__kich_co__3213E83F9BE39AA0] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[lich_su_hoa_don] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_hoa_don] int NOT NULL,
    [id_nhan_vien] int NULL,
    [ma] nvarchar(255) NULL,
    [hanh_dong] nvarchar(255) NULL,
    [thoi_gian] datetimeoffset(6) NULL,
    [deleted] bit NULL,
    [ghi_chu] nvarchar(500) NULL,
    CONSTRAINT [PK__lich_su___3213E83F6C1D8441] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[mau_sac] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ten_mau_sac] nvarchar(100) NOT NULL,
    [ma_mau_sac] nvarchar(255) NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__mau_sac__ngay_ta__08B54D69] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__mau_sac__ngay_ca__09A971A2] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__mau_sac__deleted__0A9D95DB] DEFAULT ((0)),
    [mo_ta] nvarchar(500) NULL,
    CONSTRAINT [PK__mau_sac__3213E83F5D28A5AB] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[nhan_vien] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_tai_khoan] int NULL,
    [ma] nvarchar(255) NULL,
    [ten_nhan_vien] nvarchar(255) NULL,
    [ngay_sinh] datetime2(6) NULL,
    [anh_nhan_vien] nvarchar(255) NULL,
    [ghi_chu] nvarchar(255) NULL,
    [thanh_pho] nvarchar(255) NULL,
    [quan] nvarchar(255) NULL,
    [phuong] nvarchar(255) NULL,
    [dia_chi_cu_the] nvarchar(255) NULL,
    [cccd] nvarchar(255) NULL,
    [deleted] bit NULL,
    [created_at] datetimeoffset(6) NULL,
    [created_by] int NULL,
    [updated_at] datetimeoffset(6) NULL,
    [updated_by] int NULL,
    [gioi_tinh] bit NULL,
    CONSTRAINT [PK__nhan_vie__3213E83FA39C3C41] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[phieu_giam_gia] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ma] nvarchar(255) NULL,
    [ten_phieu_giam_gia] nvarchar(255) NULL,
    [loai_phieu_giam_gia] nvarchar(255) NULL,
    [phan_tram_giam_gia] float NULL,
    [so_tien_giam_toi_da] float NULL,
    [hoa_don_toi_thieu] float NULL,
    [so_luong_dung] int NULL,
    [ngay_bat_dau] datetime2(6) NULL,
    [ngay_ket_thuc] datetime2(6) NULL,
    [trang_thai] bit NULL,
    [rieng_tu] bit NULL,
    [mo_ta] nvarchar(255) NULL,
    [deleted] bit NULL,
    CONSTRAINT [PK__phieu_gi__3213E83F31A6EFB6] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[phieu_giam_gia_ca_nhan] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_phieu_giam_gia] int NULL,
    [id_khach_hang] int NULL,
    [ma] nvarchar(255) NULL,
    [ngay_nhan] datetime2(6) NULL,
    [ngay_het_han] datetime2(6) NULL,
    [trang_thai] bit NULL,
    [deleted] bit NULL,
    CONSTRAINT [PK__phieu_gi__3213E83FF3649F63] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[phuong_thuc_thanh_toan] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ma] nvarchar(255) NULL,
    [kieu_thanh_toan] nvarchar(255) NULL,
    [deleted] bit NULL,
    CONSTRAINT [PK__phuong_t__3213E83F782C0646] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[quyen_han] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ma] nvarchar(255) NULL,
    [cap_quyen_han] int NULL,
    [deleted] bit NULL,
    CONSTRAINT [PK__quyen_ha__3213E83F645C3CAB] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[san_pham] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_danh_muc] int NULL,
    [id_thuong_hieu] int NULL,
    [ma] nvarchar(255) NULL,
    [ten_san_pham] nvarchar(255) NOT NULL,
    [mo_ta_san_pham] nvarchar(max) NULL,
    [gioi_tinh] tinyint NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__san_pham__ngay_t__0E6E26BF] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__san_pham__ngay_c__0F624AF8] DEFAULT (getdate()),
    [deleted] bit NULL,
    [id_anh_san_pham] int NULL,
    [id_chat_lieu] int NULL,
    CONSTRAINT [PK__san_pham__3213E83F430A65C1] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[tai_khoan] (
    [id] int IDENTITY(1,1) NOT NULL,
    [id_quyen_han] int NULL,
    [ma] nvarchar(255) NULL,
    [ten_dang_nhap] nvarchar(255) NULL,
    [email] nvarchar(255) NULL,
    [so_dien_thoai] nvarchar(255) NULL,
    [mat_khau] nvarchar(255) NULL,
    [deleted] bit NULL,
    CONSTRAINT [PK__tai_khoa__3213E83F1BA6F8EB] PRIMARY KEY CLUSTERED ([id])
);
GO
CREATE TABLE [dbo].[thuong_hieu] (
    [id] int IDENTITY(1,1) NOT NULL,
    [ten_thuong_hieu] nvarchar(100) NOT NULL,
    [ma_thuong_hieu] nvarchar(255) NULL,
    [ngay_tao] datetime NULL CONSTRAINT [DF__thuong_hi__ngay___10566F31] DEFAULT (getdate()),
    [ngay_cap_nhat] datetime NULL CONSTRAINT [DF__thuong_hi__ngay___114A936A] DEFAULT (getdate()),
    [deleted] bit NOT NULL CONSTRAINT [DF__thuong_hi__delet__123EB7A3] DEFAULT ((0)),
    [mo_ta] nvarchar(500) NULL,
    CONSTRAINT [PK__thuong_h__3213E83F5BE84A93] PRIMARY KEY CLUSTERED ([id])
);
GO

/* ============================ DATA ============================ */
-- anh_san_pham: 40 dong
SET IDENTITY_INSERT [dbo].[anh_san_pham] ON;
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (1, N'http://localhost:8080/uploads/images/11_Ao_Thun_Bong_Da_STRIKER_DriFIT.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (2, N'http://localhost:8080/uploads/images/22_Ao_Thun_Nam_Adidas_Climacool_BongDa.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (3, N'http://localhost:8080/uploads/images/ao-da-bong-anh.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (4, N'http://localhost:8080/uploads/images/ao-1.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (5, N'http://localhost:8080/uploads/images/avt-logo-ao-bong-da-lop.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (6, N'http://localhost:8080/uploads/images/4-xanh-coban-ld9248-800x800.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (7, N'http://localhost:8080/uploads/images/8_Ao_Thun_Bong_Ro_STRIKER_DriFIT.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (8, N'http://localhost:8080/uploads/images/19_Ao_Ba_Lo_Gym_Puma_QuickDry.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (9, N'http://localhost:8080/uploads/images/7_Ao_Ba_Lo_Tap_Gym_UnderArmour_HeatGear.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (10, N'http://localhost:8080/uploads/images/15_Ao_Polo_Tennis_Puma_Climacool.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (11, N'http://localhost:8080/uploads/images/21_Ao_Polo_STRIKER_San_Tennis.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (12, N'http://localhost:8080/uploads/images/3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (13, N'http://localhost:8080/uploads/images/13_Ao_Khoac_Gio_Nike_Windrunner.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (14, N'http://localhost:8080/uploads/images/16_Ao_Thun_Yoga_Nu_STRIKER_Cotton.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (15, N'http://localhost:8080/uploads/images/18_Ao_Khoac_Gio_Adidas_AeroReady_ChayBo.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (16, N'http://localhost:8080/uploads/images/4_Ao_Polo_Tennis_Nike_Climacool.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (17, N'http://localhost:8080/uploads/images/15_Ao_Polo_Tennis_Puma_Climacool.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (18, N'http://localhost:8080/uploads/images/ao-1.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (19, N'http://localhost:8080/uploads/images/ao-1.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (20, N'http://localhost:8080/uploads/images/7_Ao_Ba_Lo_Tap_Gym_UnderArmour_HeatGear.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (21, N'http://localhost:8080/uploads/images/19_Ao_Ba_Lo_Gym_Puma_QuickDry.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (22, N'http://localhost:8080/uploads/images/3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (23, N'http://localhost:8080/uploads/images/16_Ao_Thun_Yoga_Nu_STRIKER_Cotton.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (24, N'http://localhost:8080/uploads/images/16_Ao_Thun_Yoga_Nu_STRIKER_Cotton.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (25, N'http://localhost:8080/uploads/images/6_Ao_Khoac_Gio_Puma_QuickDry.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (26, N'http://localhost:8080/uploads/images/6_Ao_Khoac_Gio_Puma_QuickDry.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (27, N'http://localhost:8080/uploads/images/10_Ao_Polo_Golf_Nike_Climacool.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (28, N'http://localhost:8080/uploads/images/10_Ao_Polo_Golf_Nike_Climacool.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (29, N'http://localhost:8080/uploads/images/21_Ao_Polo_STRIKER_San_Tennis.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (30, N'http://localhost:8080/uploads/images/3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg', 1, '2025-02-01T09:00:00.000', '2025-02-01T09:00:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (31, N'http://localhost:8080/uploads/images/3_Ao_Thun_The_Thao_DriFIT_STRIKER.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (32, N'http://localhost:8080/uploads/images/ao-da-bong-anh.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (33, N'http://localhost:8080/uploads/images/8_Ao_Thun_Bong_Ro_STRIKER_DriFIT.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (34, N'http://localhost:8080/uploads/images/15_Ao_Polo_Tennis_Puma_Climacool.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (35, N'http://localhost:8080/uploads/images/13_Ao_Khoac_Gio_Nike_Windrunner.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (36, N'http://localhost:8080/uploads/images/7_Ao_Ba_Lo_Tap_Gym_UnderArmour_HeatGear.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (37, N'http://localhost:8080/uploads/images/4_Ao_Polo_Tennis_Nike_Climacool.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (38, N'http://localhost:8080/uploads/images/ao-1.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (39, N'http://localhost:8080/uploads/images/6_Ao_Khoac_Gio_Puma_QuickDry.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
INSERT INTO [dbo].[anh_san_pham] ([id], [url_anh], [la_anh_dai_dien], [ngay_tao], [ngay_cap_nhat], [deleted]) VALUES (40, N'http://localhost:8080/uploads/images/10_Ao_Polo_Golf_Nike_Climacool.jpg', 0, '2025-02-01T09:05:00.000', '2025-02-01T09:05:00.000', 0);
SET IDENTITY_INSERT [dbo].[anh_san_pham] OFF;
GO
-- chat_lieu: 16 dong
SET IDENTITY_INSERT [dbo].[chat_lieu] ON;
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (1, N'Polyester 100%', N'CL001', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (2, N'Cotton 100%', N'CL002', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (3, N'Cotton pha Spandex', N'CL003', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (4, N'Vải thun lạnh', N'CL004', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (5, N'Vải mè cao cấp', N'CL005', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (6, N'Vải lưới thoáng khí', N'CL006', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (7, N'Nylon Spandex', N'CL007', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (8, N'Polyester tái chế', N'CL008', '2025-01-10T08:10:00.000', '2025-01-10T08:10:00.000', 0, NULL);
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (9, N'Dri-FIT', N'CL009', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Công nghệ thấm hút mồ hôi và làm khô nhanh của Nike');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (10, N'AEROREADY', N'CL010', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Công nghệ giữ cơ thể khô thoáng của Adidas');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (11, N'dryCELL', N'CL011', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Công nghệ thoát ẩm của Puma');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (12, N'Kháng khuẩn', N'CL012', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Xử lý kháng khuẩn, khử mùi cho áo thể thao');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (13, N'Co giãn 4 chiều', N'CL013', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Vải co giãn 4 chiều giúp vận động thoải mái');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (14, N'Chống tia UV', N'CL014', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Lớp phủ chống tia cực tím khi vận động ngoài trời');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (15, N'HeatGear', N'CL015', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Công nghệ làm mát của Under Armour cho thời tiết nóng');
INSERT INTO [dbo].[chat_lieu] ([id], [ten_chat_lieu], [ma_chat_lieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (16, N'AT DRY', N'CL016', '2026-10-09T01:42:51.430', '2026-10-09T01:42:51.430', 0, N'Công nghệ thoát ẩm nhanh của Li-Ning');
SET IDENTITY_INSERT [dbo].[chat_lieu] OFF;
GO
-- chi_tiet_dot_giam_gia: 23 dong
SET IDENTITY_INSERT [dbo].[chi_tiet_dot_giam_gia] ON;
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (1, 1, 1, N'CTDGG001', 790000.00, 632000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (2, 1, 2, N'CTDGG002', 790000.00, 632000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (3, 1, 3, N'CTDGG003', 790000.00, 632000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (4, 1, 6, N'CTDGG004', 890000.00, 712000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (5, 1, 7, N'CTDGG005', 890000.00, 712000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (6, 1, 10, N'CTDGG006', 329000.00, 263200.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (7, 1, 11, N'CTDGG007', 329000.00, 263200.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (8, 1, 23, N'CTDGG008', 990000.00, 792000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (9, 3, 36, N'CTDGG009', 859000.00, 730150.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (10, 3, 37, N'CTDGG010', 859000.00, 730150.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (11, 3, 40, N'CTDGG011', 649000.00, 551650.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (12, 3, 43, N'CTDGG012', 279000.00, 237150.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (13, 3, 44, N'CTDGG013', 279000.00, 237150.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (14, 4, 77, N'CTDGG014', 449000.00, 399000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (15, 4, 78, N'CTDGG015', 449000.00, 399000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (16, 4, 80, N'CTDGG016', 399000.00, 349000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (17, 5, 49, N'CTDGG017', 590000.00, 442500.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (18, 5, 50, N'CTDGG018', 590000.00, 442500.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (19, 5, 53, N'CTDGG019', 699000.00, 524250.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (20, 5, 56, N'CTDGG020', 559000.00, 419250.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (21, 6, 26, N'CTDGG021', 690000.00, 610000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (22, 6, 30, N'CTDGG022', 629000.00, 549000.00, 0);
INSERT INTO [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_sp], [ma], [gia_ban_dau], [gia_sau_khi_giam], [deleted]) VALUES (23, 6, 33, N'CTDGG023', 549000.00, 469000.00, 0);
SET IDENTITY_INSERT [dbo].[chi_tiet_dot_giam_gia] OFF;
GO
-- chi_tiet_san_pham: 90 dong
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham] ON;
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (1, 1, 3, 1, N'CTSP001', 50, N'Áo bóng đá Nike Academy 23 - Đen - M', 790000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 1, 4);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (2, 1, 4, 1, N'CTSP002', 45, N'Áo bóng đá Nike Academy 23 - Đen - L', 790000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 1, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (3, 2, 3, 1, N'CTSP003', 30, N'Áo bóng đá Nike Academy 23 - Trắng - M', 790000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 31, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (4, 3, 4, 1, N'CTSP004', 25, N'Áo bóng đá Nike Academy 23 - Đỏ - L', 790000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 1, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (5, 4, 5, 1, N'CTSP005', 22, N'Áo bóng đá Nike Academy 23 - Xanh - XL', 790000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 1, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (6, 4, 3, 2, N'CTSP006', 40, N'Áo bóng đá Adidas Tiro 24 - Xanh - M', 890000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 2, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (7, 4, 4, 2, N'CTSP007', 38, N'Áo bóng đá Adidas Tiro 24 - Xanh - L', 890000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 2, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (8, 2, 5, 2, N'CTSP008', 20, N'Áo bóng đá Adidas Tiro 24 - Trắng - XL', 890000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 32, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (9, 1, 6, 2, N'CTSP009', 15, N'Áo bóng đá Adidas Tiro 24 - Đen - XXL', 890000.00, '2025-02-02T08:00:00.000', '2025-02-02T08:00:00.000', '2026-01-05T10:00:00.000', 0, 2, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (10, 3, 3, 3, N'CTSP010', 80, N'Áo bóng đá Kamito Aurora - Đỏ - M', 329000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-05T10:00:00.000', 0, 3, 3);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (11, 5, 4, 3, N'CTSP011', 75, N'Áo bóng đá Kamito Aurora - Xanh lá - L', 329000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-05T10:00:00.000', 0, 3, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (12, 1, 5, 3, N'CTSP012', 60, N'Áo bóng đá Kamito Aurora - Đen - XL', 329000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-05T10:00:00.000', 0, 3, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (13, 6, 4, 3, N'CTSP013', 55, N'Áo bóng đá Kamito Aurora - Vàng - L', 329000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-05T10:00:00.000', 0, 3, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (14, 11, 3, 4, N'CTSP014', 48, N'Áo bóng đá Jogarbola Classic - Navy - M', 389000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-06T10:00:00.000', 0, 4, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (15, 11, 4, 4, N'CTSP015', 44, N'Áo bóng đá Jogarbola Classic - Navy - L', 389000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-06T10:00:00.000', 0, 4, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (16, 3, 5, 4, N'CTSP016', 33, N'Áo bóng đá Jogarbola Classic - Đỏ - XL', 389000.00, '2025-02-03T08:00:00.000', '2025-02-03T08:00:00.000', '2026-01-06T10:00:00.000', 0, 4, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (17, 7, 3, 5, N'CTSP017', 62, N'Áo bóng đá Grand Sport League - Cam - M', 299000.00, '2025-02-04T08:00:00.000', '2025-02-04T08:00:00.000', '2026-01-06T10:00:00.000', 0, 5, 11);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (18, 7, 4, 5, N'CTSP018', 58, N'Áo bóng đá Grand Sport League - Cam - L', 299000.00, '2025-02-04T08:00:00.000', '2025-02-04T08:00:00.000', '2026-01-06T10:00:00.000', 0, 5, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (19, 12, 4, 5, N'CTSP019', 40, N'Áo bóng đá Grand Sport League - Ngọc - L', 299000.00, '2025-02-04T08:00:00.000', '2025-02-04T08:00:00.000', '2026-01-06T10:00:00.000', 0, 5, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (20, 5, 4, 6, N'CTSP020', 26, N'Áo thủ môn Puma GK Pro - Xanh lá - L', 759000.00, '2025-02-05T08:00:00.000', '2025-02-05T08:00:00.000', '2026-01-10T10:00:00.000', 0, 6, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (21, 1, 5, 6, N'CTSP021', 21, N'Áo thủ môn Puma GK Pro - Đen - XL', 759000.00, '2025-02-05T08:00:00.000', '2025-02-05T08:00:00.000', '2026-01-10T10:00:00.000', 0, 6, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (22, 9, 4, 6, N'CTSP022', 14, N'Áo thủ môn Puma GK Pro - Tím - L', 759000.00, '2025-02-05T08:00:00.000', '2025-02-05T08:00:00.000', '2026-01-10T10:00:00.000', 0, 6, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (23, 2, 3, 7, N'CTSP023', 28, N'Áo bóng đá Mizuno Pro - Trắng - M', 990000.00, '2025-02-06T08:00:00.000', '2025-02-06T08:00:00.000', '2026-01-12T10:00:00.000', 0, 30, 2);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (24, 11, 4, 7, N'CTSP024', 24, N'Áo bóng đá Mizuno Pro - Navy - L', 990000.00, '2025-02-06T08:00:00.000', '2025-02-06T08:00:00.000', '2026-01-12T10:00:00.000', 0, 30, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (25, 1, 5, 7, N'CTSP025', 19, N'Áo bóng đá Mizuno Pro - Đen - XL', 990000.00, '2025-02-06T08:00:00.000', '2025-02-06T08:00:00.000', '2026-01-12T10:00:00.000', 0, 30, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (26, 1, 4, 8, N'CTSP026', 36, N'Áo bóng rổ Nike Icon - Đen - L', 690000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 7, 3);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (27, 3, 4, 8, N'CTSP027', 32, N'Áo bóng rổ Nike Icon - Đỏ - L', 690000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 33, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (28, 2, 5, 8, N'CTSP028', 27, N'Áo bóng rổ Nike Icon - Trắng - XL', 690000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 7, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (29, 4, 6, 8, N'CTSP029', 18, N'Áo bóng rổ Nike Icon - Xanh - XXL', 690000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 7, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (30, 11, 4, 9, N'CTSP030', 34, N'Áo bóng rổ Adidas Legends - Navy - L', 629000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 8, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (31, 6, 5, 9, N'CTSP031', 29, N'Áo bóng rổ Adidas Legends - Vàng - XL', 629000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 8, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (32, 1, 6, 9, N'CTSP032', 17, N'Áo bóng rổ Adidas Legends - Đen - XXL', 629000.00, '2025-02-07T08:00:00.000', '2025-02-07T08:00:00.000', '2026-01-15T10:00:00.000', 0, 8, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (33, 9, 4, 10, N'CTSP033', 33, N'Áo bóng rổ Kappa Court - Tím - L', 549000.00, '2025-02-08T08:00:00.000', '2025-02-08T08:00:00.000', '2026-01-18T10:00:00.000', 0, 9, 2);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (34, 6, 5, 10, N'CTSP034', 29, N'Áo bóng rổ Kappa Court - Vàng - XL', 549000.00, '2025-02-08T08:00:00.000', '2025-02-08T08:00:00.000', '2026-01-18T10:00:00.000', 0, 9, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (35, 1, 6, 10, N'CTSP035', 18, N'Áo bóng rổ Kappa Court - Đen - XXL', 549000.00, '2025-02-08T08:00:00.000', '2025-02-08T08:00:00.000', '2026-01-18T10:00:00.000', 0, 9, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (36, 2, 3, 11, N'CTSP036', 42, N'Áo cầu lông Yonex Tour - Trắng - M', 859000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 10, 3);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (37, 2, 4, 11, N'CTSP037', 38, N'Áo cầu lông Yonex Tour - Trắng - L', 859000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 34, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (38, 11, 4, 11, N'CTSP038', 31, N'Áo cầu lông Yonex Tour - Navy - L', 859000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 10, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (39, 3, 5, 11, N'CTSP039', 23, N'Áo cầu lông Yonex Tour - Đỏ - XL', 859000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 10, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (40, 3, 3, 12, N'CTSP040', 36, N'Áo cầu lông Li-Ning AeroNaut - Đỏ - M', 649000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 11, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (41, 1, 4, 12, N'CTSP041', 34, N'Áo cầu lông Li-Ning AeroNaut - Đen - L', 649000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 11, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (42, 12, 5, 12, N'CTSP042', 25, N'Áo cầu lông Li-Ning AeroNaut - Ngọc - XL', 649000.00, '2025-02-09T08:00:00.000', '2025-02-09T08:00:00.000', '2026-01-20T10:00:00.000', 0, 11, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (43, 6, 3, 13, N'CTSP043', 71, N'Áo cầu lông Kamito Smash - Vàng - M', 279000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 12, 3);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (44, 12, 4, 13, N'CTSP044', 66, N'Áo cầu lông Kamito Smash - Ngọc - L', 279000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 12, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (45, 4, 5, 13, N'CTSP045', 52, N'Áo cầu lông Kamito Smash - Xanh - XL', 279000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 12, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (46, 10, 2, 14, N'CTSP046', 40, N'Áo cầu lông Yonex Lady Fit - Hồng - S', 769000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 10, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (47, 10, 3, 14, N'CTSP047', 37, N'Áo cầu lông Yonex Lady Fit - Hồng - M', 769000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 10, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (48, 2, 3, 14, N'CTSP048', 30, N'Áo cầu lông Yonex Lady Fit - Trắng - M', 769000.00, '2025-02-10T08:00:00.000', '2025-02-10T08:00:00.000', '2026-01-22T10:00:00.000', 0, 10, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (49, 1, 3, 15, N'CTSP049', 55, N'Áo chạy bộ Nike Miler - Đen - M', 590000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 13, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (50, 1, 4, 15, N'CTSP050', 50, N'Áo chạy bộ Nike Miler - Đen - L', 590000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 35, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (51, 8, 4, 15, N'CTSP051', 43, N'Áo chạy bộ Nike Miler - Xám - L', 590000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 13, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (52, 12, 5, 15, N'CTSP052', 31, N'Áo chạy bộ Nike Miler - Ngọc - XL', 590000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 13, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (53, 1, 3, 16, N'CTSP053', 44, N'Áo chạy bộ UA Streaker - Đen - M', 699000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 14, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (54, 7, 4, 16, N'CTSP054', 39, N'Áo chạy bộ UA Streaker - Cam - L', 699000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 14, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (55, 8, 5, 16, N'CTSP055', 28, N'Áo chạy bộ UA Streaker - Xám - XL', 699000.00, '2025-02-11T08:00:00.000', '2025-02-11T08:00:00.000', '2026-01-25T10:00:00.000', 0, 14, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (56, 11, 3, 17, N'CTSP056', 41, N'Áo chạy bộ Adidas Own The Run - Navy - M', 559000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 15, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (57, 11, 4, 17, N'CTSP057', 36, N'Áo chạy bộ Adidas Own The Run - Navy - L', 559000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 15, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (58, 2, 5, 17, N'CTSP058', 27, N'Áo chạy bộ Adidas Own The Run - Trắng - XL', 559000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 15, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (59, 10, 2, 18, N'CTSP059', 38, N'Áo chạy bộ nữ UA Tech Tee - Hồng - S', 529000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 14, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (60, 9, 3, 18, N'CTSP060', 35, N'Áo chạy bộ nữ UA Tech Tee - Tím - M', 529000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 14, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (61, 1, 3, 18, N'CTSP061', 30, N'Áo chạy bộ nữ UA Tech Tee - Đen - M', 529000.00, '2025-02-12T08:00:00.000', '2025-02-12T08:00:00.000', '2026-01-28T10:00:00.000', 0, 14, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (62, 2, 3, 19, N'CTSP062', 32, N'Áo tennis Nike Court - Trắng - M', 789000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 16, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (63, 2, 4, 19, N'CTSP063', 29, N'Áo tennis Nike Court - Trắng - L', 789000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 37, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (64, 11, 4, 19, N'CTSP064', 23, N'Áo tennis Nike Court - Navy - L', 789000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 16, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (65, 12, 3, 20, N'CTSP065', 30, N'Áo tennis Adidas Club Polo - Ngọc - M', 669000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 17, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (66, 2, 4, 20, N'CTSP066', 27, N'Áo tennis Adidas Club Polo - Trắng - L', 669000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 17, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (67, 1, 5, 20, N'CTSP067', 21, N'Áo tennis Adidas Club Polo - Đen - XL', 669000.00, '2025-02-13T08:00:00.000', '2025-02-13T08:00:00.000', '2026-02-01T10:00:00.000', 0, 17, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (68, 3, 4, 21, N'CTSP068', 34, N'Áo bóng chuyền Mizuno Attack - Đỏ - L', 629000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 18, 2);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (69, 4, 4, 21, N'CTSP069', 31, N'Áo bóng chuyền Mizuno Attack - Xanh - L', 629000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 38, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (70, 1, 5, 21, N'CTSP070', 23, N'Áo bóng chuyền Mizuno Attack - Đen - XL', 629000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 18, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (71, 6, 4, 22, N'CTSP071', 46, N'Áo bóng chuyền GS Spike - Vàng - L', 319000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 19, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (72, 5, 4, 22, N'CTSP072', 42, N'Áo bóng chuyền GS Spike - Xanh lá - L', 319000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 19, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (73, 7, 5, 22, N'CTSP073', 33, N'Áo bóng chuyền GS Spike - Cam - XL', 319000.00, '2025-02-14T08:00:00.000', '2025-02-14T08:00:00.000', '2026-02-05T10:00:00.000', 0, 19, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (74, 1, 3, 23, N'CTSP074', 39, N'Áo gym UA Rush - Đen - M', 849000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 20, 5);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (75, 1, 4, 23, N'CTSP075', 36, N'Áo gym UA Rush - Đen - L', 849000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 36, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (76, 11, 4, 23, N'CTSP076', 28, N'Áo gym UA Rush - Navy - L', 849000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 20, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (77, 1, 3, 24, N'CTSP077', 52, N'Áo tank top gym Nike Pro - Đen - M', 449000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 21, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (78, 8, 4, 24, N'CTSP078', 47, N'Áo tank top gym Nike Pro - Xám - L', 449000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 21, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (79, 2, 5, 24, N'CTSP079', 35, N'Áo tank top gym Nike Pro - Trắng - XL', 449000.00, '2025-02-15T08:00:00.000', '2025-02-15T08:00:00.000', '2026-02-10T10:00:00.000', 0, 21, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (80, 8, 4, 25, N'CTSP080', 44, N'Áo tập gym Puma Train - Xám - L', 399000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 22, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (81, 5, 4, 25, N'CTSP081', 39, N'Áo tập gym Puma Train - Xanh lá - L', 399000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 22, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (82, 1, 5, 25, N'CTSP082', 30, N'Áo tập gym Puma Train - Đen - XL', 399000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 22, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (83, 1, 2, 26, N'CTSP083', 42, N'Áo bra gym nữ UA - Đen - S', 489000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 20, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (84, 10, 3, 26, N'CTSP084', 38, N'Áo bra gym nữ UA - Hồng - M', 489000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 20, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (85, 12, 3, 26, N'CTSP085', 26, N'Áo bra gym nữ UA - Ngọc - M', 489000.00, '2025-02-16T08:00:00.000', '2025-02-16T08:00:00.000', '2026-02-12T10:00:00.000', 0, 20, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (86, 4, 4, 27, N'CTSP086', 34, N'Áo bơi Decathlon Nabaiji - Xanh - L', 359000.00, '2025-02-17T08:00:00.000', '2025-02-17T08:00:00.000', '2026-02-15T10:00:00.000', 0, 23, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (87, 1, 5, 27, N'CTSP087', 27, N'Áo bơi Decathlon Nabaiji - Đen - XL', 359000.00, '2025-02-17T08:00:00.000', '2025-02-17T08:00:00.000', '2026-02-15T10:00:00.000', 0, 23, 0);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (88, 11, 4, 28, N'CTSP088', 25, N'Áo bơi chống nắng Li-Ning - Navy - L', 519000.00, '2025-02-17T08:00:00.000', '2025-02-17T08:00:00.000', '2026-02-15T10:00:00.000', 0, 24, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (89, 12, 4, 29, N'CTSP089', 22, N'Áo đạp xe Decathlon Triban - Ngọc - L', 619000.00, '2025-02-18T08:00:00.000', '2025-02-18T08:00:00.000', '2026-02-18T10:00:00.000', 0, 25, 1);
INSERT INTO [dbo].[chi_tiet_san_pham] ([id], [id_mau_sac], [id_kich_co], [id_san_pham], [ma], [so_luong_ton_kho], [mo_ta_chi_tiet], [gia_ban], [ngay_nhap], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [so_luong_da_ban]) VALUES (90, 2, 4, 30, N'CTSP090', 40, N'Áo polo Adidas Primegreen - Trắng - L', 539000.00, '2025-02-19T08:00:00.000', '2025-02-19T08:00:00.000', '2026-10-08T20:02:42.230', 0, 28, 0);
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham] OFF;
GO
-- danh_muc: 10 dong
SET IDENTITY_INSERT [dbo].[danh_muc] ON;
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (1, N'Áo bóng đá', N'DM001', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (2, N'Áo bóng rổ', N'DM002', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (3, N'Áo cầu lông', N'DM003', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (4, N'Áo chạy bộ', N'DM004', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (5, N'Áo tennis', N'DM005', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (6, N'Áo bóng chuyền', N'DM006', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (7, N'Áo tập gym', N'DM007', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (8, N'Áo bơi - đồ bơi', N'DM008', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (9, N'Áo đạp xe', N'DM009', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
INSERT INTO [dbo].[danh_muc] ([id], [ten_danh_muc], [ma_danh_muc], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (10, N'Áo polo thể thao', N'DM010', '2025-01-10T08:00:00.000', '2025-01-10T08:00:00.000', 0, NULL);
SET IDENTITY_INSERT [dbo].[danh_muc] OFF;
GO
-- dia_chi_khach_hang: 24 dong
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] ON;
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (1, 1, N'DC001', N'Hà Nội', N'Cầu Giấy', N'Quan Hoa', N'Số 24 ngõ 165 Cầu Giấy', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (2, 1, N'DC002', N'Hà Nội', N'Nam Từ Liêm', N'Mỹ Đình 1', N'Toà S2 Vinhomes Smart City', 0, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (3, 2, N'DC003', N'Hà Nội', N'Hoàng Mai', N'Định Công', N'Số 9 ngách 12 Định Công Hạ', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (4, 3, N'DC004', N'Hồ Chí Minh', N'Quận 3', N'Võ Thị Sáu', N'Số 145 Nguyễn Đình Chiểu', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (5, 3, N'DC005', N'Hồ Chí Minh', N'Bình Thạnh', N'Phường 25', N'Toà Landmark 81, Điện Biên Phủ', 0, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (6, 4, N'DC006', N'Hải Phòng', N'Lê Chân', N'Trại Cau', N'Số 56 Tô Hiệu', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (7, 5, N'DC007', N'Đà Nẵng', N'Hải Châu', N'Hoà Cường Bắc', N'Số 210 Núi Thành', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (8, 6, N'DC008', N'Hà Nội', N'Thanh Xuân', N'Nhân Chính', N'Số 3 ngõ 82 Nguyễn Tuân', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (9, 7, N'DC009', N'Hồ Chí Minh', N'Quận 7', N'Tân Phong', N'Số 18 Nguyễn Thị Thập', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (10, 8, N'DC010', N'Cần Thơ', N'Ninh Kiều', N'An Khánh', N'Số 77 Nguyễn Văn Cừ', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (11, 9, N'DC011', N'Hà Nội', N'Long Biên', N'Việt Hưng', N'Số 5 ngõ 30 Ngô Gia Tự', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (12, 10, N'DC012', N'Bình Dương', N'Thủ Dầu Một', N'Phú Hoà', N'Số 102 Đại lộ Bình Dương', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (13, 11, N'DC013', N'Hà Nội', N'Ba Đình', N'Ngọc Khánh', N'Số 41 Nguyễn Chí Thanh', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (14, 12, N'DC014', N'Hồ Chí Minh', N'Tân Bình', N'Phường 4', N'Số 89 Cộng Hoà', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (15, 13, N'DC015', N'Đà Nẵng', N'Thanh Khê', N'Vĩnh Trung', N'Số 132 Điện Biên Phủ', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (16, 13, N'DC016', N'Quảng Nam', N'Hội An', N'Cẩm Châu', N'Số 20 Cửa Đại', 0, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (17, 14, N'DC017', N'Hà Nội', N'Hà Đông', N'Văn Quán', N'Số 7 Nguyễn Khuyến', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (18, 15, N'DC018', N'Nghệ An', N'Vinh', N'Hưng Bình', N'Số 33 Lê Hồng Phong', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (19, 16, N'DC019', N'Hồ Chí Minh', N'Quận 10', N'Phường 12', N'Số 202 Ba Tháng Hai', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (20, 17, N'DC020', N'Hà Nội', N'Tây Hồ', N'Xuân La', N'Số 18 Võ Chí Công', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (21, 18, N'DC021', N'Bắc Ninh', N'Bắc Ninh', N'Suối Hoa', N'Số 55 Nguyễn Đăng Đạo', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (22, 19, N'DC022', N'Khánh Hoà', N'Nha Trang', N'Lộc Thọ', N'Số 96 Trần Phú', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (23, 20, N'DC023', N'Hồ Chí Minh', N'Thủ Đức', N'Linh Trung', N'Số 12 Võ Văn Ngân', 1, 0);
INSERT INTO [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ma], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [deleted]) VALUES (24, 20, N'DC024', N'Đồng Nai', N'Biên Hoà', N'Tân Hiệp', N'Số 74 Phạm Văn Thuận', 0, 0);
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] OFF;
GO
-- dot_giam_gia: 6 dong
SET IDENTITY_INSERT [dbo].[dot_giam_gia] ON;
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (1, N'DGG001', N'Giảm giá hè 2026 - Áo bóng đá', N'PHAN_TRAM', 20.00, 200000.00, '2026-06-01', '2026-08-31', 1, 0);
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (2, N'DGG002', N'Black Friday 2026', N'PHAN_TRAM', 30.00, 500000.00, '2026-11-25', '2026-11-30', 0, 0);
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (3, N'DGG003', N'Xả kho áo cầu lông', N'PHAN_TRAM', 15.00, 150000.00, '2026-07-01', '2026-07-31', 1, 0);
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (4, N'DGG004', N'Ưu đãi áo gym tháng 5', N'TIEN_MAT', 50000.00, 50000.00, '2026-05-01', '2026-05-31', 0, 0);
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (5, N'DGG005', N'Tuần lễ áo chạy bộ', N'PHAN_TRAM', 25.00, 200000.00, '2026-08-01', '2026-08-15', 1, 0);
INSERT INTO [dbo].[dot_giam_gia] ([id], [ma], [ten_dot_giam_gia], [loai_giam_gia_ap_dung], [gia_tri_giam_gia], [so_tien_giam_toi_da], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted]) VALUES (6, N'DGG006', N'Khai trương - Áo bóng rổ', N'TIEN_MAT', 80000.00, 80000.00, '2026-03-01', '2026-03-31', 0, 0);
SET IDENTITY_INSERT [dbo].[dot_giam_gia] OFF;
GO
-- gio_hang: 11 dong
SET IDENTITY_INSERT [dbo].[gio_hang] ON;
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (1, 1, NULL, 1958000.00, '2026-07-20T08:30:00.000', '2026-08-01T09:00:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (2, 2, NULL, 890000.00, '2026-07-22T10:15:00.000', '2026-08-02T11:20:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (3, 5, NULL, 1687000.00, '2026-07-25T14:45:00.000', '2026-08-03T15:10:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (4, 7, NULL, 608000.00, '2026-07-28T09:05:00.000', '2026-08-03T20:00:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (5, 9, NULL, 1508000.00, '2026-07-30T16:20:00.000', '2026-08-04T08:40:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (6, 10, NULL, 848000.00, '2026-08-01T19:50:00.000', '2026-08-04T21:15:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (7, 12, NULL, 1298000.00, '2026-08-02T08:20:00.000', '2026-08-04T10:30:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (8, 14, NULL, 978000.00, '2026-08-02T15:40:00.000', '2026-08-05T07:50:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (9, 16, NULL, 937000.00, '2026-08-03T11:10:00.000', '2026-08-05T08:20:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (10, 19, NULL, 1680000.00, '2026-08-04T20:05:00.000', '2026-08-05T09:15:00.000');
INSERT INTO [dbo].[gio_hang] ([id], [id_khach_hang], [id_hoa_don], [tong_tien], [created_at], [updated_at]) VALUES (12, 21, 27, 0.00, '2026-10-09T03:00:19.507', '2026-10-09T03:00:19.507');
SET IDENTITY_INSERT [dbo].[gio_hang] OFF;
GO
-- gio_hang_chi_tiet: 23 dong
SET IDENTITY_INSERT [dbo].[gio_hang_chi_tiet] ON;
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (1, 1, 1, 1, 790000.00, 790000.00, '2026-07-20T08:30:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (2, 1, 43, 1, 279000.00, 279000.00, '2026-07-21T09:00:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (3, 1, 49, 1, 590000.00, 590000.00, '2026-07-21T09:05:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (4, 1, 17, 1, 299000.00, 299000.00, '2026-08-01T09:00:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (5, 2, 6, 1, 890000.00, 890000.00, '2026-07-22T10:15:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (6, 3, 74, 1, 849000.00, 849000.00, '2026-07-25T14:45:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (7, 3, 90, 1, 539000.00, 539000.00, '2026-07-26T08:00:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (8, 3, 17, 1, 299000.00, 299000.00, '2026-08-03T15:10:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (9, 4, 10, 1, 329000.00, 329000.00, '2026-07-28T09:05:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (10, 4, 43, 1, 279000.00, 279000.00, '2026-07-28T09:10:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (11, 5, 36, 1, 859000.00, 859000.00, '2026-07-30T16:20:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (12, 5, 40, 1, 649000.00, 649000.00, '2026-08-04T08:40:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (13, 6, 33, 1, 549000.00, 549000.00, '2026-08-01T19:50:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (14, 6, 17, 1, 299000.00, 299000.00, '2026-08-01T19:55:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (15, 7, 46, 1, 769000.00, 769000.00, '2026-08-02T08:20:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (16, 7, 59, 1, 529000.00, 529000.00, '2026-08-04T10:30:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (17, 8, 83, 1, 489000.00, 489000.00, '2026-08-02T15:40:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (18, 8, 84, 1, 489000.00, 489000.00, '2026-08-05T07:50:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (19, 9, 86, 1, 359000.00, 359000.00, '2026-08-03T11:10:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (20, 9, 43, 1, 279000.00, 279000.00, '2026-08-03T11:15:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (21, 9, 17, 1, 299000.00, 299000.00, '2026-08-05T08:20:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (22, 10, 23, 1, 990000.00, 990000.00, '2026-08-04T20:05:00.000');
INSERT INTO [dbo].[gio_hang_chi_tiet] ([id], [id_gio_hang], [id_chi_tiet_sp], [so_luong], [gia], [thanh_tien], [created_at]) VALUES (23, 10, 26, 1, 690000.00, 690000.00, '2026-08-05T09:15:00.000');
SET IDENTITY_INSERT [dbo].[gio_hang_chi_tiet] OFF;
GO
-- hinh_thuc_thanh_toan: 27 dong
SET IDENTITY_INSERT [dbo].[hinh_thuc_thanh_toan] ON;
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (1, 1, 3, 1510000.00, 0.00, N'HTTT001', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (2, 2, 1, 0.00, 890000.00, N'HTTT002', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (3, 3, 2, 809000.00, 0.00, N'HTTT003', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (4, 4, 4, 0.00, 1310000.00, N'HTTT004', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (5, 5, 3, 1515000.00, 0.00, N'HTTT005', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (6, 6, 1, 0.00, 690000.00, N'HTTT006', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (7, 7, 4, 0.00, 0.00, N'HTTT007', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (8, 8, 3, 1578000.00, 0.00, N'HTTT008', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (9, 9, 4, 0.00, 798000.00, N'HTTT009', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (10, 10, 1, 0.00, 1269000.00, N'HTTT010', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (11, 11, 3, 1268000.00, 0.00, N'HTTT011', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (12, 12, 4, 0.00, 0.00, N'HTTT012', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (13, 13, 2, 2929000.00, 0.00, N'HTTT013', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (14, 14, 5, 744000.00, 0.00, N'HTTT014', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (15, 15, 1, 0.00, 1258000.00, N'HTTT015', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (16, 16, 3, 1648000.00, 0.00, N'HTTT016', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (17, 17, 6, 908000.00, 0.00, N'HTTT017', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (18, 18, 3, 1113000.00, 0.00, N'HTTT018', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (19, 19, 4, 0.00, 1043000.00, N'HTTT019', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (20, 20, 2, 2070000.00, 0.00, N'HTTT020', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (21, 21, 5, 604000.00, 0.00, N'HTTT021', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (22, 22, 4, 0.00, 0.00, N'HTTT022', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (23, 23, 1, 0.00, 558000.00, N'HTTT023', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (24, 24, 3, 1208000.00, 0.00, N'HTTT024', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (25, 25, 4, 0.00, 0.00, N'HTTT025', 0);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (26, 3, 1, 0.00, 0.00, N'HTTT026', 1);
INSERT INTO [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [ma], [deleted]) VALUES (27, 28, 1, 0.00, 709000.00, NULL, 0);
SET IDENTITY_INSERT [dbo].[hinh_thuc_thanh_toan] OFF;
GO
-- hoa_don: 27 dong
SET IDENTITY_INSERT [dbo].[hoa_don] ON;
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (1, 1, 1, 1, N'HD000001', 1580000.00, N'ONLINE', 30000.00, 1610000.00, 1510000.00, N'Giao giờ hành chính', N'Nguyễn Anh Tuấn', N'Số 24 ngõ 165 Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội', N'0912000001', N'tuan.anh@gmail.com', '2026-01-18T09:12:00.000', '2026-01-18 09:20:00.0000000 +07:00', 4, 0, '2026-01-18T09:12:00.000', 1, '2026-01-21T15:00:00.0000000', 1);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (2, 2, 2, NULL, N'HD000002', 890000.00, N'TAI_QUAY', 0.00, 890000.00, 890000.00, N'Khách mua trực tiếp', N'Trần Minh Quân', N'Số 9 ngách 12 Định Công Hạ, Định Công, Hoàng Mai, Hà Nội', N'0912000002', N'quan.minh@gmail.com', '2026-01-25T14:30:00.000', '2026-01-25 14:32:00.0000000 +07:00', 4, 0, '2026-01-25T14:30:00.000', 2, '2026-01-25T14:35:00.0000000', 2);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (3, 3, 1, 7, N'HD000003', 859000.00, N'TAI_QUAY', 0.00, 859000.00, 809000.00, N'Khách thử áo tại cửa hàng', N'Lê Thu Hương', N'Số 145 Nguyễn Đình Chiểu, Võ Thị Sáu, Quận 3, Hồ Chí Minh', N'0912000003', N'huong.thu@gmail.com', '2026-02-05T10:05:00.000', '2026-02-05 10:15:00.0000000 +07:00', 4, 0, '2026-02-05T10:05:00.000', 1, '2026-02-05T10:20:00.0000000', 1);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (4, 4, 3, NULL, N'HD000004', 1280000.00, N'ONLINE', 30000.00, 1310000.00, 1310000.00, N'Gọi trước khi giao', N'Phạm Đức Anh', N'Số 56 Tô Hiệu, Trại Cau, Lê Chân, Hải Phòng', N'0912000004', N'anh.duc@gmail.com', '2026-02-22T16:40:00.000', NULL, 3, 0, '2026-02-22T16:40:00.000', 3, '2026-02-24T08:30:00.0000000', 3);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (5, 5, 2, 3, N'HD000005', 1580000.00, N'ONLINE', 35000.00, 1615000.00, 1515000.00, N'Đóng gói cẩn thận', N'Vũ Ngọc Mai', N'Số 210 Núi Thành, Hoà Cường Bắc, Hải Châu, Đà Nẵng', N'0912000005', N'mai.ngoc@gmail.com', '2026-03-03T11:20:00.000', '2026-03-03 11:25:00.0000000 +07:00', 2, 0, '2026-03-03T11:20:00.000', 2, '2026-03-04T09:00:00.0000000', 2);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (6, 6, 4, NULL, N'HD000006', 690000.00, N'TAI_QUAY', 0.00, 690000.00, 690000.00, NULL, N'Đặng Bảo Long', N'Số 3 ngõ 82 Nguyễn Tuân, Nhân Chính, Thanh Xuân, Hà Nội', N'0912000006', N'long.bao@gmail.com', '2026-03-14T15:50:00.000', '2026-03-14 15:52:00.0000000 +07:00', 4, 0, '2026-03-14T15:50:00.000', 4, '2026-03-14T15:55:00.0000000', 4);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (7, 7, 1, NULL, N'HD000007', 0.00, N'ONLINE', 25000.00, 25000.00, 25000.00, N'Khách hủy do đặt nhầm size', N'Hoàng Phương Thảo', N'Số 18 Nguyễn Thị Thập, Tân Phong, Quận 7, Hồ Chí Minh', N'0912000007', N'thao.phuong@gmail.com', '2026-03-25T08:45:00.000', NULL, 5, 0, '2026-03-25T08:45:00.000', 1, '2026-03-26T10:10:00.0000000', 1);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (8, 8, 3, 4, N'HD000008', 1698000.00, N'ONLINE', 30000.00, 1728000.00, 1578000.00, N'Giao cuối tuần', N'Bùi Hoàng Nam', N'Số 77 Nguyễn Văn Cừ, An Khánh, Ninh Kiều, Cần Thơ', N'0912000008', N'nam.hoang@gmail.com', '2026-04-10T13:15:00.000', '2026-04-10 13:22:00.0000000 +07:00', 4, 0, '2026-04-10T13:15:00.000', 3, '2026-04-14T17:00:00.0000000', 3);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (9, 9, 5, NULL, N'HD000009', 778000.00, N'ONLINE', 20000.00, 798000.00, 798000.00, NULL, N'Ngô Khánh Ly', N'Số 5 ngõ 30 Ngô Gia Tự, Việt Hưng, Long Biên, Hà Nội', N'0912000009', N'ly.khanh@gmail.com', '2026-04-21T09:35:00.000', '2026-04-21 09:40:00.0000000 +07:00', 4, 0, '2026-04-21T09:35:00.000', 5, '2026-04-23T14:20:00.0000000', 5);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (10, 10, 2, NULL, N'HD000010', 1269000.00, N'TAI_QUAY', 0.00, 1269000.00, 1269000.00, N'Khách quen', N'Dương Việt Cường', N'Số 102 Đại lộ Bình Dương, Phú Hoà, Thủ Dầu Một, Bình Dương', N'0912000010', N'cuong.viet@gmail.com', '2026-05-06T10:50:00.000', '2026-05-06 10:53:00.0000000 +07:00', 4, 0, '2026-05-06T10:50:00.000', 2, '2026-05-06T11:00:00.0000000', 2);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (11, 1, 4, 3, N'HD000011', 1338000.00, N'ONLINE', 30000.00, 1368000.00, 1268000.00, N'Giao ngoài giờ hành chính', N'Nguyễn Anh Tuấn', N'Toà S2 Vinhomes Smart City, Mỹ Đình 1, Nam Từ Liêm, Hà Nội', N'0912000001', N'tuan.anh@gmail.com', '2026-06-12T19:05:00.000', '2026-06-12 19:10:00.0000000 +07:00', 3, 0, '2026-06-12T19:05:00.000', 4, '2026-06-14T08:15:00.0000000', 4);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (12, 3, 5, NULL, N'HD000012', 1188000.00, N'ONLINE', 25000.00, 1213000.00, 1213000.00, N'Đơn mới, chờ xác nhận', N'Lê Thu Hương', N'Toà Landmark 81, Điện Biên Phủ, Phường 25, Bình Thạnh, Hồ Chí Minh', N'0912000003', N'huong.thu@gmail.com', '2026-07-28T20:30:00.000', NULL, 1, 0, '2026-07-28T20:30:00.000', 5, NULL, NULL);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (13, 11, 6, 8, N'HD000013', 3289000.00, N'ONLINE', 40000.00, 3329000.00, 2929000.00, N'Đặt áo cho cả đội bóng đá', N'Lý Thanh Tùng', N'Số 41 Nguyễn Chí Thanh, Ngọc Khánh, Ba Đình, Hà Nội', N'0912000011', N'tung.thanh@gmail.com', '2026-05-14T10:20:00.000', '2026-05-14 10:30:00.0000000 +07:00', 4, 0, '2026-05-14T10:20:00.000', 6, '2026-05-18T16:00:00.0000000', 6);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (14, 12, 7, 7, N'HD000014', 769000.00, N'ONLINE', 25000.00, 794000.00, 744000.00, N'Áo cầu lông nữ', N'Phan Huyền Nga', N'Số 89 Cộng Hoà, Phường 4, Tân Bình, Hồ Chí Minh', N'0912000012', N'huyen.nga@gmail.com', '2026-05-20T14:15:00.000', '2026-05-20 14:20:00.0000000 +07:00', 4, 0, '2026-05-20T14:15:00.000', 7, '2026-05-23T11:30:00.0000000', 7);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (15, 13, 8, NULL, N'HD000015', 1258000.00, N'TAI_QUAY', 0.00, 1258000.00, 1258000.00, N'Mua áo bóng chuyền tại quầy', N'Trương Quốc Huy', N'Số 132 Điện Biên Phủ, Vĩnh Trung, Thanh Khê, Đà Nẵng', N'0912000013', N'huy.quoc@gmail.com', '2026-06-02T09:40:00.000', '2026-06-02 09:45:00.0000000 +07:00', 4, 0, '2026-06-02T09:40:00.000', 8, '2026-06-02T09:50:00.0000000', 8);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (16, 14, 5, 9, N'HD000016', 1698000.00, N'ONLINE', 30000.00, 1728000.00, 1648000.00, N'Combo áo tập gym', N'Cao Thùy Dương', N'Số 7 Nguyễn Khuyến, Văn Quán, Hà Đông, Hà Nội', N'0912000014', N'duong.thuy@gmail.com', '2026-06-15T16:25:00.000', '2026-06-15 16:30:00.0000000 +07:00', 4, 0, '2026-06-15T16:25:00.000', 5, '2026-06-19T10:15:00.0000000', 5);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (17, 15, 2, NULL, N'HD000017', 878000.00, N'ONLINE', 30000.00, 908000.00, 908000.00, N'Áo bơi mùa hè', N'Hà Trung Kiên', N'Số 33 Lê Hồng Phong, Hưng Bình, Vinh, Nghệ An', N'0912000015', N'kien.trung@gmail.com', '2026-06-24T11:05:00.000', '2026-06-24 11:12:00.0000000 +07:00', 4, 0, '2026-06-24T11:05:00.000', 2, '2026-06-28T15:40:00.0000000', 2);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (18, 16, 6, 1, N'HD000018', 1188000.00, N'ONLINE', 25000.00, 1213000.00, 1113000.00, N'Khách hàng mới', N'Đinh Lan Anh', N'Số 202 Ba Tháng Hai, Phường 12, Quận 10, Hồ Chí Minh', N'0912000016', N'anh.lan@gmail.com', '2026-07-02T13:50:00.000', '2026-07-02 13:55:00.0000000 +07:00', 4, 0, '2026-07-02T13:50:00.000', 6, '2026-07-06T09:20:00.0000000', 6);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (19, 17, 7, NULL, N'HD000019', 1018000.00, N'ONLINE', 25000.00, 1043000.00, 1043000.00, N'Áo chạy bộ nữ', N'Mai Gia Linh', N'Số 18 Võ Chí Công, Xuân La, Tây Hồ, Hà Nội', N'0912000017', N'linh.gia@gmail.com', '2026-07-09T10:30:00.000', '2026-07-09 10:35:00.0000000 +07:00', 3, 0, '2026-07-09T10:30:00.000', 7, '2026-07-11T08:00:00.0000000', 7);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (20, 18, 8, 2, N'HD000020', 2270000.00, N'TAI_QUAY', 0.00, 2270000.00, 2070000.00, N'Mua nhiều áo cho CLB', N'Tạ Đăng Khoa', N'Số 55 Nguyễn Đăng Đạo, Suối Hoa, Bắc Ninh', N'0912000018', N'khoa.dang@gmail.com', '2026-07-16T15:10:00.000', '2026-07-16 15:20:00.0000000 +07:00', 4, 0, '2026-07-16T15:10:00.000', 8, '2026-07-16T15:30:00.0000000', 8);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (21, 19, 1, 7, N'HD000021', 619000.00, N'ONLINE', 35000.00, 654000.00, 604000.00, N'Áo đạp xe', N'Chu Mỹ Tâm', N'Số 96 Trần Phú, Lộc Thọ, Nha Trang, Khánh Hoà', N'0912000019', N'tam.my@gmail.com', '2026-07-23T09:45:00.000', '2026-07-23 09:50:00.0000000 +07:00', 2, 0, '2026-07-23T09:45:00.000', 1, '2026-07-25T14:00:00.0000000', 1);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (22, 20, 3, NULL, N'HD000022', 789000.00, N'ONLINE', 30000.00, 819000.00, 819000.00, N'Áo tennis và polo', N'Đoàn Hữu Bình', N'Số 12 Võ Văn Ngân, Linh Trung, Thủ Đức, Hồ Chí Minh', N'0912000020', N'binh.huu@gmail.com', '2026-07-30T17:20:00.000', NULL, 1, 0, '2026-07-30T17:20:00.000', 3, NULL, NULL);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (23, 5, 4, NULL, N'HD000023', 558000.00, N'TAI_QUAY', 0.00, 558000.00, 558000.00, N'Mua thêm áo cầu lông', N'Vũ Ngọc Mai', N'Số 210 Núi Thành, Hoà Cường Bắc, Hải Châu, Đà Nẵng', N'0912000005', N'mai.ngoc@gmail.com', '2026-08-01T10:15:00.000', '2026-08-01 10:18:00.0000000 +07:00', 10, 0, '2026-08-01T10:15:00.000', 4, '2026-10-09T10:14:27.6880000', 4);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (24, 8, 6, NULL, N'HD000024', 1178000.00, N'ONLINE', 30000.00, 1208000.00, 1208000.00, N'Áo bóng rổ cho con', N'Bùi Hoàng Nam', N'Số 77 Nguyễn Văn Cừ, An Khánh, Ninh Kiều, Cần Thơ', N'0912000008', N'nam.hoang@gmail.com', '2026-08-03T14:40:00.000', '2026-08-03 14:45:00.0000000 +07:00', 2, 0, '2026-08-03T14:40:00.000', 6, '2026-08-04T09:00:00.0000000', 6);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (25, 13, 7, NULL, N'HD000025', 849000.00, N'ONLINE', 25000.00, 874000.00, 874000.00, N'Đơn mới nhất', N'Trương Quốc Huy', N'Số 132 Điện Biên Phủ, Vĩnh Trung, Thanh Khê, Đà Nẵng', N'0912000013', N'huy.quoc@gmail.com', '2026-08-05T08:50:00.000', NULL, 5, 0, '2026-08-05T08:50:00.000', 7, '2026-10-09T10:07:37.8440000', NULL);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (27, 21, 1, NULL, N'HD000027', 0.00, N'Tại quầy', 0.00, 0.00, 0.00, NULL, N'Khách lẻ', N'', N'', NULL, '2026-10-09T03:00:18.857', NULL, 0, 1, '2026-10-09T03:00:18.857', NULL, NULL, NULL);
INSERT INTO [dbo].[hoa_don] ([id], [id_khach_hang], [id_nhan_vien], [id_phieu_giam_gia], [ma], [tien_san_pham], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [created_at], [created_by], [updated_at], [updated_by]) VALUES (28, 21, 1, 9, N'HD000028', 789000.00, N'Tại quầy', 0.00, 789000.00, 709000.00, NULL, N'Khách lẻ', N'', N'', NULL, '2026-10-09T03:00:33.403', '2026-10-09 03:01:40.4150000 +07:00', 5, 0, '2026-10-09T03:00:33.403', NULL, '2026-10-09T03:01:40.4150000', NULL);
SET IDENTITY_INSERT [dbo].[hoa_don] OFF;
GO
-- hoa_don_chi_tiet: 42 dong
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] ON;
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (1, 1, 1, N'HDCT001', 790000.00, 1, NULL, 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (2, 2, 6, N'HDCT002', 890000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (3, 3, 36, N'HDCT003', 859000.00, 1, N'Khách đã thử vừa người', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (4, 4, 49, N'HDCT004', 590000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (5, 4, 26, N'HDCT005', 690000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (6, 4, 43, N'HDCT006', 279000.00, 0, N'Khách bỏ bớt 1 áo', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (7, 5, 23, N'HDCT007', 990000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (8, 5, 52, N'HDCT008', 590000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (9, 6, 26, N'HDCT009', 690000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (10, 7, 10, N'HDCT010', 329000.00, 0, N'Hủy do khách đặt nhầm size', 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (11, 8, 74, N'HDCT011', 849000.00, 1, NULL, 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (12, 9, 10, N'HDCT012', 329000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (13, 9, 77, N'HDCT013', 449000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (14, 10, 23, N'HDCT014', 990000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (15, 10, 43, N'HDCT015', 279000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (16, 11, 62, N'HDCT016', 789000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (17, 11, 33, N'HDCT017', 549000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (18, 12, 40, N'HDCT018', 649000.00, 1, N'Chờ xác nhận số lượng', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (19, 12, 90, N'HDCT019', 539000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (20, 13, 17, N'HDCT020', 299000.00, 1, N'In số áo cho cả đội', 0, 11);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (21, 14, 46, N'HDCT021', 769000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (22, 15, 68, N'HDCT022', 629000.00, 1, NULL, 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (23, 16, 74, N'HDCT023', 849000.00, 1, NULL, 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (24, 17, 86, N'HDCT024', 359000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (25, 17, 88, N'HDCT025', 519000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (26, 18, 11, N'HDCT026', 329000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (27, 18, 36, N'HDCT027', 859000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (28, 18, 43, N'HDCT028', 279000.00, 0, N'Đổi sang mẫu khác', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (29, 19, 59, N'HDCT029', 529000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (30, 19, 83, N'HDCT030', 489000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (31, 20, 1, N'HDCT031', 790000.00, 1, NULL, 0, 2);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (32, 20, 26, N'HDCT032', 690000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (33, 20, 36, N'HDCT033', 859000.00, 0, N'Hết size, hoàn tiền', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (34, 21, 89, N'HDCT034', 619000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (35, 22, 62, N'HDCT035', 789000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (36, 22, 90, N'HDCT036', 539000.00, 0, N'Chờ khách xác nhận màu', 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (37, 23, 43, N'HDCT037', 279000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (38, 23, 44, N'HDCT038', 279000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (39, 24, 30, N'HDCT039', 629000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (40, 24, 33, N'HDCT040', 549000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (41, 25, 74, N'HDCT041', 849000.00, 1, NULL, 0, 1);
INSERT INTO [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_sp], [ma], [gia], [trang_thai], [ghi_chu], [deleted], [so_luong]) VALUES (42, 28, 64, NULL, 789000.00, 1, NULL, 0, 1);
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] OFF;
GO
-- khach_hang: 21 dong
SET IDENTITY_INSERT [dbo].[khach_hang] ON;
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (1, 9, N'KH001', N'Nguyễn Anh Tuấn', 1, '1999-01-15T00:00:00.0000000', 0, '2025-06-01T09:00:00.0000000', 1, '2026-01-12T10:00:00.0000000', 2, N'001099112233', 1);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (2, 10, N'KH002', N'Trần Minh Quân', 1, '2000-04-08T00:00:00.0000000', 0, '2025-06-14T10:20:00.0000000', 1, '2026-01-20T11:30:00.0000000', 2, N'001200223344', 3);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (3, 11, N'KH003', N'Lê Thu Hương', 0, '1997-12-25T00:00:00.0000000', 0, '2025-07-03T14:00:00.0000000', 1, '2026-02-02T09:10:00.0000000', 3, N'001197334455', 4);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (4, 12, N'KH004', N'Phạm Đức Anh', 1, '1996-08-19T00:00:00.0000000', 0, '2025-07-25T16:40:00.0000000', 1, '2026-02-18T15:25:00.0000000', 2, N'001196445566', 6);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (5, 13, N'KH005', N'Vũ Ngọc Mai', 0, '2001-02-11T00:00:00.0000000', 0, '2025-08-09T08:55:00.0000000', 1, '2026-03-01T08:40:00.0000000', 3, N'001201556677', 7);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (6, 14, N'KH006', N'Đặng Bảo Long', 1, '1994-06-30T00:00:00.0000000', 0, '2025-09-12T11:15:00.0000000', 1, '2026-03-11T13:05:00.0000000', 2, N'001194667788', 8);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (7, 15, N'KH007', N'Hoàng Phương Thảo', 0, '1998-10-05T00:00:00.0000000', 0, '2025-10-01T13:30:00.0000000', 1, '2026-03-22T17:00:00.0000000', 5, N'001198778899', 9);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (8, 16, N'KH008', N'Bùi Hoàng Nam', 1, '1992-03-27T00:00:00.0000000', 0, '2025-11-19T15:45:00.0000000', 1, '2026-04-08T10:35:00.0000000', 2, N'001192889900', 10);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (9, 17, N'KH009', N'Ngô Khánh Ly', 0, '2002-07-14T00:00:00.0000000', 0, '2026-01-06T09:25:00.0000000', 1, '2026-04-19T12:15:00.0000000', 5, N'001202990011', 11);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (10, 18, N'KH010', N'Dương Việt Cường', 1, '1995-11-23T00:00:00.0000000', 0, '2026-02-20T10:05:00.0000000', 1, '2026-05-02T14:50:00.0000000', 3, N'001195001122', 12);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (11, 19, N'KH011', N'Lý Thanh Tùng', 1, '1993-09-02T00:00:00.0000000', 0, '2026-02-25T08:30:00.0000000', 1, '2026-05-12T09:00:00.0000000', 2, N'001193112244', 13);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (12, 20, N'KH012', N'Phan Huyền Nga', 0, '2000-12-18T00:00:00.0000000', 0, '2026-03-02T11:40:00.0000000', 1, '2026-05-18T16:20:00.0000000', 5, N'001200223355', 14);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (13, 21, N'KH013', N'Trương Quốc Huy', 1, '1991-05-09T00:00:00.0000000', 0, '2026-03-15T14:10:00.0000000', 1, '2026-06-01T10:45:00.0000000', 3, N'001191334466', 15);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (14, 22, N'KH014', N'Cao Thùy Dương', 0, '1999-08-21T00:00:00.0000000', 0, '2026-04-01T09:50:00.0000000', 1, '2026-06-10T13:30:00.0000000', 7, N'001199445577', 17);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (15, 23, N'KH015', N'Hà Trung Kiên', 1, '1997-02-03T00:00:00.0000000', 0, '2026-04-18T16:00:00.0000000', 1, '2026-06-22T08:50:00.0000000', 2, N'001197556688', 18);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (16, 24, N'KH016', N'Đinh Lan Anh', 0, '2003-04-27T00:00:00.0000000', 0, '2026-05-05T10:30:00.0000000', 1, '2026-07-01T15:10:00.0000000', 7, N'001203667799', 19);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (17, 25, N'KH017', N'Mai Gia Linh', 0, '2001-10-11T00:00:00.0000000', 0, '2026-05-22T13:15:00.0000000', 1, '2026-07-08T11:00:00.0000000', 5, N'001201778800', 20);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (18, 26, N'KH018', N'Tạ Đăng Khoa', 1, '1990-07-07T00:00:00.0000000', 0, '2026-06-09T08:40:00.0000000', 1, '2026-07-15T14:25:00.0000000', 3, N'001190889911', 21);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (19, 27, N'KH019', N'Chu Mỹ Tâm', 0, '1998-03-30T00:00:00.0000000', 0, '2026-06-28T15:20:00.0000000', 1, '2026-07-22T09:40:00.0000000', 7, N'001198990022', 22);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (20, 28, N'KH020', N'Đoàn Hữu Bình', 1, '1994-11-16T00:00:00.0000000', 0, '2026-07-14T09:10:00.0000000', 1, '2026-07-30T17:30:00.0000000', 2, N'001194001133', 23);
INSERT INTO [dbo].[khach_hang] ([id], [id_tai_khoan], [ma], [ten], [gioi_tinh], [ngay_sinh], [deleted], [created_at], [created_by], [updated_at], [updated_by], [cccd], [id_dia_chi_khach_hang]) VALUES (21, NULL, N'KHACH_LE', N'Khách lẻ', NULL, NULL, 1, '2026-10-08T19:56:49.6590940', NULL, NULL, NULL, NULL, NULL);
SET IDENTITY_INSERT [dbo].[khach_hang] OFF;
GO
-- kich_co: 7 dong
SET IDENTITY_INSERT [dbo].[kich_co] ON;
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (1, N'XS', N'KC001', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (2, N'S', N'KC002', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (3, N'M', N'KC003', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (4, N'L', N'KC004', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (5, N'XL', N'KC005', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (6, N'XXL', N'KC006', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
INSERT INTO [dbo].[kich_co] ([id], [ten_kich_co], [ma_kich_co], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (7, N'3XL', N'KC007', '2025-01-10T08:30:00.000', '2025-01-10T08:30:00.000', 0, NULL);
SET IDENTITY_INSERT [dbo].[kich_co] OFF;
GO
-- lich_su_hoa_don: 65 dong
SET IDENTITY_INSERT [dbo].[lich_su_hoa_don] ON;
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (1, 1, 1, N'LSHD001', N'Tạo hóa đơn', '2026-01-18 09:12:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (2, 1, 1, N'LSHD002', N'Xác nhận thanh toán', '2026-01-18 09:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (3, 1, 1, N'LSHD003', N'Bàn giao đơn vị vận chuyển', '2026-01-19 08:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (4, 1, 1, N'LSHD004', N'Giao hàng thành công', '2026-01-21 15:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (5, 2, 2, N'LSHD005', N'Tạo hóa đơn tại quầy', '2026-01-25 14:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (6, 2, 2, N'LSHD006', N'Thanh toán tiền mặt', '2026-01-25 14:32:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (7, 2, 2, N'LSHD007', N'Hoàn thành', '2026-01-25 14:35:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (8, 3, 1, N'LSHD008', N'Tạo hóa đơn tại quầy', '2026-02-05 10:05:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (9, 3, 1, N'LSHD009', N'Áp dụng phiếu AOTHETHAO50', '2026-02-05 10:10:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (10, 3, 1, N'LSHD010', N'Hoàn thành', '2026-02-05 10:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (11, 4, 3, N'LSHD011', N'Tạo hóa đơn', '2026-02-22 16:40:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (12, 4, 3, N'LSHD012', N'Xác nhận đơn hàng', '2026-02-23 09:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (13, 4, 3, N'LSHD013', N'Đang giao hàng', '2026-02-24 08:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (14, 5, 2, N'LSHD014', N'Tạo hóa đơn', '2026-03-03 11:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (15, 5, 2, N'LSHD015', N'Xác nhận thanh toán', '2026-03-03 11:25:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (16, 5, 2, N'LSHD016', N'Chờ giao hàng', '2026-03-04 09:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (17, 6, 4, N'LSHD017', N'Tạo hóa đơn tại quầy', '2026-03-14 15:50:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (18, 6, 4, N'LSHD018', N'Hoàn thành', '2026-03-14 15:55:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (19, 7, 1, N'LSHD019', N'Tạo hóa đơn', '2026-03-25 08:45:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (20, 7, 1, N'LSHD020', N'Hủy đơn theo yêu cầu khách', '2026-03-26 10:10:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (21, 8, 3, N'LSHD021', N'Tạo hóa đơn', '2026-04-10 13:15:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (22, 8, 3, N'LSHD022', N'Xác nhận thanh toán', '2026-04-10 13:22:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (23, 8, 3, N'LSHD023', N'Giao hàng thành công', '2026-04-14 17:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (24, 9, 5, N'LSHD024', N'Tạo hóa đơn', '2026-04-21 09:35:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (25, 9, 5, N'LSHD025', N'Giao hàng thành công', '2026-04-23 14:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (26, 10, 2, N'LSHD026', N'Tạo hóa đơn tại quầy', '2026-05-06 10:50:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (27, 10, 2, N'LSHD027', N'Hoàn thành', '2026-05-06 11:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (28, 11, 4, N'LSHD028', N'Tạo hóa đơn', '2026-06-12 19:05:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (29, 11, 4, N'LSHD029', N'Đang giao hàng', '2026-06-14 08:15:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (30, 12, 5, N'LSHD030', N'Tạo hóa đơn', '2026-07-28 20:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (31, 13, 6, N'LSHD031', N'Tạo hóa đơn', '2026-05-14 10:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (32, 13, 6, N'LSHD032', N'Áp dụng phiếu TEAM20', '2026-05-14 10:25:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (33, 13, 6, N'LSHD033', N'In số áo cho đội', '2026-05-15 09:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (34, 13, 6, N'LSHD034', N'Giao hàng thành công', '2026-05-18 16:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (35, 14, 7, N'LSHD035', N'Tạo hóa đơn', '2026-05-20 14:15:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (36, 14, 7, N'LSHD036', N'Giao hàng thành công', '2026-05-23 11:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (37, 15, 8, N'LSHD037', N'Tạo hóa đơn tại quầy', '2026-06-02 09:40:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (38, 15, 8, N'LSHD038', N'Hoàn thành', '2026-06-02 09:50:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (39, 16, 5, N'LSHD039', N'Tạo hóa đơn', '2026-06-15 16:25:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (40, 16, 5, N'LSHD040', N'Xác nhận thanh toán', '2026-06-15 16:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (41, 16, 5, N'LSHD041', N'Giao hàng thành công', '2026-06-19 10:15:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (42, 17, 2, N'LSHD042', N'Tạo hóa đơn', '2026-06-24 11:05:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (43, 17, 2, N'LSHD043', N'Giao hàng thành công', '2026-06-28 15:40:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (44, 18, 6, N'LSHD044', N'Tạo hóa đơn', '2026-07-02 13:50:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (45, 18, 6, N'LSHD045', N'Áp dụng phiếu WELCOME10', '2026-07-02 13:52:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (46, 18, 6, N'LSHD046', N'Giao hàng thành công', '2026-07-06 09:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (47, 19, 7, N'LSHD047', N'Tạo hóa đơn', '2026-07-09 10:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (48, 19, 7, N'LSHD048', N'Đang giao hàng', '2026-07-11 08:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (49, 20, 8, N'LSHD049', N'Tạo hóa đơn tại quầy', '2026-07-16 15:10:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (50, 20, 8, N'LSHD050', N'Hoàn thành', '2026-07-16 15:30:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (51, 21, 1, N'LSHD051', N'Tạo hóa đơn', '2026-07-23 09:45:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (52, 21, 1, N'LSHD052', N'Chờ giao hàng', '2026-07-25 14:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (53, 22, 3, N'LSHD053', N'Tạo hóa đơn', '2026-07-30 17:20:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (54, 23, 4, N'LSHD054', N'Tạo hóa đơn tại quầy', '2026-08-01 10:15:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (55, 23, 4, N'LSHD055', N'Hoàn thành', '2026-08-01 10:25:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (56, 24, 6, N'LSHD056', N'Tạo hóa đơn', '2026-08-03 14:40:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (57, 24, 6, N'LSHD057', N'Chờ giao hàng', '2026-08-04 09:00:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (58, 25, 7, N'LSHD058', N'Tạo hóa đơn', '2026-08-05 08:50:00.0000000 +07:00', 0, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (59, 28, 1, N'LSHD_1791489700588', N'Thanh toán tại quầy (Tiền mặt) - Tự động xác nhận và hoàn thành, đã trừ tồn kho', '2026-10-09 03:01:40.5880000 +07:00', 1, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (60, 25, 1, N'LSHD_1791515237142', N'Cập nhật trạng thái: Đã xác nhận - Đã trừ tồn kho', '2026-10-09 10:07:17.1460000 +07:00', 1, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (61, 25, 1, N'LSHD_1791515243784', N'Cập nhật trạng thái: Chờ vận chuyển', '2026-10-09 10:07:23.7840000 +07:00', 1, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (62, 25, 1, N'LSHD_1791515248368', N'Cập nhật trạng thái: Đang vận chuyển', '2026-10-09 10:07:28.3680000 +07:00', 1, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (63, 25, 1, N'LSHD_1791515257844', N'Cập nhật trạng thái: Đã hoàn thành', '2026-10-09 10:07:37.8440000 +07:00', 1, NULL);
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (64, 23, 1, N'LSHD_1791515650156', N'Giao hàng thất bại', '2026-10-09 10:14:10.1650000 +07:00', 1, N'Lý do: Khach khong nhan hang');
INSERT INTO [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [id_nhan_vien], [ma], [hanh_dong], [thoi_gian], [deleted], [ghi_chu]) VALUES (65, 23, 1, N'LSHD_1791515667693', N'Hàng giao thất bại đã về kho - Đã cộng lại tồn kho', '2026-10-09 10:14:27.6930000 +07:00', 1, NULL);
SET IDENTITY_INSERT [dbo].[lich_su_hoa_don] OFF;
GO
-- mau_sac: 12 dong
SET IDENTITY_INSERT [dbo].[mau_sac] ON;
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (1, N'Đen', N'MS001', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (2, N'Trắng', N'MS002', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (3, N'Đỏ', N'MS003', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (4, N'Xanh dương', N'MS004', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (5, N'Xanh lá', N'MS005', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (6, N'Vàng', N'MS006', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (7, N'Cam', N'MS007', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (8, N'Xám', N'MS008', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (9, N'Tím', N'MS009', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (10, N'Hồng', N'MS010', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (11, N'Xanh navy', N'MS011', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
INSERT INTO [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau_sac], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (12, N'Xanh ngọc', N'MS012', '2025-01-10T08:25:00.000', '2025-01-10T08:25:00.000', 0, NULL);
SET IDENTITY_INSERT [dbo].[mau_sac] OFF;
GO
-- nhan_vien: 8 dong
SET IDENTITY_INSERT [dbo].[nhan_vien] ON;
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (1, 1, N'NV001', N'Nguyễn Văn Sơn', '1990-03-12T00:00:00.0000000', N'/images/nhan-vien/nv001.jpg', N'Quản trị hệ thống', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'Số 12 ngõ 68 Trần Thái Tông', N'001090001234', 0, '2025-01-05 08:00:00.0000000 +07:00', 1, '2026-01-10 09:15:00.0000000 +07:00', 1, 1);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (2, 2, N'NV002', N'Trần Minh Hoàng', '1995-07-24T00:00:00.0000000', N'/images/nhan-vien/nv002.jpg', N'Nhân viên bán hàng', N'Hà Nội', N'Hai Bà Trưng', N'Bách Khoa', N'Số 45 Lê Thanh Nghị', N'001095004321', 0, '2025-02-11 08:30:00.0000000 +07:00', 1, '2026-02-01 10:00:00.0000000 +07:00', 1, 1);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (3, 3, N'NV003', N'Lê Thùy Linh', '1998-11-02T00:00:00.0000000', N'/images/nhan-vien/nv003.jpg', N'Nhân viên kho', N'Hà Nội', N'Đống Đa', N'Khương Thượng', N'Số 7 ngách 21 Tôn Thất Tùng', N'001198007654', 0, '2025-03-20 09:00:00.0000000 +07:00', 1, '2026-02-14 14:20:00.0000000 +07:00', 2, 0);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (4, 4, N'NV004', N'Phạm Anh Tuấn', '1993-05-18T00:00:00.0000000', N'/images/nhan-vien/nv004.jpg', N'Nhân viên tư vấn', N'Hồ Chí Minh', N'Quận 1', N'Bến Nghé', N'Số 88 Nguyễn Huệ', N'079093001122', 0, '2025-05-02 08:15:00.0000000 +07:00', 1, '2026-03-05 11:45:00.0000000 +07:00', 2, 1);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (5, 5, N'NV005', N'Đỗ Ngọc Mai', '1999-09-09T00:00:00.0000000', N'/images/nhan-vien/nv005.jpg', N'Nhân viên bán hàng', N'Đà Nẵng', N'Hải Châu', N'Thanh Bình', N'Số 30 Nguyễn Tất Thành', N'048199009988', 0, '2025-08-16 08:45:00.0000000 +07:00', 1, '2026-04-01 16:30:00.0000000 +07:00', 2, 0);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (6, 6, N'NV006', N'Vũ Hồng Phúc', '1996-01-27T00:00:00.0000000', N'/images/nhan-vien/nv006.jpg', N'Nhân viên in ấn áo đấu', N'Hà Nội', N'Bắc Từ Liêm', N'Cổ Nhuế 1', N'Số 15 Phạm Văn Đồng', N'001096112233', 0, '2025-09-08 08:00:00.0000000 +07:00', 1, '2026-04-15 09:30:00.0000000 +07:00', 1, 1);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (7, 7, N'NV007', N'Ngô Thu Hiền', '2000-06-14T00:00:00.0000000', N'/images/nhan-vien/nv007.jpg', N'Nhân viên CSKH', N'Hồ Chí Minh', N'Gò Vấp', N'Phường 10', N'Số 250 Quang Trung', N'079200334455', 0, '2025-11-03 09:20:00.0000000 +07:00', 1, '2026-05-10 13:40:00.0000000 +07:00', 1, 0);
INSERT INTO [dbo].[nhan_vien] ([id], [id_tai_khoan], [ma], [ten_nhan_vien], [ngay_sinh], [anh_nhan_vien], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [cccd], [deleted], [created_at], [created_by], [updated_at], [updated_by], [gioi_tinh]) VALUES (8, 8, N'NV008', N'Trịnh Đăng Khoa', '1997-04-05T00:00:00.0000000', N'/images/nhan-vien/nv008.jpg', N'Nhân viên kho', N'Hải Phòng', N'Ngô Quyền', N'Máy Tơ', N'Số 68 Lê Lợi', N'031097556677', 0, '2026-01-12 08:10:00.0000000 +07:00', 1, '2026-05-20 15:00:00.0000000 +07:00', 1, 1);
SET IDENTITY_INSERT [dbo].[nhan_vien] OFF;
GO
-- phieu_giam_gia: 10 dong
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] ON;
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (1, N'WELCOME10', N'Chào mừng khách hàng mới', N'PHAN_TRAM', 10, 100000, 500000, 100, '2026-01-01T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0, N'Giảm 10% cho đơn hàng đầu tiên, tối đa 100.000đ', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (2, N'SALE200K', N'Giảm 200K cho đơn lớn', N'TIEN_MAT', 0, 200000, 2000000, 50, '2026-03-01T00:00:00.0000000', '2026-09-30T23:59:59.0000000', 0, 0, N'Giảm ngay 200.000đ cho đơn từ 2 triệu', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (3, N'FREESHIP100', N'Hỗ trợ phí vận chuyển', N'TIEN_MAT', 0, 100000, 800000, 200, '2026-02-01T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0, N'Hỗ trợ tối đa 100.000đ phí vận chuyển', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (4, N'SUMMER10', N'Ưu đãi hè 2026', N'PHAN_TRAM', 10, 150000, 1000000, 150, '2026-06-01T00:00:00.0000000', '2026-08-31T23:59:59.0000000', 0, 0, N'Giảm 10% mùa hè, tối đa 150.000đ', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (5, N'VIP100K', N'Ưu đãi khách hàng thân thiết', N'TIEN_MAT', 0, 100000, 500000, 30, '2026-01-15T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 1, N'Phiếu riêng tư cho khách hàng thân thiết', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (6, N'NEWYEAR15', N'Mừng năm mới 2026', N'PHAN_TRAM', 15, 300000, 1500000, 0, '2026-01-01T00:00:00.0000000', '2026-02-15T23:59:59.0000000', 0, 0, N'Chương trình đã kết thúc', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (7, N'AOTHETHAO50', N'Giảm 50K mọi áo thể thao', N'TIEN_MAT', 0, 50000, 300000, 300, '2026-04-01T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0, N'Giảm 50.000đ cho đơn áo thể thao từ 300.000đ', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (8, N'TEAM20', N'Ưu đãi mua áo theo đội', N'PHAN_TRAM', 20, 400000, 3000000, 40, '2026-05-01T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0, N'Giảm 20% khi mua áo số lượng lớn cho cả đội', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (9, N'GYMFIT80', N'Ưu đãi áo tập gym', N'TIEN_MAT', 0, 80000, 600000, 79, '2026-06-01T00:00:00.0000000', '2026-10-31T23:59:59.0000000', 1, 0, N'Giảm 80.000đ cho đơn áo gym từ 600.000đ', 0);
INSERT INTO [dbo].[phieu_giam_gia] ([id], [ma], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [phan_tram_giam_gia], [so_tien_giam_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [rieng_tu], [mo_ta], [deleted]) VALUES (10, N'BIRTHDAY15', N'Quà sinh nhật khách hàng', N'PHAN_TRAM', 15, 200000, 500000, 60, '2026-01-01T00:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 1, N'Phiếu riêng tư tặng dịp sinh nhật khách hàng', 0);
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] OFF;
GO
-- phieu_giam_gia_ca_nhan: 16 dong
SET IDENTITY_INSERT [dbo].[phieu_giam_gia_ca_nhan] ON;
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (1, 5, 1, N'PGGCN001', '2026-01-15T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (2, 5, 2, N'PGGCN002', '2026-01-15T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (3, 5, 3, N'PGGCN003', '2026-01-15T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 0, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (4, 5, 4, N'PGGCN004', '2026-02-01T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (5, 5, 6, N'PGGCN005', '2026-02-01T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (6, 1, 9, N'PGGCN006', '2026-01-06T09:30:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (7, 1, 10, N'PGGCN007', '2026-02-20T10:10:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (8, 2, 8, N'PGGCN008', '2026-03-01T08:00:00.0000000', '2026-09-30T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (9, 10, 11, N'PGGCN009', '2026-03-05T08:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (10, 10, 13, N'PGGCN010', '2026-03-20T08:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (11, 5, 15, N'PGGCN011', '2026-04-20T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (12, 1, 16, N'PGGCN012', '2026-05-05T10:35:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (13, 1, 17, N'PGGCN013', '2026-05-22T13:20:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (14, 10, 18, N'PGGCN014', '2026-06-09T09:00:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (15, 1, 19, N'PGGCN015', '2026-06-28T15:30:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
INSERT INTO [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_phieu_giam_gia], [id_khach_hang], [ma], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted]) VALUES (16, 1, 20, N'PGGCN016', '2026-07-14T09:20:00.0000000', '2026-12-31T23:59:59.0000000', 1, 0);
SET IDENTITY_INSERT [dbo].[phieu_giam_gia_ca_nhan] OFF;
GO
-- phuong_thuc_thanh_toan: 6 dong
SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] ON;
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (1, N'PTTT001', N'Tiền mặt', 0);
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (2, N'PTTT002', N'Chuyển khoản', 0);
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (3, N'PTTT003', N'VNPay', 0);
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (4, N'PTTT004', N'COD - Thanh toán khi nhận hàng', 0);
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (5, N'PTTT005', N'Momo', 0);
INSERT INTO [dbo].[phuong_thuc_thanh_toan] ([id], [ma], [kieu_thanh_toan], [deleted]) VALUES (6, N'PTTT006', N'ZaloPay', 0);
SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] OFF;
GO
-- quyen_han: 3 dong
SET IDENTITY_INSERT [dbo].[quyen_han] ON;
INSERT INTO [dbo].[quyen_han] ([id], [ma], [cap_quyen_han], [deleted]) VALUES (1, N'ADMIN', 1, 0);
INSERT INTO [dbo].[quyen_han] ([id], [ma], [cap_quyen_han], [deleted]) VALUES (2, N'NHAN_VIEN', 2, 0);
INSERT INTO [dbo].[quyen_han] ([id], [ma], [cap_quyen_han], [deleted]) VALUES (3, N'KHACH_HANG', 3, 0);
SET IDENTITY_INSERT [dbo].[quyen_han] OFF;
GO
-- san_pham: 30 dong
SET IDENTITY_INSERT [dbo].[san_pham] ON;
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (1, 1, 1, N'SP001', N'Áo bóng đá Nike Dri-FIT Academy 23', N'Áo bóng đá nam chất polyester thoáng khí, công nghệ Dri-FIT thấm hút mồ hôi vượt trội, form vừa vặn cho vận động cường độ cao.', 1, '2025-02-01T09:10:00.000', '2026-01-05T10:00:00.000', 0, 1, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (2, 1, 2, N'SP002', N'Áo bóng đá Adidas Tiro 24 Jersey', N'Áo đấu Adidas Tiro 24 công nghệ AEROREADY giữ cơ thể khô ráo, ba sọc kinh điển bên tay áo, cổ bo ôm gọn.', 1, '2025-02-01T09:10:00.000', '2026-01-05T10:00:00.000', 0, 2, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (3, 1, 6, N'SP003', N'Áo bóng đá Kamito Aurora', N'Áo bóng đá không logo CLB, vải mè co giãn nhẹ, giá tốt cho đội phong trào, in số và tên dễ dàng.', 2, '2025-02-01T09:10:00.000', '2026-01-05T10:00:00.000', 0, 3, 5);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (4, 1, 7, N'SP004', N'Áo bóng đá Jogarbola Classic', N'Áo đấu phong cách cổ điển, vải mè cao cấp thoáng mát, đường may chắc chắn phù hợp thi đấu sân phủi.', 2, '2025-02-01T09:10:00.000', '2026-01-06T10:00:00.000', 0, 4, 5);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (5, 1, 8, N'SP005', N'Áo bóng đá Grand Sport League', N'Áo bóng đá thi đấu chuẩn giải phong trào, chất vải nhẹ, thấm hút nhanh, có sẵn nhiều màu cho cả đội.', 2, '2025-02-02T09:10:00.000', '2026-01-06T10:00:00.000', 0, 5, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (6, 1, 3, N'SP006', N'Áo thủ môn Puma Goalkeeper Pro', N'Áo thủ môn dài tay, có đệm mút mỏng ở khuỷu tay, công nghệ dryCELL thoát ẩm, form rộng thoải mái khi đổ người.', 2, '2025-02-03T09:10:00.000', '2026-01-10T10:00:00.000', 0, 6, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (7, 1, 5, N'SP007', N'Áo bóng đá Mizuno Pro Jersey', N'Áo đấu cao cấp của Mizuno, vải siêu nhẹ, cổ tròn co giãn, thiết kế tối giản chuẩn Nhật Bản.', 1, '2025-02-04T09:10:00.000', '2026-01-12T10:00:00.000', 0, 30, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (8, 2, 1, N'SP008', N'Áo bóng rổ Nike Icon Jersey', N'Áo ba lỗ bóng rổ, nách khoét rộng thoải mái vung tay, vải lưới hai lớp thoáng khí, form suông chuẩn sân bóng.', 2, '2025-02-05T09:10:00.000', '2026-01-15T10:00:00.000', 0, 7, 6);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (9, 2, 2, N'SP009', N'Áo bóng rổ Adidas Legends Tank', N'Áo bóng rổ tank top form rộng, công nghệ AEROREADY, logo in cao su nổi bền màu sau nhiều lần giặt.', 2, '2025-02-05T09:10:00.000', '2026-01-15T10:00:00.000', 0, 8, 6);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (10, 2, 4, N'SP010', N'Áo bóng rổ Kappa Court Vest', N'Áo bóng rổ vải thun lạnh mát, co giãn tốt, phối màu tương phản nổi bật, phù hợp giải phong trào.', 2, '2025-02-06T09:10:00.000', '2026-01-18T10:00:00.000', 0, 9, 4);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (11, 3, 9, N'SP011', N'Áo cầu lông Yonex Tour Elite', N'Áo cầu lông chính hãng Yonex, vải siêu nhẹ thoát ẩm nhanh, thiết kế đường cắt tinh tế giúp vung vợt tự do.', 1, '2025-02-07T09:10:00.000', '2026-01-20T10:00:00.000', 0, 10, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (12, 3, 11, N'SP012', N'Áo cầu lông Li-Ning AeroNaut', N'Áo cầu lông công nghệ AT DRY thoát ẩm cực nhanh, phần lưng có dải lưới tăng thông thoáng.', 2, '2025-02-07T09:10:00.000', '2026-01-20T10:00:00.000', 0, 11, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (13, 3, 6, N'SP013', N'Áo cầu lông Kamito Smash', N'Áo cầu lông giá tốt cho người chơi phong trào, vải mè nhẹ, thấm hút ổn, nhiều màu tươi sáng.', 2, '2025-02-08T09:10:00.000', '2026-01-22T10:00:00.000', 0, 12, 5);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (14, 3, 9, N'SP014', N'Áo cầu lông Yonex Lady Fit', N'Áo cầu lông nữ dáng ôm nhẹ, cổ tim thanh lịch, chất vải co giãn 4 chiều thoải mái khi di chuyển.', 0, '2025-02-08T09:10:00.000', '2026-01-22T10:00:00.000', 0, 10, 7);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (15, 4, 1, N'SP015', N'Áo chạy bộ Nike Miler', N'Áo chạy bộ tay ngắn siêu nhẹ, có dải phản quang phía sau tăng an toàn khi chạy tối, Dri-FIT khô nhanh.', 1, '2025-02-09T09:10:00.000', '2026-01-25T10:00:00.000', 0, 13, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (16, 4, 10, N'SP016', N'Áo chạy bộ Under Armour Streaker', N'Áo chạy bộ công nghệ HeatGear làm mát, vải mỏng nhẹ gần như không cảm nhận trọng lượng.', 2, '2025-02-09T09:10:00.000', '2026-01-25T10:00:00.000', 0, 14, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (17, 4, 2, N'SP017', N'Áo chạy bộ Adidas Own The Run', N'Áo chạy bộ AEROREADY, thiết kế đường may phẳng hạn chế cọ xát khi chạy đường dài.', 2, '2025-02-10T09:10:00.000', '2026-01-28T10:00:00.000', 0, 15, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (18, 4, 10, N'SP018', N'Áo chạy bộ nữ Under Armour Tech Tee', N'Áo chạy bộ nữ form suông nhẹ, vải mềm mịn không xù, thoáng khí và nhanh khô sau buổi tập.', 0, '2025-02-10T09:10:00.000', '2026-01-28T10:00:00.000', 0, 14, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (19, 5, 1, N'SP019', N'Áo tennis Nike Court Dri-FIT', N'Áo tennis cổ bẻ, vải Dri-FIT co giãn, phần vai được thiết kế riêng cho động tác giao bóng.', 1, '2025-02-11T09:10:00.000', '2026-02-01T10:00:00.000', 0, 16, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (20, 5, 2, N'SP020', N'Áo tennis Adidas Club Polo', N'Áo polo tennis chất liệu tái chế thân thiện môi trường, cổ áo giữ form tốt sau nhiều lần giặt.', 2, '2025-02-11T09:10:00.000', '2026-02-01T10:00:00.000', 0, 17, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (21, 6, 5, N'SP021', N'Áo bóng chuyền Mizuno Attack', N'Áo bóng chuyền thi đấu, phần vai và nách gia cố chắc chắn, co giãn tốt khi bật nhảy đập bóng.', 2, '2025-02-12T09:10:00.000', '2026-02-05T10:00:00.000', 0, 18, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (22, 6, 8, N'SP022', N'Áo bóng chuyền Grand Sport Spike', N'Áo bóng chuyền phong trào, vải thoáng mát, in số áo rõ nét, giá hợp lý cho cả đội.', 2, '2025-02-12T09:10:00.000', '2026-02-05T10:00:00.000', 0, 19, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (23, 7, 10, N'SP023', N'Áo gym Under Armour Rush Compression', N'Áo body giữ nhiệt ôm sát, hỗ trợ cơ bắp trong lúc tập tạ, công nghệ HeatGear thoát nhiệt hiệu quả.', 1, '2025-02-13T09:10:00.000', '2026-02-10T10:00:00.000', 0, 20, 7);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (24, 7, 1, N'SP024', N'Áo tank top gym Nike Pro', N'Áo ba lỗ tập gym, nách khoét sâu khoe cơ vai, vải co giãn 4 chiều theo mọi chuyển động.', 1, '2025-02-13T09:10:00.000', '2026-02-10T10:00:00.000', 0, 21, 3);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (25, 7, 3, N'SP025', N'Áo tập gym Puma Train Favourite', N'Áo tập tay ngắn công nghệ dryCELL, form regular dễ mặc, phù hợp cả tập gym lẫn mặc thường ngày.', 2, '2025-02-14T09:10:00.000', '2026-02-12T10:00:00.000', 0, 22, 8);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (26, 7, 10, N'SP026', N'Áo bra tập gym nữ Under Armour', N'Áo bra thể thao nữ nâng đỡ mức trung bình, dây lưng bản to không hằn da, thoáng khí khi tập cường độ cao.', 0, '2025-02-14T09:10:00.000', '2026-02-12T10:00:00.000', 0, 20, 7);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (27, 8, 12, N'SP027', N'Áo bơi Decathlon Nabaiji', N'Áo bơi bó sát chống clo, giữ form tốt sau nhiều lần xuống hồ, đường may phẳng không cọ da.', 2, '2025-02-15T09:10:00.000', '2026-02-15T10:00:00.000', 0, 23, 7);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (28, 8, 11, N'SP028', N'Áo bơi chống nắng Li-Ning UV Guard', N'Áo bơi dài tay chống tia UV, thích hợp bơi ngoài trời và các hoạt động dưới nước ban ngày.', 2, '2025-02-15T09:10:00.000', '2026-02-15T10:00:00.000', 0, 24, 7);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (29, 9, 12, N'SP029', N'Áo đạp xe Decathlon Triban', N'Áo đạp xe có ba túi sau lưng đựng đồ, khoá kéo dài, ôm nhẹ giảm cản gió khi đạp tốc độ.', 2, '2025-02-16T09:10:00.000', '2026-02-18T10:00:00.000', 0, 25, 1);
INSERT INTO [dbo].[san_pham] ([id], [id_danh_muc], [id_thuong_hieu], [ma], [ten_san_pham], [mo_ta_san_pham], [gioi_tinh], [ngay_tao], [ngay_cap_nhat], [deleted], [id_anh_san_pham], [id_chat_lieu]) VALUES (30, 10, 2, N'SP030', N'Áo polo thể thao Adidas Primegreen', N'Áo polo thể thao từ sợi tái chế, cổ bẻ lịch sự, mặc đi tập lẫn đi chơi đều hợp.', 2, '2025-02-17T09:10:00.000', '2026-02-20T10:00:00.000', 0, 28, 8);
SET IDENTITY_INSERT [dbo].[san_pham] OFF;
GO
-- tai_khoan: 28 dong
SET IDENTITY_INSERT [dbo].[tai_khoan] ON;
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (1, 1, N'TK001', N'admin', N'admin@striker.vn', N'0901000001', N'$2a$10$McPS6zW0Gku7NAHgqoRryeZcYA02sV0VVDRGj0FTG35Jspv1LsaeO', 1);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (2, 2, N'TK002', N'nvhoang', N'hoang.nv@striker.vn', N'0901000002', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (3, 2, N'TK003', N'nvlinh', N'linh.nv@striker.vn', N'0901000003', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (4, 2, N'TK004', N'nvtuan', N'tuan.nv@striker.vn', N'0901000004', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (5, 2, N'TK005', N'nvmai', N'mai.nv@striker.vn', N'0901000005', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (6, 2, N'TK006', N'nvphuc', N'phuc.nv@striker.vn', N'0901000006', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (7, 2, N'TK007', N'nvhien', N'hien.nv@striker.vn', N'0901000007', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (8, 2, N'TK008', N'nvkhoa', N'khoa.nv@striker.vn', N'0901000008', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (9, 3, N'TK009', N'anhtuan99', N'tuan.anh@gmail.com', N'0912000001', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (10, 3, N'TK010', N'minhquan', N'quan.minh@gmail.com', N'0912000002', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (11, 3, N'TK011', N'thuhuong', N'huong.thu@gmail.com', N'0912000003', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (12, 3, N'TK012', N'ducanh', N'anh.duc@gmail.com', N'0912000004', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (13, 3, N'TK013', N'ngocmai', N'mai.ngoc@gmail.com', N'0912000005', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (14, 3, N'TK014', N'baolong', N'long.bao@gmail.com', N'0912000006', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (15, 3, N'TK015', N'phuongthao', N'thao.phuong@gmail.com', N'0912000007', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (16, 3, N'TK016', N'hoangnam', N'nam.hoang@gmail.com', N'0912000008', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (17, 3, N'TK017', N'khanhly', N'ly.khanh@gmail.com', N'0912000009', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (18, 3, N'TK018', N'vietcuong', N'cuong.viet@gmail.com', N'0912000010', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (19, 3, N'TK019', N'thanhtung', N'tung.thanh@gmail.com', N'0912000011', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (20, 3, N'TK020', N'ngahuyen', N'huyen.nga@gmail.com', N'0912000012', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (21, 3, N'TK021', N'quochuy', N'huy.quoc@gmail.com', N'0912000013', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (22, 3, N'TK022', N'thuyduong', N'duong.thuy@gmail.com', N'0912000014', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (23, 3, N'TK023', N'trungkien', N'kien.trung@gmail.com', N'0912000015', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (24, 3, N'TK024', N'lananh', N'anh.lan@gmail.com', N'0912000016', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (25, 3, N'TK025', N'gialinh', N'linh.gia@gmail.com', N'0912000017', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (26, 3, N'TK026', N'dangkhoa', N'khoa.dang@gmail.com', N'0912000018', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (27, 3, N'TK027', N'mytam', N'tam.my@gmail.com', N'0912000019', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
INSERT INTO [dbo].[tai_khoan] ([id], [id_quyen_han], [ma], [ten_dang_nhap], [email], [so_dien_thoai], [mat_khau], [deleted]) VALUES (28, 3, N'TK028', N'huubinh', N'binh.huu@gmail.com', N'0912000020', N'$2a$10$K7l1c8Rk9zQ4vXqE2sT0eO5nHqbZ1w3sYtC2mJ8pR6uD4aF0gI9jK', 0);
SET IDENTITY_INSERT [dbo].[tai_khoan] OFF;
GO
-- thuong_hieu: 12 dong
SET IDENTITY_INSERT [dbo].[thuong_hieu] ON;
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (1, N'Nike', N'TH001', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (2, N'Adidas', N'TH002', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (3, N'Puma', N'TH003', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (4, N'Kappa', N'TH004', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (5, N'Mizuno', N'TH005', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (6, N'Kamito', N'TH006', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (7, N'Jogarbola', N'TH007', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (8, N'Grand Sport', N'TH008', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (9, N'Yonex', N'TH009', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (10, N'Under Armour', N'TH010', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (11, N'Li-Ning', N'TH011', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
INSERT INTO [dbo].[thuong_hieu] ([id], [ten_thuong_hieu], [ma_thuong_hieu], [ngay_tao], [ngay_cap_nhat], [deleted], [mo_ta]) VALUES (12, N'Decathlon', N'TH012', '2025-01-10T08:05:00.000', '2025-01-10T08:05:00.000', 0, NULL);
SET IDENTITY_INSERT [dbo].[thuong_hieu] OFF;
GO

/* ============================ INDEXES ============================ */
CREATE UNIQUE NONCLUSTERED INDEX [UX_chat_lieu_ten] ON [dbo].[chat_lieu] ([ten_chat_lieu]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_danh_muc_ten] ON [dbo].[danh_muc] ([ten_danh_muc]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_kich_co_ten] ON [dbo].[kich_co] ([ten_kich_co]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_mau_sac_ten] ON [dbo].[mau_sac] ([ten_mau_sac]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_thuong_hieu_ten] ON [dbo].[thuong_hieu] ([ten_thuong_hieu]);
GO

/* ============================ FOREIGN KEYS ============================ */
ALTER TABLE [dbo].[chi_tiet_dot_giam_gia] ADD CONSTRAINT [FK_CTDGG_CTSP] FOREIGN KEY ([id_chi_tiet_sp]) REFERENCES [dbo].[chi_tiet_san_pham] ([id]);
GO
ALTER TABLE [dbo].[chi_tiet_dot_giam_gia] ADD CONSTRAINT [FK_CTDGG_DotGiamGia] FOREIGN KEY ([id_dot_giam_gia]) REFERENCES [dbo].[dot_giam_gia] ([id]);
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD CONSTRAINT [FK_CTSP_AnhSP] FOREIGN KEY ([id_anh_san_pham]) REFERENCES [dbo].[anh_san_pham] ([id]);
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD CONSTRAINT [FK_CTSP_KichCo] FOREIGN KEY ([id_kich_co]) REFERENCES [dbo].[kich_co] ([id]);
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD CONSTRAINT [FK_CTSP_MauSac] FOREIGN KEY ([id_mau_sac]) REFERENCES [dbo].[mau_sac] ([id]);
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD CONSTRAINT [FK_CTSP_SanPham] FOREIGN KEY ([id_san_pham]) REFERENCES [dbo].[san_pham] ([id]);
GO
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD CONSTRAINT [FK_DiaChiKH_KhachHang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[khach_hang] ([id]);
GO
ALTER TABLE [dbo].[gio_hang] ADD CONSTRAINT [FK_GioHang_HoaDon] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[hoa_don] ([id]);
GO
ALTER TABLE [dbo].[gio_hang] ADD CONSTRAINT [FK_GioHang_KhachHang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[khach_hang] ([id]);
GO
ALTER TABLE [dbo].[gio_hang_chi_tiet] ADD CONSTRAINT [FK_GHCT_CTSP] FOREIGN KEY ([id_chi_tiet_sp]) REFERENCES [dbo].[chi_tiet_san_pham] ([id]);
GO
ALTER TABLE [dbo].[gio_hang_chi_tiet] ADD CONSTRAINT [FK_GHCT_GioHang] FOREIGN KEY ([id_gio_hang]) REFERENCES [dbo].[gio_hang] ([id]);
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan] ADD CONSTRAINT [FK_HTTT_HoaDon] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[hoa_don] ([id]);
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan] ADD CONSTRAINT [FK_HTTT_PhuongThuc] FOREIGN KEY ([id_phuong_thuc_thanh_toan]) REFERENCES [dbo].[phuong_thuc_thanh_toan] ([id]);
GO
ALTER TABLE [dbo].[hoa_don] ADD CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[khach_hang] ([id]);
GO
ALTER TABLE [dbo].[hoa_don] ADD CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY ([id_nhan_vien]) REFERENCES [dbo].[nhan_vien] ([id]);
GO
ALTER TABLE [dbo].[hoa_don] ADD CONSTRAINT [FK_HoaDon_PhieuGiamGia] FOREIGN KEY ([id_phieu_giam_gia]) REFERENCES [dbo].[phieu_giam_gia] ([id]);
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD CONSTRAINT [FK_HDCT_CTSP] FOREIGN KEY ([id_chi_tiet_sp]) REFERENCES [dbo].[chi_tiet_san_pham] ([id]);
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD CONSTRAINT [FK_HDCT_HoaDon] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[hoa_don] ([id]);
GO
ALTER TABLE [dbo].[khach_hang] ADD CONSTRAINT [FK_KhachHang_DiaChi] FOREIGN KEY ([id_dia_chi_khach_hang]) REFERENCES [dbo].[dia_chi_khach_hang] ([id]);
GO
ALTER TABLE [dbo].[khach_hang] ADD CONSTRAINT [FK_KhachHang_TaiKhoan] FOREIGN KEY ([id_tai_khoan]) REFERENCES [dbo].[tai_khoan] ([id]);
GO
ALTER TABLE [dbo].[lich_su_hoa_don] ADD CONSTRAINT [FK_LSHD_HoaDon] FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[hoa_don] ([id]);
GO
ALTER TABLE [dbo].[lich_su_hoa_don] ADD CONSTRAINT [FK_LSHD_NhanVien] FOREIGN KEY ([id_nhan_vien]) REFERENCES [dbo].[nhan_vien] ([id]);
GO
ALTER TABLE [dbo].[nhan_vien] ADD CONSTRAINT [FK_NhanVien_TaiKhoan] FOREIGN KEY ([id_tai_khoan]) REFERENCES [dbo].[tai_khoan] ([id]);
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD CONSTRAINT [FK_PGGCN_KhachHang] FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[khach_hang] ([id]);
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD CONSTRAINT [FK_PGGCN_PhieuGiamGia] FOREIGN KEY ([id_phieu_giam_gia]) REFERENCES [dbo].[phieu_giam_gia] ([id]);
GO
ALTER TABLE [dbo].[san_pham] ADD CONSTRAINT [FK_SanPham_AnhSP] FOREIGN KEY ([id_anh_san_pham]) REFERENCES [dbo].[anh_san_pham] ([id]);
GO
ALTER TABLE [dbo].[san_pham] ADD CONSTRAINT [FK_SanPham_ChatLieu] FOREIGN KEY ([id_chat_lieu]) REFERENCES [dbo].[chat_lieu] ([id]);
GO
ALTER TABLE [dbo].[san_pham] ADD CONSTRAINT [FK_SanPham_DanhMuc] FOREIGN KEY ([id_danh_muc]) REFERENCES [dbo].[danh_muc] ([id]);
GO
ALTER TABLE [dbo].[san_pham] ADD CONSTRAINT [FK_SanPham_ThuongHieu] FOREIGN KEY ([id_thuong_hieu]) REFERENCES [dbo].[thuong_hieu] ([id]);
GO
ALTER TABLE [dbo].[tai_khoan] ADD CONSTRAINT [FK_TaiKhoan_QuyenHan] FOREIGN KEY ([id_quyen_han]) REFERENCES [dbo].[quyen_han] ([id]);
GO

PRINT N'>>> Hoan tat tao database Database_STRIKER_Final';
GO
