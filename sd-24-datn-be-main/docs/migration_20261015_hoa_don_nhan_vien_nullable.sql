/* Cho phép giỏ/đơn online tồn tại trước khi nhân viên xác nhận và nhận xử lý. */
IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.hoa_don')
      AND name = N'id_nhan_vien'
      AND is_nullable = 0
)
    ALTER TABLE dbo.hoa_don ALTER COLUMN id_nhan_vien INT NULL;
GO
