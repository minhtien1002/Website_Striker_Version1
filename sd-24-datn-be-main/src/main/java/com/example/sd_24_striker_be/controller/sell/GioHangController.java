package com.example.sd_24_striker_be.controller.sell;

import com.example.sd_24_striker_be.dto.sell.request.AddToCartRequest;
import com.example.sd_24_striker_be.dto.sell.request.UpdateCartItemRequest;
import com.example.sd_24_striker_be.dto.sell.response.CartResponse;
import com.example.sd_24_striker_be.dto.sell.response.CartItemResponse;
import com.example.sd_24_striker_be.service.sell.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gio-hang")
@CrossOrigin(origins = "*")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    /**
     * Get cart by invoice ID
     */
    @GetMapping("/hoa-don/{hoaDonId}")
    public ResponseEntity<?> getCartByInvoiceId(@PathVariable Integer hoaDonId) {
        CartResponse cart = gioHangService.getOrCreateCartByInvoiceId(hoaDonId);
            return ResponseEntity.ok(cart);
    }

    /**
     * Add product to cart
     */
    @PostMapping("/hoa-don/{hoaDonId}/them-san-pham")
    public ResponseEntity<?> addProductToCart(
            @PathVariable Integer hoaDonId,
            @Valid @RequestBody AddToCartRequest request) {
        CartItemResponse cartItem = gioHangService.addProductToCart(hoaDonId, request);
            return ResponseEntity.ok(cartItem);
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/hoa-don/{hoaDonId}/cap-nhat/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer hoaDonId,
            @PathVariable Integer cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartItemResponse cartItem = gioHangService.updateCartItem(hoaDonId, cartItemId, request);
            return ResponseEntity.ok(cartItem);
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/hoa-don/{hoaDonId}/xoa/{cartItemId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable Integer hoaDonId,
            @PathVariable Integer cartItemId) {
        gioHangService.removeCartItem(hoaDonId, cartItemId);
            return ResponseEntity.ok().build();
    }

    /**
     * Clear all items from cart
     */
    @DeleteMapping("/hoa-don/{hoaDonId}/xoa-tat-ca")
    public ResponseEntity<?> clearCart(@PathVariable Integer hoaDonId) {
        gioHangService.clearCart(hoaDonId);
            return ResponseEntity.ok().build();
    }
}
