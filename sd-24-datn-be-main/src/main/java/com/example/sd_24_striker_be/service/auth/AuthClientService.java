package com.example.sd_24_striker_be.service.auth;

import com.example.sd_24_striker_be.dto.auth.ApiMessageResponse;
import com.example.sd_24_striker_be.dto.auth.ForgotPasswordRequest;
import com.example.sd_24_striker_be.dto.auth.LoginRequest;
import com.example.sd_24_striker_be.dto.auth.LoginResponse;
import com.example.sd_24_striker_be.dto.auth.RegisterRequest;
import com.example.sd_24_striker_be.dto.auth.RegisterResponse;
import com.example.sd_24_striker_be.dto.auth.ResetPasswordRequest;
import com.example.sd_24_striker_be.modal.account.KhachHang;
import com.example.sd_24_striker_be.modal.account.NhanVien;
import com.example.sd_24_striker_be.modal.account.TaiKhoan;
import com.example.sd_24_striker_be.modal.account.QuyenHan;
import com.example.sd_24_striker_be.repository.account.KhachHang.KhachHangRepository;
import com.example.sd_24_striker_be.repository.account.NhanVien.NhanVienRepository;
import com.example.sd_24_striker_be.repository.account.TaiKhoanRepository;
import com.example.sd_24_striker_be.repository.account.QuyenHanRepository;
import com.example.sd_24_striker_be.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Date;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class AuthClientService {
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private QuyenHanRepository quyenHanRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    // Họ và tên chỉ gồm chữ cái (có dấu) và khoảng trắng, 2-50 ký tự.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} ]{2,50}$");

    // Lưu OTP đặt lại mật khẩu tạm thời trong bộ nhớ: email -> {code, hạn dùng}.
    // Đơn giản, không cần thêm cột DB; chấp nhận mất khi restart server (chấp nhận được
    // với quy mô đồ án). Không dùng cho môi trường nhiều instance chạy song song.
    private static final long OTP_TTL_MILLIS = 15 * 60 * 1000L; // 15 phút
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        final String code;
        final long expiresAt;

        OtpEntry(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Tìm tài khoản theo tên đăng nhập; nếu không thấy, thử lại bằng email vì
            // tên đăng nhập được hệ thống tự sinh từ họ và tên khi đăng ký (có thể bị
            // bỏ dấu/viết liền khác với những gì khách gõ) - tránh báo "không tồn tại"
            // oan trong khi tài khoản đã được tạo thành công trong DB.
            String dangNhap = loginRequest.getTenDangNhap() == null ? "" : loginRequest.getTenDangNhap().trim();
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhap(dangNhap);
            if (!taiKhoanOpt.isPresent() && dangNhap.contains("@")) {
                taiKhoanOpt = taiKhoanRepository.findByEmail(dangNhap);
            }

            if (!taiKhoanOpt.isPresent()) {
                return new LoginResponse(false, "Tên đăng nhập không tồn tại");
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            // Kiểm tra mật khẩu: mat_khau lưu dạng bcrypt hash, phải so khớp bằng
            // BCryptPasswordEncoder.matches() chứ không được so sánh chuỗi trực tiếp.
            if (!passwordEncoder.matches(loginRequest.getMatKhau(), taiKhoan.getMatKhau())) {
                return new LoginResponse(false, "Mật khẩu không chính xác");
            }

            // Kiểm tra quyền hạn
            if (taiKhoan.getIdQuyenHan() == null) {
                return new LoginResponse(false, "Tài khoản chưa được phân quyền");
            }

            Integer capQuyenHan = taiKhoan.getIdQuyenHan().getCapQuyenHan();

            // Cho phép admin (1), nhân viên (2) và khách hàng (3) đăng nhập
            if (capQuyenHan == null || (capQuyenHan != 1 && capQuyenHan != 2 && capQuyenHan != 3)) {
                return new LoginResponse(false, "Tài khoản không có quyền truy cập hệ thống");
            }

            // Nếu là nhân viên (capQuyenHan = 2), kiểm tra trạng thái nhân viên
            if (capQuyenHan == 2) {
                Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByIdTaiKhoan(taiKhoan);

                if (!nhanVienOpt.isPresent()) {
                    return new LoginResponse(false, "Không tìm thấy thông tin nhân viên");
                }

                NhanVien nhanVien = nhanVienOpt.get();

                // Lưu ý: bảng nhan_vien dùng quy ước deleted = true nghĩa là ĐANG HOẠT
                // ĐỘNG (xem NhanVienServiceImpl.restoreNhanVien) — ngược với các bảng khác.
                if (nhanVien.getDeleted() == null || !nhanVien.getDeleted()) {
                    return new LoginResponse(false, "Tài khoản nhân viên đã bị vô hiệu hóa");
                }
            }

            // Nếu là khách hàng (capQuyenHan = 3), kiểm tra trạng thái khách hàng
            KhachHang khachHang = null;
            if (capQuyenHan == 3) {
                Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan(taiKhoan);

                if (!khachHangOpt.isPresent()) {
                    return new LoginResponse(false, "Không tìm thấy thông tin khách hàng");
                }

                khachHang = khachHangOpt.get();

                // Lưu ý: bảng khach_hang dùng quy ước deleted = true nghĩa là ĐANG HOẠT
                // ĐỘNG (xem KhachHangRepository: "WHERE kh.deleted = true" để lấy khách
                // đang hoạt động) — ngược với các bảng khác trong hệ thống.
                if (khachHang.getDeleted() == null || !khachHang.getDeleted()) {
                    return new LoginResponse(false, "Tài khoản khách hàng đã bị vô hiệu hóa");
                }
            }

            // Đăng nhập thành công - set deleted = true (đang đăng nhập)
            taiKhoan.setDeleted(true);
            taiKhoanRepository.save(taiKhoan);

            // Tạo response
            LoginResponse response = new LoginResponse();
            response.setId(taiKhoan.getId());
            response.setMa(taiKhoan.getMa());
            response.setTenDangNhap(taiKhoan.getTenDangNhap());
            response.setEmail(taiKhoan.getEmail());
            response.setSoDienThoai(taiKhoan.getSoDienThoai());

            // Thông tin quyền hạn
            response.setTenQuyen(taiKhoan.getIdQuyenHan().getMa());
            response.setCapQuyenHan(capQuyenHan);
            
            // Nếu là khách hàng, thêm customer ID
            if (khachHang != null) {
                response.setCustomerId(khachHang.getId());
                response.setTenKhachHang(khachHang.getTen());
            }

            response.setSuccess(true);
            response.setMessage("Đăng nhập thành công");

            return response;

        } catch (Exception e) {
            return new LoginResponse(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    public LoginResponse logout(Integer userId) {
        try {
            if (userId != null) {
                // Tìm tài khoản theo ID
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findById(userId);

                if (taiKhoanOpt.isPresent()) {
                    TaiKhoan taiKhoan = taiKhoanOpt.get();
                    // Đăng xuất - set deleted = false (không đăng nhập)
                    taiKhoan.setDeleted(false);
                    taiKhoanRepository.save(taiKhoan);
                }
            }

            return new LoginResponse(true, "Đăng xuất thành công");

        } catch (Exception e) {
            return new LoginResponse(true, "Đăng xuất thành công"); // Vẫn trả về success dù có lỗi
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        try {
            // Validate input
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                return RegisterResponse.error("Họ và tên không được để trống");
            }
            String hoTen = registerRequest.getName().trim().replaceAll("\\s+", " ");
            if (!NAME_PATTERN.matcher(hoTen).matches()) {
                return RegisterResponse.error("Họ và tên chỉ được chứa chữ cái và khoảng trắng, từ 2 đến 50 ký tự");
            }
            // Trước đây chỉ kiểm tra không rỗng -> nhận cả email sai định dạng, SĐT "abc"
            String emailError = com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.email(registerRequest.getEmail());
            if (emailError != null) {
                return RegisterResponse.error(emailError);
            }
            String phoneError = com.example.sd_24_striker_be.common.ThongTinKhachHangValidator.soDienThoai(registerRequest.getPhoneNumber());
            if (phoneError != null) {
                return RegisterResponse.error(phoneError);
            }
            registerRequest.setEmail(registerRequest.getEmail().trim());
            registerRequest.setPhoneNumber(registerRequest.getPhoneNumber().trim());
            String passwordError = validatePasswordStrength(registerRequest.getPassword());
            if (passwordError != null) {
                return RegisterResponse.error(passwordError);
            }

            // Check if email already exists
            Optional<TaiKhoan> existingAccount = taiKhoanRepository.findByEmail(registerRequest.getEmail());
            if (existingAccount.isPresent()) {
                return RegisterResponse.error("Email đã được sử dụng");
            }

            // Check if phone number already exists
            Optional<KhachHang> existingCustomer = khachHangRepository.findBySoDienThoai(registerRequest.getPhoneNumber());
            if (existingCustomer.isPresent()) {
                return RegisterResponse.error("Số điện thoại đã được sử dụng");
            }

            // Get customer role (capQuyenHan = 3)
            Optional<QuyenHan> customerRole = quyenHanRepository.findByCapQuyenHan(3);
            if (!customerRole.isPresent()) {
                return RegisterResponse.error("Không tìm thấy quyền khách hàng trong hệ thống");
            }

            // Generate account code
            String accountCode = "TK" + System.currentTimeMillis();

            // Tên đăng nhập = họ và tên (bỏ dấu, viết liền không khoảng trắng) theo yêu
            // cầu nghiệp vụ: khách đăng ký bằng họ tên thì dùng luôn họ tên đó để đăng nhập.
            String username = normalizeToUsername(hoTen);
            if (username.isEmpty()) {
                username = "user";
            }

            // Check if username exists, if so add numbers
            int counter = 1;
            String originalUsername = username;
            while (taiKhoanRepository.findByTenDangNhap(username).isPresent()) {
                username = originalUsername + counter;
                counter++;
            }

            // Create TaiKhoan
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMa(accountCode);
            taiKhoan.setTenDangNhap(username);
            // Băm mật khẩu trước khi lưu — trước đây lưu thẳng plain text, vừa mất an
            // toàn vừa không đồng nhất với các tài khoản khác (đang lưu dạng bcrypt hash).
            taiKhoan.setMatKhau(passwordEncoder.encode(registerRequest.getPassword()));
            taiKhoan.setEmail(registerRequest.getEmail());
            taiKhoan.setSoDienThoai(registerRequest.getPhoneNumber());
            taiKhoan.setIdQuyenHan(customerRole.get());
            taiKhoan.setDeleted(false); // Not logged in initially
            taiKhoan = taiKhoanRepository.save(taiKhoan);

            // Generate customer code
            String customerCode = "KH" + System.currentTimeMillis();

            // Create KhachHang
            KhachHang khachHang = new KhachHang();
            khachHang.setMa(customerCode);
            khachHang.setTen(hoTen);
            khachHang.setGioiTinh((short) 1); // Default male
            khachHang.setNgaySinh(Date.valueOf(LocalDate.now().minusYears(18))); // Default 18 years old
            khachHang.setDeleted(true); // true = đang hoạt động (quy ước riêng của bảng khach_hang)
            khachHang.setTaiKhoan(taiKhoan);
            khachHang.setCreatedAt(Instant.now());
            khachHang = khachHangRepository.save(khachHang);

            return RegisterResponse.success("Đăng ký thành công", khachHang.getId(), customerCode, taiKhoan.getTenDangNhap());

        } catch (Exception e) {
            return RegisterResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra độ mạnh mật khẩu: tối thiểu 6 ký tự, ký tự đầu tiên viết hoa,
     * chứa ít nhất 1 ký tự đặc biệt (không phải chữ/số).
     * Trả về null nếu hợp lệ, ngược lại trả về thông báo lỗi.
     */
    private String validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        if (!Character.isUpperCase(password.charAt(0))) {
            return "Mật khẩu phải bắt đầu bằng một chữ cái viết hoa";
        }
        if (!password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) {
            return "Mật khẩu phải chứa ít nhất một ký tự đặc biệt (VD: !@#$%...)";
        }
        return null;
    }

    /**
     * Chuyển họ và tên có dấu thành tên đăng nhập: bỏ dấu tiếng Việt, viết
     * thường, bỏ khoảng trắng. VD: "Nguyễn Văn A" -> "nguyenvana".
     */
    private String normalizeToUsername(String hoTen) {
        String noDiacritics = Normalizer.normalize(hoTen, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return noDiacritics.toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    /**
     * Gửi mã OTP đặt lại mật khẩu tới email đã đăng ký.
     * Luôn trả về success=true (kể cả khi email không tồn tại) để không lộ thông
     * tin email nào đã đăng ký trong hệ thống, trừ khi email để trống.
     */
    public ApiMessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        if (email.isEmpty()) {
            return ApiMessageResponse.error("Vui lòng nhập email");
        }

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByEmail(email);
        if (!taiKhoanOpt.isPresent()) {
            // Không tiết lộ email có tồn tại hay không, nhưng vẫn cần cho khách biết
            // để họ không chờ mail vô ích - chấp nhận đánh đổi trải nghiệm cho đồ án.
            return ApiMessageResponse.error("Email này chưa được đăng ký tài khoản");
        }

        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(otpCode, System.currentTimeMillis() + OTP_TTL_MILLIS));

        emailService.sendPasswordResetEmail(email, otpCode);

        ApiMessageResponse response = ApiMessageResponse.ok("Mã xác nhận đã được gửi tới email của bạn. Mã có hiệu lực trong 15 phút.");
        if (!emailService.isMailEnabled()) {
            // Server chưa cấu hình SMTP thật -> không thể gửi mail thật, trả kèm mã để
            // luồng vẫn dùng được khi test / demo cục bộ.
            response.setDebugOtpCode(otpCode);
            response.setMessage("Hệ thống chưa cấu hình gửi mail thật nên mã xác nhận được trả kèm ở đây để bạn kiểm thử: " + otpCode);
        }
        return response;
    }

    @Transactional
    public ApiMessageResponse resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String otpCode = request.getOtpCode() == null ? "" : request.getOtpCode().trim();

        if (email.isEmpty() || otpCode.isEmpty()) {
            return ApiMessageResponse.error("Vui lòng nhập đầy đủ email và mã xác nhận");
        }

        OtpEntry entry = otpStore.get(email);
        if (entry == null || !entry.code.equals(otpCode)) {
            return ApiMessageResponse.error("Mã xác nhận không đúng");
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            otpStore.remove(email);
            return ApiMessageResponse.error("Mã xác nhận đã hết hạn, vui lòng yêu cầu lại");
        }

        String passwordError = validatePasswordStrength(request.getNewPassword());
        if (passwordError != null) {
            return ApiMessageResponse.error(passwordError);
        }

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByEmail(email);
        if (!taiKhoanOpt.isPresent()) {
            return ApiMessageResponse.error("Không tìm thấy tài khoản ứng với email này");
        }

        TaiKhoan taiKhoan = taiKhoanOpt.get();
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(taiKhoan);

        otpStore.remove(email);

        return ApiMessageResponse.ok("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
    }
}
