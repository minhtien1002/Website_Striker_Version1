package com.example.sd_24_striker_be.common;

import java.util.regex.Pattern;

/**
 * Validate thông tin khách hàng dùng chung cho: đăng ký, thanh toán (kể cả khách không đăng
 * nhập), địa chỉ giao hàng, thêm/sửa khách hàng ở trang admin. Mỗi hàm trả về câu báo lỗi,
 * hoặc null nếu hợp lệ.
 */
public final class ThongTinKhachHangValidator {

    private ThongTinKhachHangValidator() {
    }

    /** Chữ cái (có dấu) và khoảng trắng, 2-50 ký tự. */
    private static final Pattern HO_TEN = Pattern.compile("^[\\p{L} ]{2,50}$");
    /** Số điện thoại Việt Nam: đúng 10 chữ số, bắt đầu bằng 0. */
    private static final Pattern SO_DIEN_THOAI = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static String hoTen(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Họ và tên không được để trống";
        }
        if (!HO_TEN.matcher(value.trim()).matches()) {
            return "Họ và tên chỉ gồm chữ cái và khoảng trắng, từ 2 đến 50 ký tự";
        }
        return null;
    }

    public static String soDienThoai(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Số điện thoại không được để trống";
        }
        if (!SO_DIEN_THOAI.matcher(value.trim()).matches()) {
            return "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0";
        }
        return null;
    }

    /** Email bắt buộc. */
    public static String email(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Email không được để trống";
        }
        return emailKhongBatBuoc(value);
    }

    /** Email không bắt buộc, nhưng nếu nhập thì phải đúng định dạng. */
    public static String emailKhongBatBuoc(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (value.trim().length() > 100 || !EMAIL.matcher(value.trim()).matches()) {
            return "Email không đúng định dạng (ví dụ: tenban@gmail.com)";
        }
        return null;
    }

    public static String diaChi(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Địa chỉ không được để trống";
        }
        String v = value.trim();
        if (v.length() < 5 || v.length() > 255) {
            return "Địa chỉ phải từ 5 đến 255 ký tự";
        }
        return null;
    }

    /** Ném IllegalArgumentException với lỗi đầu tiên gặp phải (nếu có). */
    public static void throwIfInvalid(String... loi) {
        for (String l : loi) {
            if (l != null) {
                throw new IllegalArgumentException(l);
            }
        }
    }
}
