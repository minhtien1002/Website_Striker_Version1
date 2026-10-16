USE [Database_STRIKER_Final];
GO

/*
  lich_su_hoa_don.deleted đang được dùng để lưu trạng thái hóa đơn sau thao tác.
  Kiểu BIT cũ làm mọi mã 2-10 bị ép thành 1, nên timeline luôn hiện "Chờ xác nhận".
*/
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

-- Khôi phục những trạng thái có thể xác định chắc chắn từ nội dung lịch sử cũ.
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

SELECT c.name AS column_name, t.name AS data_type
FROM sys.columns c
JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID(N'dbo.lich_su_hoa_don')
  AND c.name = N'deleted';
GO
