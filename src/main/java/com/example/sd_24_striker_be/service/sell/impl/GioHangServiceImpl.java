package com.example.sd_24_striker_be.service.sell.impl;

import com.example.sd_24_striker_be.dto.sell.request.AddToCartRequest;
import com.example.sd_24_striker_be.dto.sell.request.UpdateCartItemRequest;
import com.example.sd_24_striker_be.dto.sell.response.CartResponse;
import com.example.sd_24_striker_be.dto.sell.response.CartItemResponse;
import com.example.sd_24_striker_be.exception.ResourceNotFoundException;
import com.example.sd_24_striker_be.modal.bill.HoaDon;
import com.example.sd_24_striker_be.modal.product.ChiTietSanPham;
import com.example.sd_24_striker_be.modal.sell.GioHang;
import com.example.sd_24_striker_be.modal.sell.GioHangChiTiet;
import com.example.sd_24_striker_be.repository.bill.HoaDonRepository;
import com.example.sd_24_striker_be.repository.product.ChiTietSanPhamRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangRepository;
import com.example.sd_24_striker_be.repository.sell.GioHangChiTietRepository;
import com.example.sd_24_striker_be.service.product.ChiTietSanPhamService;
import com.example.sd_24_striker_be.service.sell.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GioHangServiceImpl implements GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @Override
    public CartResponse getOrCreateCartByInvoiceId(Integer hoaDonId) {
        // Find existing cart or create new one
        Optional<GioHang> existingCart = gioHangRepository.findByHoaDonId(hoaDonId);
        
        if (existingCart.isPresent()) {
            return mapToCartResponse(existingCart.get());
        }
        
        // Create new cart
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + hoaDonId));
        
        GioHang newCart = GioHang.builder()
            .idHoaDon(hoaDon)
            .idKhachHang(hoaDon.getIdKhachHang())
            .tongTien(BigDecimal.ZERO)
            .build();
        
        GioHang savedCart = gioHangRepository.save(newCart);
        return mapToCartResponse(savedCart);
    }

    @Override
    public CartItemResponse addProductToCart(Integer hoaDonId, AddToCartRequest request) {
        // Get or create cart
        GioHang cart = gioHangRepository.findByHoaDonId(hoaDonId)
            .orElseGet(() -> {
                HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + hoaDonId));
                
                GioHang newCart = GioHang.builder()
                    .idHoaDon(hoaDon)
                    .idKhachHang(hoaDon.getIdKhachHang())
                    .tongTien(BigDecimal.ZERO)
                    .build();
                
                return gioHangRepository.save(newCart);
            });

        // Get product details
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(request.getIdChiTietSanPham())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm với ID: " + request.getIdChiTietSanPham()));

        if (Boolean.TRUE.equals(chiTietSanPham.getDeleted())) {
            throw new IllegalArgumentException("Sản phẩm này đã ngừng bán");
        }

        // Check stock availability
        if (chiTietSanPham.getSoLuongTonKho() < request.getSoLuong()) {
            throw new IllegalArgumentException("Không đủ hàng trong kho. Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        // Giá lấy từ server (giá bán + đợt giảm giá đang chạy), KHÔNG dùng request.getGia()
        BigDecimal gia = chiTietSanPhamService.getEffectivePrice(chiTietSanPham);

        // Check if product already exists in cart
        Optional<GioHangChiTiet> existingItem = gioHangChiTietRepository
            .findByGioHangIdAndChiTietSpId(cart.getId(), request.getIdChiTietSanPham());

        GioHangChiTiet cartItem;
        if (existingItem.isPresent()) {
            // Update existing item
            cartItem = existingItem.get();
            int newQuantity = cartItem.getSoLuong() + request.getSoLuong();
            
            // Check total quantity against stock
            if (chiTietSanPham.getSoLuongTonKho() < newQuantity) {
                throw new IllegalArgumentException("Không đủ hàng trong kho. Còn lại: " + chiTietSanPham.getSoLuongTonKho());
            }
            
            cartItem.setSoLuong(newQuantity);
            cartItem.setGia(gia);
        } else {
            // Create new cart item
            cartItem = GioHangChiTiet.builder()
                .idGioHang(cart)
                .idChiTietSp(chiTietSanPham)
                .soLuong(request.getSoLuong())
                .gia(gia)
                .build();
        }

        // KHÔNG trừ tồn kho ở đây. Giỏ hàng chỉ là bản nháp; tồn kho được trừ đúng một lần
        // tại bước chốt đơn (HoaDonServiceImpl.deductStockForHoaDon) - khi bán tại quầy là
        // lúc thanh toán, khi bán online là lúc admin bấm "Đã xác nhận". Bản cũ trừ ngay tại
        // đây khiến tồn kho tụt xuống dù đơn chưa chắc thành công, và còn bị trừ chồng lần
        // nữa ở bước thanh toán online.
        GioHangChiTiet savedItem = gioHangChiTietRepository.save(cartItem);
        
        // Update cart total
        updateCartTotal(cart.getId());
        
        return mapToCartItemResponse(savedItem);
    }

    @Override
    public CartItemResponse updateCartItem(Integer hoaDonId, Integer cartItemId, UpdateCartItemRequest request) {
        // Verify cart exists for this invoice
        GioHang cart = gioHangRepository.findByHoaDonId(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        // Get cart item
        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + cartItemId));

        // Verify item belongs to this cart
        if (!cartItem.getIdGioHang().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Sản phẩm không thuộc giỏ hàng này");
        }

        // Get product details
        ChiTietSanPham chiTietSanPham = cartItem.getIdChiTietSp();
        int newQuantity = request.getSoLuong();

        // Giỏ hàng không giữ chỗ tồn kho nữa (xem addProductToCart), nên phải đối chiếu
        // TỔNG số lượng mới với tồn kho thực tế, không phải phần chênh lệch như bản cũ.
        if (newQuantity > 0 && chiTietSanPham.getSoLuongTonKho() < newQuantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho. Còn lại: " + chiTietSanPham.getSoLuongTonKho());
        }

        // Update quantity
        cartItem.setSoLuong(newQuantity);
        GioHangChiTiet savedItem = gioHangChiTietRepository.save(cartItem);
        
        // Update cart total
        updateCartTotal(cart.getId());
        
        return mapToCartItemResponse(savedItem);
    }

    @Override
    public void removeCartItem(Integer hoaDonId, Integer cartItemId) {
        // Verify cart exists for this invoice
        GioHang cart = gioHangRepository.findByHoaDonId(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        // Get cart item
        GioHangChiTiet cartItem = gioHangChiTietRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + cartItemId));

        // Verify item belongs to this cart
        if (!cartItem.getIdGioHang().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Sản phẩm không thuộc giỏ hàng này");
        }

        // Không cộng trả tồn kho: giỏ hàng chưa từng trừ kho (xem addProductToCart).
        gioHangChiTietRepository.delete(cartItem);
        
        // Update cart total
        updateCartTotal(cart.getId());
    }

    @Override
    public void clearCart(Integer hoaDonId) {
        GioHang cart = gioHangRepository.findByHoaDonId(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));

        // Không cộng trả tồn kho: giỏ hàng chưa từng trừ kho (xem addProductToCart).
        gioHangChiTietRepository.deleteByGioHangId(cart.getId());
        
        // Reset cart total
        cart.setTongTien(BigDecimal.ZERO);
        gioHangRepository.save(cart);
    }

    @Override
    public CartResponse getCartByInvoiceId(Integer hoaDonId) {
        GioHang cart = gioHangRepository.findByHoaDonId(hoaDonId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho hóa đơn ID: " + hoaDonId));
        
        return mapToCartResponse(cart);
    }

    private void updateCartTotal(Integer cartId) {
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(cartId);
        BigDecimal total = items.stream()
            .map(item -> item.getThanhTien())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        GioHang cart = gioHangRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng với ID: " + cartId));
        
        cart.setTongTien(total);
        gioHangRepository.save(cart);
    }

    private CartResponse mapToCartResponse(GioHang cart) {
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(cart.getId());
        
        return CartResponse.builder()
            .id(cart.getId())
            .idKhachHang(cart.getIdKhachHang() != null ? cart.getIdKhachHang().getId() : null)
            .tenKhachHang(cart.getIdKhachHang() != null ? cart.getIdKhachHang().getTen() : null)
            .idHoaDon(cart.getIdHoaDon().getId())
            .maHoaDon(cart.getIdHoaDon().getMa())
            .tongTien(cart.getTongTien())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .items(items.stream().map(this::mapToCartItemResponse).collect(Collectors.toList()))
            .totalItems(items.size())
            .build();
    }

    private CartItemResponse mapToCartItemResponse(GioHangChiTiet item) {
        ChiTietSanPham chiTiet = item.getIdChiTietSp();

        // Per-variant image (chiTiet.idAnhSanPham) is optional and usually null;
        // fall back to the product's default image (idSanPham.idAnhSanPham) in that case.
        String urlAnh = chiTiet.getIdAnhSanPham() != null
            ? chiTiet.getIdAnhSanPham().getUrlAnh()
            : (chiTiet.getIdSanPham().getIdAnhSanPham() != null
                ? chiTiet.getIdSanPham().getIdAnhSanPham().getUrlAnh()
                : null);

        return CartItemResponse.builder()
            .id(item.getId())
            .idChiTietSanPham(chiTiet.getId())
            .maSanPham(chiTiet.getMa())
            .tenSanPham(chiTiet.getIdSanPham().getTenSanPham())
            .tenMauSac(chiTiet.getIdMauSac().getTenMauSac())
            .hexMauSac(chiTiet.getIdMauSac().getHex())
            .tenKichCo(chiTiet.getIdKichCo().getTenKichCo())
            .tenThuongHieu(chiTiet.getIdSanPham().getIdThuongHieu().getTenThuongHieu())
            .tenDanhMuc(chiTiet.getIdSanPham().getIdDanhMuc().getTenDanhMuc())
            .urlAnhSanPham(urlAnh)
            .soLuong(item.getSoLuong())
            .gia(item.getGia())
            .thanhTien(item.getThanhTien())
            .soLuongTonKho(chiTiet.getSoLuongTonKho())
            .createdAt(item.getCreatedAt())
            .build();
    }

    @Override
    public void updateCartCustomer(Integer hoaDonId, Integer customerId) {
        // Find cart by invoice ID
        Optional<GioHang> cartOptional = gioHangRepository.findByHoaDonId(hoaDonId);
        
        if (cartOptional.isPresent()) {
            GioHang cart = cartOptional.get();
            
            // Get customer from invoice
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + hoaDonId));
            
            // Update cart customer to match invoice customer
            cart.setIdKhachHang(hoaDon.getIdKhachHang());
            gioHangRepository.save(cart);
        }
    }
}
