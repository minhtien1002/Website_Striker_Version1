package com.example.sd_24_striker_be.service.sell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tính phí vận chuyển theo địa chỉ giao hàng - nguồn DUY NHẤT cho phí ship của đơn online.
 *
 * <ol>
 *   <li>Nếu cấu hình {@code ghn.token} và {@code ghn.shop-id}: gọi API tính phí của Giao Hàng Nhanh
 *       (tra mã quận/phường theo tên trong địa chỉ).</li>
 *   <li>Nếu chưa cấu hình GHN hoặc GHN lỗi: tính theo bảng giá vùng, so với tỉnh của cửa hàng
 *       ({@code app.cua-hang.tinh}) - nội tỉnh / cùng miền / khác miền.</li>
 * </ol>
 * Địa chỉ dạng chuỗi "số nhà, phường/xã, quận/huyện, tỉnh/thành" (cách lưu của hóa đơn).
 */
@Service
public class PhiVanChuyenService {

    private static final Logger log = LoggerFactory.getLogger(PhiVanChuyenService.class);
    private static final String GHN = "https://online-gateway.ghn.vn/shiip/public-api";

    @Value("${ghn.token:}")
    private String ghnToken;
    @Value("${ghn.shop-id:}")
    private String ghnShopId;
    @Value("${ghn.from-district-id:}")
    private String ghnFromDistrictId;
    @Value("${app.cua-hang.tinh:Hà Nội}")
    private String tinhCuaHang;
    @Value("${app.phi-van-chuyen.noi-tinh:20000}")
    private BigDecimal phiNoiTinh;
    @Value("${app.phi-van-chuyen.cung-mien:30000}")
    private BigDecimal phiCungMien;
    @Value("${app.phi-van-chuyen.khac-mien:40000}")
    private BigDecimal phiKhacMien;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper om = new ObjectMapper();

    public record KetQua(BigDecimal phiVanChuyen, String nguon, String tinhThanh) {
    }

    public KetQua tinhPhi(String diaChi, int soLuongSanPham) {
        String[] parts = diaChi == null ? new String[0] : diaChi.split(",");
        String tinh = parts.length >= 1 ? parts[parts.length - 1].trim() : "";
        String quan = parts.length >= 2 ? parts[parts.length - 2].trim() : "";
        String phuong = parts.length >= 3 ? parts[parts.length - 3].trim() : "";
        if (tinh.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ địa chỉ giao hàng (tỉnh/thành, quận/huyện, phường/xã)");
        }
        if (!ghnToken.isBlank() && !ghnShopId.isBlank()) {
            try {
                BigDecimal phi = tinhPhiGhn(tinh, quan, phuong, Math.max(1, soLuongSanPham));
                if (phi != null) {
                    return new KetQua(phi, "GHN", tinh);
                }
            } catch (Exception e) {
                log.warn("Không tính được phí GHN cho \"{}\": {} - dùng bảng giá vùng", diaChi, e.getMessage());
            }
        }
        return new KetQua(tinhTheoVung(tinh), "Bảng giá theo vùng", tinh);
    }

    // ===================== Bảng giá theo vùng =====================

    private static final Set<String> MIEN_BAC = Set.of("ha noi", "hai phong", "quang ninh", "bac ninh", "bac giang", "hai duong",
            "hung yen", "vinh phuc", "phu tho", "thai nguyen", "bac kan", "cao bang", "lang son", "ha giang", "tuyen quang",
            "lao cai", "yen bai", "lai chau", "dien bien", "son la", "hoa binh", "ha nam", "nam dinh", "thai binh", "ninh binh");
    private static final Set<String> MIEN_TRUNG = Set.of("thanh hoa", "nghe an", "ha tinh", "quang binh", "quang tri",
            "thua thien hue", "hue", "da nang", "quang nam", "quang ngai", "binh dinh", "phu yen", "khanh hoa", "ninh thuan",
            "binh thuan", "kon tum", "gia lai", "dak lak", "dak nong", "lam dong");

    private BigDecimal tinhTheoVung(String tinh) {
        String t = chuanHoaTinh(tinh);
        String ch = chuanHoaTinh(tinhCuaHang);
        if (t.equals(ch)) {
            return phiNoiTinh;
        }
        return mien(t) == mien(ch) ? phiCungMien : phiKhacMien;
    }

    private int mien(String tinh) {
        if (MIEN_BAC.contains(tinh)) return 1;
        if (MIEN_TRUNG.contains(tinh)) return 2;
        return 3;
    }

    static String chuanHoa(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private static String chuanHoaTinh(String s) {
        return chuanHoa(s).replaceFirst("^(thanh pho|tp|tinh) ", "").trim();
    }

    private static String boTienTo(String s) {
        return chuanHoa(s).replaceFirst("^(thanh pho|tp|tinh|quan|huyen|thi xa|thi tran|phuong|xa) ", "").trim();
    }

    // ===================== GHN =====================

    private BigDecimal tinhPhiGhn(String tinh, String quan, String phuong, int soLuong) throws Exception {
        Integer provinceId = timId(get("/master-data/province", null), "ProvinceID", "ProvinceName", tinh);
        if (provinceId == null) return null;
        Integer districtId = timId(post("/master-data/district", Map.of("province_id", provinceId)), "DistrictID", "DistrictName", quan);
        if (districtId == null) return null;
        String wardCode = null;
        JsonNode wards = post("/master-data/ward", Map.of("district_id", districtId));
        for (JsonNode w : wards) {
            if (boTienTo(w.path("WardName").asText()).equals(boTienTo(phuong))) {
                wardCode = w.path("WardCode").asText();
                break;
            }
        }
        if (wardCode == null) return null;
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("service_type_id", 2);
        body.put("to_district_id", districtId);
        body.put("to_ward_code", wardCode);
        body.put("weight", 300 * soLuong); // ước lượng 300g / áo
        body.put("length", 30);
        body.put("width", 25);
        body.put("height", Math.min(50, 3 * soLuong));
        if (!ghnFromDistrictId.isBlank()) {
            body.put("from_district_id", Integer.parseInt(ghnFromDistrictId.trim()));
        }
        JsonNode fee = post("/v2/shipping-order/fee", body);
        return fee.has("total") ? BigDecimal.valueOf(fee.get("total").asLong()) : null;
    }

    private Integer timId(JsonNode arr, String idField, String nameField, String ten) {
        String can = boTienTo(ten);
        for (JsonNode n : arr) {
            if (boTienTo(n.path(nameField).asText()).equals(can)) return n.path(idField).asInt();
            for (JsonNode ex : n.path("NameExtension")) {
                if (boTienTo(ex.asText()).equals(can)) return n.path(idField).asInt();
            }
        }
        return null;
    }

    private JsonNode get(String path, Object ignored) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(GHN + path)).GET());
    }

    private JsonNode post(String path, Object body) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(GHN + path))
                .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body))));
    }

    private JsonNode send(HttpRequest.Builder b) throws Exception {
        HttpRequest req = b.header("Token", ghnToken.trim()).header("ShopId", ghnShopId.trim())
                .header("Content-Type", "application/json").timeout(Duration.ofSeconds(8)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode json = om.readTree(res.body());
        if (res.statusCode() != 200 || json.path("code").asInt() != 200) {
            throw new IllegalStateException("GHN trả lỗi: " + json.path("message").asText(res.body()));
        }
        return json.path("data");
    }
}
