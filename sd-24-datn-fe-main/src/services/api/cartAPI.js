import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/gio-hang';

const cartAPI = {
  // Get cart by invoice ID
  getCartByInvoiceId: async (hoaDonId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/hoa-don/${hoaDonId}`);
      return response.data;
    } catch (error) {
      console.error('Error getting cart:', error);
      throw error;
    }
  },

  // Add product to cart
  addProductToCart: async (hoaDonId, productData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/hoa-don/${hoaDonId}/them-san-pham`, {
        idChiTietSanPham: productData.idChiTietSanPham,
        soLuong: productData.soLuong,
        gia: productData.gia
      });
      return response.data;
    } catch (error) {
      console.error('Error adding product to cart:', error);
      throw error;
    }
  },

  // Update cart item quantity
  updateCartItem: async (hoaDonId, cartItemId, soLuong) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/hoa-don/${hoaDonId}/cap-nhat/${cartItemId}`, {
        soLuong: soLuong
      });
      return response.data;
    } catch (error) {
      console.error('Error updating cart item:', error);
      throw error;
    }
  },

  // Remove item from cart
  removeCartItem: async (hoaDonId, cartItemId) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/hoa-don/${hoaDonId}/xoa/${cartItemId}`);
      return response.data;
    } catch (error) {
      console.error('Error removing cart item:', error);
      throw error;
    }
  },

  // Clear all items from cart
  clearCart: async (hoaDonId) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/hoa-don/${hoaDonId}/xoa-tat-ca`);
      return response.data;
    } catch (error) {
      console.error('Error clearing cart:', error);
      throw error;
    }
  }
};

export default cartAPI;
