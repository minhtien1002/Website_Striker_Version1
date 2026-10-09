package com.example.sd_24_striker_be.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Dùng cho cột kiểu datetimeoffset (hoa_don.ngay_thanh_toan, lich_su_hoa_don.thoi_gian).
 *
 * mssql-jdbc ghi java.sql.Timestamp vào datetimeoffset bằng giờ địa phương nhưng gắn nhầm
 * múi "+00:00" (vd 20:36 giờ VN thành "20:36 +00:00"), nên khi đọc lại bị lệch +7 tiếng, và
 * mỗi lần entity được lưu lại thì giá trị lại trôi thêm 7 tiếng. Chuyển sang OffsetDateTime
 * trước khi ghi để driver lưu đúng múi giờ; phía Java vẫn dùng Timestamp như cũ.
 */
@Converter
public class TimestampOffsetConverter implements AttributeConverter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime convertToDatabaseColumn(Timestamp attribute) {
        return attribute == null ? null : attribute.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    @Override
    public Timestamp convertToEntityAttribute(OffsetDateTime dbData) {
        return dbData == null ? null : Timestamp.from(dbData.toInstant());
    }
}
