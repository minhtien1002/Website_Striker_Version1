package com.example.sd_24_striker_be.service.sale.impl.PhieuGiamGia;

import com.example.sd_24_striker_be.dto.sale.request.PhieuGiamGia.PhieuGiamGiaDTO;
import com.example.sd_24_striker_be.dto.sale.response.PhieuGiamGia.PhieuGiamGiaDetailResponse;
import com.example.sd_24_striker_be.modal.account.KhachHang;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.sale.PhieuGiamGia;
import com.example.sd_24_striker_be.modal.sale.PhieuGiamGiaCaNhan;
import com.example.sd_24_striker_be.repository.account.KhachHang.KhachHangRepository;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.sale.PhieuGiamGia.PhieuGiamGiaCaNhanRepository;
import com.example.sd_24_striker_be.repository.sale.PhieuGiamGia.PhieuGiamGiaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaServices {
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    private final HoaDonRepository hoaDonRepository;

    public PhieuGiamGiaServices(PhieuGiamGiaRepository phieuGiamGiaRepository, KhachHangRepository khachHangRepository, PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository, HoaDonRepository hoaDonRepository) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.khachHangRepository = khachHangRepository;
        this.phieuGiamGiaCaNhanRepository = phieuGiamGiaCaNhanRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    /**
     * Convert date string (YYYY-MM-DD) to Instant at start of day
     */
    private Instant convertDateStringToInstant(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateString);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD, got: " + dateString);
        }
    }

    /**
     * Sinh mã phiếu giảm giá duy nhất dạng "PGGxxxxxx" khi admin không tự nhập mã.
     */
    private String generateUniqueMa() {
        String candidate;
        do {
            candidate = "PGG" + String.format("%06d", (int) (Math.random() * 1_000_000));
        } while (phieuGiamGiaRepository.existsByMaAndDeletedFalse(candidate));
        return candidate;
    }

    public List<PhieuGiamGia> getall() {
        Instant now = Instant.now();
        List<PhieuGiamGia> list = phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        list.forEach(pgg -> {
            if (pgg.getNgayKetThuc() != null && pgg.getNgayKetThuc().isBefore(now)) {
                if (Boolean.TRUE.equals(pgg.getTrangThai())) {
                    pgg.setTrangThai(false);
                    phieuGiamGiaRepository.save(pgg);
                }
            }
        });
        return list;
    }


    /**
     * Kiểm tra dữ liệu phiếu giảm giá. Trước đây backend nhận cả tên rỗng, giảm 150%, số
     * lượng âm, ngày kết thúc trước ngày bắt đầu (chỉ form admin kiểm tra).
     */
    private void validateDto(PhieuGiamGiaDTO dto, Instant ngayBatDau, Instant ngayKetThuc) {
        if (dto.getTenPhieuGiamGia() == null || dto.getTenPhieuGiamGia().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phiếu giảm giá không được để trống");
        }
        String loai = dto.getLoaiPhieuGiamGia();
        if ("PHAN_TRAM".equalsIgnoreCase(loai)) {
            if (dto.getPhanTramGiamGia() == null || dto.getPhanTramGiamGia() <= 0 || dto.getPhanTramGiamGia() > 100) {
                throw new IllegalArgumentException("Phần trăm giảm phải từ 1 đến 100");
            }
            if (dto.getSoTienGiamToiDa() == null || dto.getSoTienGiamToiDa() <= 0) {
                throw new IllegalArgumentException("Số tiền giảm tối đa phải lớn hơn 0");
            }
        } else if ("SO_TIEN_CO_DINH".equalsIgnoreCase(loai) || "TIEN_MAT".equalsIgnoreCase(loai)) {
            Double soTien = dto.getSoTienGiamToiDa() != null ? dto.getSoTienGiamToiDa() : dto.getPhanTramGiamGia();
            if (soTien == null || soTien <= 0) {
                throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0");
            }
            if (dto.getHoaDonToiThieu() != null && dto.getHoaDonToiThieu() > 0 && soTien >= dto.getHoaDonToiThieu()) {
                throw new IllegalArgumentException("Số tiền giảm phải nhỏ hơn giá trị đơn hàng tối thiểu");
            }
        } else {
            throw new IllegalArgumentException("Loại phiếu giảm giá không hợp lệ");
        }
        if (dto.getSoTienGiamToiDa() != null && dto.getSoTienGiamToiDa() < 0) {
            throw new IllegalArgumentException("Số tiền giảm tối đa không được âm");
        }
        if (dto.getHoaDonToiThieu() != null && dto.getHoaDonToiThieu() < 0) {
            throw new IllegalArgumentException("Giá trị đơn tối thiểu không được âm");
        }
        if (dto.getSoLuongDung() == null || dto.getSoLuongDung() < 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 0");
        }
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    public PhieuGiamGia add(PhieuGiamGiaDTO dto) {
        Instant now = Instant.now();
        Instant todayStart = now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Convert date strings to Instant
        Instant ngayBatDauInstant = convertDateStringToInstant(dto.getNgayBatDau());
        Instant ngayKetThucInstant = convertDateStringToInstant(dto.getNgayKetThuc());
        validateDto(dto, ngayBatDauInstant, ngayKetThucInstant);

        // validate ngày - allow today's date
        if (ngayBatDauInstant != null && ngayBatDauInstant.isBefore(todayStart)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được nhỏ hơn ngày hiện tại");
        }

        // 1️⃣ Tạo phiếu giảm giá chung
        // LƯU Ý: trước đây không gán "ma" (mã phiếu) khi tạo mới -> mọi phiếu giảm giá mới
        // tạo đều có ma=NULL trong DB. Khách hàng nhập mã ở trang thanh toán được so khớp
        // bằng voucher.getMa() (xem VoucherClientController) nên KHÔNG BAO GIỜ khớp được với
        // các phiếu mới tạo -> đây chính là nguyên nhân "mã giảm giá mới thêm không chọn/áp
        // dụng được". Nếu admin có nhập mã thì dùng mã đó (sau khi kiểm tra trùng), ngược lại
        // tự sinh một mã duy nhất.
        String ma = dto.getMa() != null ? dto.getMa().trim().toUpperCase() : "";
        if (!ma.isEmpty()) {
            if (phieuGiamGiaRepository.existsByMaAndDeletedFalse(ma)) {
                throw new IllegalArgumentException("Mã phiếu giảm giá \"" + ma + "\" đã tồn tại");
            }
        } else {
            ma = generateUniqueMa();
        }

        PhieuGiamGia pgg = PhieuGiamGia.builder()
                .ma(ma)
                .tenPhieuGiamGia(dto.getTenPhieuGiamGia())
                .loaiPhieuGiamGia(dto.getLoaiPhieuGiamGia())
                .phanTramGiamGia(dto.getPhanTramGiamGia())
                .soTienGiamToiDa(dto.getSoTienGiamToiDa())
                .hoaDonToiThieu(dto.getHoaDonToiThieu())
                .soLuongDung(dto.getSoLuongDung())
                .ngayBatDau(ngayBatDauInstant)
                .ngayKetThuc(ngayKetThucInstant)
                .riengTu(dto.getRiengTu())
                .moTa(dto.getMoTa())
                .deleted(false)
                .trangThai(true)
                .build();

        PhieuGiamGia saved = phieuGiamGiaRepository.save(pgg);

        // 2️⃣ Nếu có danh sách khách hàng → tạo phiếu cá nhân
        if (dto.getKhachHangIds() != null && !dto.getKhachHangIds().isEmpty()) {
            for (Integer khId : dto.getKhachHangIds()) {
                KhachHang khachHang = khachHangRepository.findById(khId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng id = " + khId));

                PhieuGiamGiaCaNhan caNhan = PhieuGiamGiaCaNhan.builder()
                        .idPhieuGiamGia(saved)
                        .idKhachHang(khachHang)
                        .ma("PGG-" + saved.getId() + "-KH" + khachHang.getId()) // tạo mã riêng VD: PGG-1-KH001
                        .ngayNhan(now)
                        .ngayHetHan(saved.getNgayKetThuc())
                        .trangThai(true)
                        .deleted(false)
                        .build();

                phieuGiamGiaCaNhanRepository.save(caNhan);
            }
        }

        return saved;
    }

    // Method to get a single coupon by ID
    public PhieuGiamGia getById(Long id) {
        return phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá với ID: " + id));
    }

    // Method to update an existing coupon
    public PhieuGiamGia update(Long id, PhieuGiamGiaDTO dto) {
        PhieuGiamGia existingPgg = getById(id);
        Instant now = Instant.now();
        
        // Convert date strings to Instant
        Instant ngayBatDauInstant = convertDateStringToInstant(dto.getNgayBatDau());
        Instant ngayKetThucInstant = convertDateStringToInstant(dto.getNgayKetThuc());

        // For update operations, we don't validate past dates since we might be editing existing vouchers
        validateDto(dto, ngayBatDauInstant, ngayKetThucInstant);

        // Update fields
        existingPgg.setTenPhieuGiamGia(dto.getTenPhieuGiamGia());
        existingPgg.setLoaiPhieuGiamGia(dto.getLoaiPhieuGiamGia());
        existingPgg.setPhanTramGiamGia(dto.getPhanTramGiamGia());
        existingPgg.setSoTienGiamToiDa(dto.getSoTienGiamToiDa());
        existingPgg.setHoaDonToiThieu(dto.getHoaDonToiThieu());
        existingPgg.setSoLuongDung(dto.getSoLuongDung());
        existingPgg.setNgayBatDau(ngayBatDauInstant);
        existingPgg.setNgayKetThuc(ngayKetThucInstant);
        existingPgg.setRiengTu(dto.getRiengTu());
        existingPgg.setMoTa(dto.getMoTa());
        existingPgg.setTrangThai(ngayKetThucInstant != null && ngayKetThucInstant.isBefore(now) ? false : true);

        PhieuGiamGia saved = phieuGiamGiaRepository.save(existingPgg);

        // ✅ Nếu riêng tư = false thì xóa hết PGG cá nhân có cùng id
        if (Boolean.FALSE.equals(dto.getRiengTu())) {
            phieuGiamGiaCaNhanRepository.deleteAllByPhieuGiamGiaId(saved.getId());
        }

        return saved;
    }

    public PhieuGiamGia delete(Integer id) {
        PhieuGiamGia existing = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá id = " + id));
        existing.setTrangThai(!Boolean.TRUE.equals(existing.getTrangThai()));
        return phieuGiamGiaRepository.save(existing);
    }

    public PhieuGiamGiaDetailResponse getDetail(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy PGG"));

        PhieuGiamGiaDetailResponse dto = PhieuGiamGiaDetailResponse.builder()
                .id(pgg.getId())
                .ma(pgg.getMa())
                .tenPhieuGiamGia(pgg.getTenPhieuGiamGia())
                .loaiPhieuGiamGia(pgg.getLoaiPhieuGiamGia())
                .phanTramGiamGia(pgg.getPhanTramGiamGia())
                .soTienGiamToiDa(pgg.getSoTienGiamToiDa())
                .hoaDonToiThieu(pgg.getHoaDonToiThieu())
                .soLuongDung(pgg.getSoLuongDung())
                .ngayBatDau(pgg.getNgayBatDau() != null
                        ? pgg.getNgayBatDau().atZone(ZoneId.systemDefault()).toLocalDate()
                        : null)
                .ngayKetThuc(pgg.getNgayKetThuc() != null
                        ? pgg.getNgayKetThuc().atZone(ZoneId.systemDefault()).toLocalDate()
                        : null)
                .trangThai(pgg.getTrangThai())
                .riengTu(pgg.getRiengTu())
                .moTa(pgg.getMoTa())
                .build();


        // Nếu là PGG cá nhân (riengTu = true) thì load thêm dữ liệu
        if (Boolean.TRUE.equals(pgg.getRiengTu())) {
            List<PhieuGiamGiaCaNhan> caNhanList =
                    phieuGiamGiaCaNhanRepository.findAllByIdPhieuGiamGiaId(pgg.getId());

            List<PhieuGiamGiaDetailResponse.CustomerDetail> customers = caNhanList.stream()
                    .map(cn -> {
                        KhachHang kh = cn.getIdKhachHang();
                        return new PhieuGiamGiaDetailResponse.CustomerDetail(
                                kh.getId(),
                                kh.getTaiKhoan().getEmail(),
                                kh.getTaiKhoan().getSoDienThoai()
                        );
                    })
                    // loại bỏ trùng theo idKhachHang
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(
                                    PhieuGiamGiaDetailResponse.CustomerDetail::getIdKhachHang,
                                    c -> c,
                                    (c1, c2) -> c1 // nếu trùng thì giữ 1 cái
                            ),
                            m -> new ArrayList(m.values())
                    ));
            dto.setCustomers(customers);
        } else {
            dto.setCustomers(Collections.emptyList());
        }
        return dto;
    }

    public void toggleCustomer(Integer pggId, Integer khId) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(pggId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        KhachHang kh = khachHangRepository.findById(khId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Kiểm tra KH đã có trong PGG chưa
        Optional<PhieuGiamGiaCaNhan> existing = phieuGiamGiaCaNhanRepository
                .findByIdPhieuGiamGiaAndIdKhachHang(pgg, kh);

        if (existing.isPresent()) {
            // Nếu đã tồn tại thì xóa
            phieuGiamGiaCaNhanRepository.delete(existing.get());
        } else {
            // Nếu chưa có thì thêm mới
            PhieuGiamGiaCaNhan newPggCn = PhieuGiamGiaCaNhan.builder()
                    .idPhieuGiamGia(pgg)
                    .idKhachHang(kh)
                    .ma("PGG-" + pgg.getId() + "-KH" + kh.getId())
                    .ngayNhan(Instant.now())
                    .ngayHetHan(pgg.getNgayKetThuc())
                    .trangThai(true)
                    .deleted(false)
                    .build();
            phieuGiamGiaCaNhanRepository.save(newPggCn);
        }
    }

    // Lấy tất cả phiếu giảm giá còn hoạt động (công khai và cá nhân cho khách hàng)
    public List<PhieuGiamGia> getActiveVouchersForCustomer(Integer customerId) {
        List<PhieuGiamGia> activeVouchers = new ArrayList<>();
        Instant now = Instant.now();
        
        // 1. Lấy phiếu giảm giá công khai còn hoạt động
        List<PhieuGiamGia> publicVouchers = phieuGiamGiaRepository.findActivePublicVouchers(now);
        activeVouchers.addAll(publicVouchers);
        
        // 2. Nếu có customerId, lấy thêm phiếu giảm giá cá nhân
        if (customerId != null) {
            List<PhieuGiamGiaCaNhan> personalVouchers = phieuGiamGiaCaNhanRepository
                    .findActivePersonalVouchers(customerId, now);
            
            for (PhieuGiamGiaCaNhan pggCaNhan : personalVouchers) {
                PhieuGiamGia pgg = pggCaNhan.getIdPhieuGiamGia();
                if (!activeVouchers.contains(pgg)) {
                    activeVouchers.add(pgg);
                }
            }
        }
        
        return activeVouchers;
    }

    // Lấy phiếu giảm giá công khai còn hoạt động
    public List<PhieuGiamGia> getActivePublicVouchers() {
        Instant now = Instant.now();
        return phieuGiamGiaRepository.findActivePublicVouchers(now);
    }

    /**
     * Gắn phiếu giảm giá vào hóa đơn NHÁP (chưa đặt). KHÔNG trừ lượt dùng ở đây: lượt chỉ bị
     * trừ đúng 1 lần lúc đơn được đặt/thanh toán (truLuotSuDung). Trước đây bán tại quầy trừ
     * lượt ngay khi áp (gỡ ra thì cộng lại), còn trang khách không gọi API này nên mua hàng
     * online không hề trừ lượt.
     */
    public HoaDon applyVoucherToInvoice(Integer invoiceId, Integer voucherId) {
        HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Chỉ áp dụng phiếu giảm giá cho hóa đơn chưa thanh toán");
        }

        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá với ID: " + voucherId));

        validateVoucherForApplication(phieuGiamGia);
        if (phieuGiamGia.getSoLuongDung() != null && phieuGiamGia.getSoLuongDung() <= 0) {
            throw new RuntimeException("Phiếu giảm giá đã hết lượt sử dụng");
        }
        kiemTraQuyenSuDung(phieuGiamGia, hoaDon.getIdKhachHang());

        hoaDon.setIdPhieuGiamGia(phieuGiamGia);
        return hoaDonRepository.save(hoaDon);
    }

    /**
     * Gỡ phiếu giảm giá khỏi hóa đơn NHÁP. Đơn đã đặt/thanh toán thì không gỡ được và lượt
     * dùng đã trừ không bao giờ được hoàn lại.
     */
    public HoaDon removeVoucherFromInvoice(Integer invoiceId) {
        HoaDon hoaDon = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (hoaDon.getIdPhieuGiamGia() == null) {
            throw new RuntimeException("Hóa đơn không có phiếu giảm giá để xóa");
        }
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 0) {
            throw new RuntimeException("Đơn hàng đã được đặt, không thể gỡ phiếu giảm giá");
        }

        hoaDon.setIdPhieuGiamGia(null);
        return hoaDonRepository.save(hoaDon);
    }

    /**
     * Trừ 1 lượt dùng khi đơn có phiếu giảm giá được đặt/thanh toán (gọi trong transaction
     * thanh toán - nếu thanh toán lỗi thì rollback luôn cả việc trừ lượt). Lượt đã trừ không
     * hoàn lại, kể cả khi đơn bị hủy sau đó.
     */
    /**
     * Phiếu riêng tư chỉ dùng được bởi khách đã được tặng phiếu (có dòng phieu_giam_gia_ca_nhan).
     * Trước đây chỉ API kiểm tra mã xét điều này, còn lúc áp/thanh toán thì không -> khách
     * khác (hoặc khách lẻ) vẫn dùng được phiếu riêng tư nếu biết id.
     */
    public void kiemTraQuyenSuDung(PhieuGiamGia phieuGiamGia, KhachHang khachHang) {
        if (phieuGiamGia == null || !Boolean.TRUE.equals(phieuGiamGia.getRiengTu())) {
            return;
        }
        boolean duocTang = khachHang != null && phieuGiamGiaCaNhanRepository
                .findByIdPhieuGiamGiaAndIdKhachHang(phieuGiamGia, khachHang)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .isPresent();
        if (!duocTang) {
            throw new RuntimeException("Phiếu giảm giá " + phieuGiamGia.getMa() + " là phiếu riêng, khách hàng này không được sử dụng");
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void truLuotSuDung(PhieuGiamGia phieuGiamGia) {
        if (phieuGiamGia == null || phieuGiamGia.getSoLuongDung() == null) {
            return; // không giới hạn lượt
        }
        if (phieuGiamGiaRepository.truMotLuotSuDung(phieuGiamGia.getId()) == 0) {
            throw new RuntimeException("Phiếu giảm giá " + phieuGiamGia.getMa() + " đã hết lượt sử dụng");
        }
    }

    /**
     * Kiểm tra tính hợp lệ của phiếu giảm giá khi áp dụng
     */
    private void validateVoucherForApplication(PhieuGiamGia phieuGiamGia) {
        Instant now = Instant.now();

        // Kiểm tra trạng thái
        if (phieuGiamGia.getTrangThai() == null || !phieuGiamGia.getTrangThai()) {
            throw new RuntimeException("Phiếu giảm giá không hoạt động");
        }

        // Kiểm tra xóa mềm
        if (phieuGiamGia.getDeleted() != null && phieuGiamGia.getDeleted()) {
            throw new RuntimeException("Phiếu giảm giá đã bị xóa");
        }

        // Kiểm tra ngày bắt đầu
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayBatDau().isAfter(now)) {
            throw new RuntimeException("Phiếu giảm giá chưa có hiệu lực");
        }

        // Kiểm tra ngày kết thúc
        if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
            throw new RuntimeException("Phiếu giảm giá đã hết hạn");
        }
    }

    /**
     * Kiểm tra phiếu giảm giá có thể áp dụng cho khách hàng không
     */
    public boolean canApplyVoucherForCustomer(Integer voucherId, Integer customerId) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(voucherId)
                .orElse(null);

        if (phieuGiamGia == null) {
            return false;
        }

        try {
            validateVoucherForApplication(phieuGiamGia);
        } catch (RuntimeException e) {
            return false;
        }

        // Kiểm tra số lượng
        if (phieuGiamGia.getSoLuongDung() != null && phieuGiamGia.getSoLuongDung() <= 0) {
            return false;
        }

        // Nếu là phiếu riêng tư, kiểm tra khách hàng có quyền sử dụng không
        if (Boolean.TRUE.equals(phieuGiamGia.getRiengTu())) {
            if (customerId == null) {
                return false;
            }
            
            KhachHang khachHang = khachHangRepository.findById(customerId).orElse(null);
            if (khachHang == null) {
                return false;
            }

            // Kiểm tra khách hàng có trong danh sách phiếu cá nhân không
            Optional<PhieuGiamGiaCaNhan> personalVoucher = phieuGiamGiaCaNhanRepository
                    .findByIdPhieuGiamGiaAndIdKhachHang(phieuGiamGia, khachHang);
            
            return personalVoucher.isPresent();
        }

        return true; // Phiếu công khai
    }
}
