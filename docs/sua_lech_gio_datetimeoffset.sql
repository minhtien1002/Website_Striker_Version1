-- Sua lech gio o 2 cot datetimeoffset: hoa_don.ngay_thanh_toan, lich_su_hoa_don.thoi_gian.
-- Nguyen nhan: mssql-jdbc ghi java.sql.Timestamp vao datetimeoffset bang gio VN nhung gan
-- mui "+00:00" => hien thi lech +7 tieng, va moi lan luu lai hoa don thi ngay_thanh_toan
-- troi them 7 tieng. Code da sua bang TimestampOffsetConverter; script nay sua du lieu cu.
-- Chi dong vao cac dong dang co mui +00:00 nen chay lai nhieu lan van an toan.
USE Database_STRIKER_Final;
GO

-- Lich su chi ghi 1 lan => gio dung, chi sai nhan mui gio
UPDATE dbo.lich_su_hoa_don
SET thoi_gian = TODATETIMEOFFSET(CAST(thoi_gian AS datetime2), '+07:00')
WHERE DATEPART(TZOFFSET, thoi_gian) = 0;
GO

-- ngay_thanh_toan: bo phan troi (boi so cua 7 tieng) neu phan du < 10 phut, roi gan +07:00
UPDATE dbo.hoa_don
SET ngay_thanh_toan = TODATETIMEOFFSET(
        DATEADD(HOUR, -7 * CASE
            WHEN d.diff_s > 0 AND d.diff_s % 25200 < 600 THEN d.diff_s / 25200
            ELSE 0 END,
        CAST(ngay_thanh_toan AS datetime2)), '+07:00')
FROM dbo.hoa_don
CROSS APPLY (SELECT DATEDIFF(SECOND, CAST(ngay_tao AS datetime2), CAST(ngay_thanh_toan AS datetime2)) AS diff_s) d
WHERE DATEPART(TZOFFSET, ngay_thanh_toan) = 0;
GO
