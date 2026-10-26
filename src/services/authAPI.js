import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/auth';
const CLIENT_BASE_URL = 'http://localhost:8080/api/auth-client';

// Create axios instance with default config for admin/employee auth
const authAPI = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Create axios instance with default config for customer auth
const authClientAPI = axios.create({
  baseURL: CLIENT_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Auth API endpoints
export const AuthAPI = {
  // Admin/Employee login endpoint (roles 1, 2)
  adminLogin: async (loginData) => {
    const response = await authAPI.post('/login', {
      tenDangNhap: loginData.username || loginData.email, // Support both username and email
      matKhau: loginData.password,
      rememberMe: loginData.rememberMe || false
    });
    return response.data;
  },

  // Customer login endpoint (role 3)
  login: async (loginData) => {
    const response = await authClientAPI.post('/login', {
      tenDangNhap: loginData.username || loginData.email, // Support both username and email
      matKhau: loginData.password,
      rememberMe: loginData.rememberMe || false
    });
    return response.data;
  },

  // Admin/Employee logout endpoint
  adminLogout: async (userId = null) => {
    const url = userId ? `/logout?userId=${userId}` : '/logout';
    const response = await authAPI.post(url);
    return response.data;
  },

  // Customer logout endpoint
  logout: async (userId = null) => {
    const url = userId ? `/logout?userId=${userId}` : '/logout';
    const response = await authClientAPI.post(url);
    return response.data;
  },

  // Customer registration endpoint
  register: async (registerData) => {
    const response = await authClientAPI.post('/register', {
      name: registerData.name,
      email: registerData.email,
      phoneNumber: registerData.phoneNumber,
      password: registerData.password
    });
    return response.data;
  },

  // Step 1 of forgot-password flow: request an OTP code sent to the registered email
  forgotPassword: async (email) => {
    const response = await authClientAPI.post('/forgot-password', { email });
    return response.data;
  },

  // Step 2 of forgot-password flow: confirm the OTP code and set a new password
  resetPassword: async ({ email, otpCode, newPassword }) => {
    const response = await authClientAPI.post('/reset-password', { email, otpCode, newPassword });
    return response.data;
  }
};

export default AuthAPI;
