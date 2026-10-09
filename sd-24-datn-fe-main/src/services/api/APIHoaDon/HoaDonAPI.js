import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add request interceptor for authentication
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Add response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error.response?.data || error.message);
        return Promise.reject(error);
    }
);

/**
 * Get all invoices with filtering and pagination
 * @param {Object} params - Query parameters
 * @param {number} params.page - Page number (0-based)
 * @param {number} params.size - Page size
 * @param {string} params.keyword - Search keyword
 * @param {number} params.minAmount - Minimum amount filter
 * @param {number} params.maxAmount - Maximum amount filter
 * @param {number} params.startDate - Start date timestamp
 * @param {number} params.endDate - End date timestamp
 * @param {number} params.trangThai - Status filter
 * @param {string} params.loaiDon - Order type filter
 * @param {string} params.sortBy - Sort field
 * @param {string} params.sortDir - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllHoaDon = async (params = {}) => {
    try {
        const response = await api.get('/hoa-don/home', { params });
        return response.data;
    } catch (error) {
        console.error('Error fetching invoices:', error);
        throw error;
    }
};

/**
 * Get invoice by ID
 * @param {number} id - Invoice ID
 * @returns {Promise} API response
 */
export const getHoaDonById = async (id) => {
    try {
        const response = await api.get(`/hoa-don/${id}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching invoice by ID:', error);
        throw error;
    }
};

/**
 * Update invoice
 * @param {number} id - Invoice ID
 * @param {Object} data - Invoice data to update
 * @returns {Promise} API response
 */
export const updateHoaDon = async (id, data) => {
    try {
        const response = await api.put(`/hoa-don/${id}`, data);
        return response.data;
    } catch (error) {
        console.error('Error updating invoice:', error);
        throw error;
    }
};

/**
 * Delete a PENDING/draft invoice (hoa_don_cho - an abandoned POS cart that was never
 * actually placed, trangThai = 0). This can NOT delete a real, placed order - use
 * cancelHoaDon (POST /hoa-don/{id}/huy) to cancel those instead, since a real order
 * must go through the cancel flow (restores stock, logs history, notifies customer).
 * NOTE: there is no generic DELETE /hoa-don/{id} on the backend by design.
 * @param {number} id - Invoice ID
 * @returns {Promise} API response
 */
export const deleteHoaDon = async (id) => {
    try {
        const response = await api.delete(`/ban-hang/hoa-don-cho/${id}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting pending invoice:', error);
        throw error;
    }
};

/**
 * Get status counts for all invoice statuses
 * @returns {Promise} API response with status counts
 */
export const getStatusCounts = async () => {
    try {
        const response = await api.get('/hoa-don/status-counts');
        return response.data;
    } catch (error) {
        console.error('Error fetching status counts:', error);
        throw error;
    }
};

/**
 * Get price range (min and max) from database
 * @returns {Promise} API response with price range
 */
export const getPriceRange = async () => {
    try {
        const response = await api.get('/hoa-don/price-range');
        return response.data;
    } catch (error) {
        console.error('Error fetching price range:', error);
        throw error;
    }
};

/**
 * Get total revenue of all completed invoices (trạng thái 5), across every page
 * @returns {Promise<number>}
 */
export const getCompletedRevenue = async () => {
    try {
        const response = await api.get('/hoa-don/completed-revenue');
        return Number(response.data) || 0;
    } catch (error) {
        console.error('Error fetching completed revenue:', error);
        throw error;
    }
};

/**
 * Get invoice by invoice code (for QR functionality)
 * @param {string} maHoaDon - Invoice code
 * @returns {Promise} API response with invoice data
 */
export const getHoaDonByMa = async (maHoaDon) => {
    try {
        const response = await api.get(`/hoa-don/QR-by-ma/${maHoaDon}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching invoice by code:', error);
        throw error;
    }
};

/**
 * Download invoice PDF
 * @param {number} id - Invoice ID
 * @returns {Promise} PDF blob for download
 */
export const downloadInvoicePDF = async (id) => {
    try {
        const response = await api.get(`/hoa-don/${id}/pdf`, {
            responseType: 'blob',
            headers: {
                'Accept': 'application/pdf'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error downloading invoice PDF:', error);
        throw error;
    }
};

export default {
    getAllHoaDon,
    getHoaDonById,
    updateHoaDon,
    deleteHoaDon,
    getStatusCounts,
    getPriceRange,
    getCompletedRevenue,
    getHoaDonByMa,
    downloadInvoicePDF
};