import khachHangAPI from '@/services/api/APIKhachHang/KhachHangAPI.js'

const khachHangService = {
    // Transform backend data to frontend format
    mapBackendToFrontend: (backendData) => {
        return {
            id: backendData.id,
            ma: backendData.ma,
            tenKhachHang: backendData.ten || backendData.tenKhachHang,
            ngaySinh: backendData.ngaySinh,
            gioiTinh: backendData.gioiTinh,
            soDienThoai: backendData.soDienThoai,
            cccd: backendData.cccd,
            email: backendData.email,
            diaChiCuThe: backendData.diaChiCuThe,
            thanhPho: backendData.thanhPho,
            quan: backendData.quan,
            phuong: backendData.phuong,
            trangThai: backendData.deleted, // Based on memory: deleted=true means active
            taiKhoanID: backendData.taiKhoanId
        }
    },

    // Transform frontend data to backend format for create
    mapFrontendToBackendCreate: (frontendData) => {
        return {
            // KhachHang fields
            ten: frontendData.tenKhachHang,
            ngaySinh: frontendData.ngaySinh,
            gioiTinh: frontendData.gioiTinh ? 1 : 0, // Convert boolean to Short (1/0)
            cccd: frontendData.cccd || null,

            // TaiKhoan fields
            email: frontendData.email || '',
            soDienThoai: frontendData.soDienThoai,
            matKhau: frontendData.matKhau || 'default123',

            // DiaChiKhachHang fields (3rd table)
            thanhPho: frontendData.thanhPho || frontendData.diaChiTinhThanh,
            quan: frontendData.quan || frontendData.diaChiQuanHuyen,
            phuong: frontendData.phuong || frontendData.diaChiPhuongXa,
            diaChiCuThe: frontendData.diaChiCuThe || frontendData.diaChiSoNhaTenDuong
        }
    },

    // Transform frontend data to backend format for update
    mapFrontendToBackendUpdate: (frontendData) => {
        return {
            id: frontendData.id,
            ten: frontendData.tenKhachHang,
            ngaySinh: frontendData.ngaySinh,
            gioiTinh: frontendData.gioiTinh ? 1 : 0, // Convert boolean to Short (1/0)
            cccd: frontendData.cccd,

            // TaiKhoan fields
            email: frontendData.email,
            soDienThoai: frontendData.soDienThoai,

            // DiaChiKhachHang fields (3rd table)
            thanhPho: frontendData.thanhPho || frontendData.diaChiTinhThanh,
            quan: frontendData.quan || frontendData.diaChiQuanHuyen,
            phuong: frontendData.phuong || frontendData.diaChiPhuongXa,
            diaChiCuThe: frontendData.diaChiCuThe || frontendData.diaChiSoNhaTenDuong,

            // Status for soft delete
            deleted: frontendData.trangThai
        }
    },

    // Get all customers with filtering and pagination
    getAllCustomers: async (filters = {}) => {
        try {
            const params = {
                page: filters.page || 0,
                size: filters.size || 10,
                keyword: filters.search || '',
                gioiTinh: filters.gioiTinh !== '' ? filters.gioiTinh : null,
                trangThai: filters.trangThai !== '' ? filters.trangThai : null,
                sortBy: filters.sortBy || 'id',
                sortDir: filters.sortDir || 'desc'
            }

            const response = await khachHangAPI.getAllKhachHang(params)

            // Transform backend data to frontend format
            if (response.content) {
                response.content = response.content.map(customer =>
                    khachHangService.mapBackendToFrontend(customer)
                )
            }

            return response
        } catch (error) {
            console.error('Service error getting customers:', error)
            throw error
        }
    },

    // Get customer by ID
    getCustomerById: async (id) => {
        try {
            const response = await khachHangAPI.getKhachHangById(id)
            return response.data
        } catch (error) {
            console.error('Service error getting customer by ID:', error)
            throw error
        }
    },

    // Create new customer
    createCustomer: async (customerData) => {
        try {
            const backendData = khachHangService.mapFrontendToBackendCreate(customerData)
            const response = await khachHangAPI.createKhachHang(backendData)
            return response
        } catch (error) {
            console.error('Service error creating customer:', error)
            throw error
        }
    },

    // Update customer
    updateCustomer: async (id, customerData) => {
        try {
            const backendData = khachHangService.mapFrontendToBackendUpdate(customerData)
            const response = await khachHangAPI.updateKhachHang(id, backendData)
            return response
        } catch (error) {
            console.error('Service error updating customer:', error)
            throw error
        }
    },

    // Delete customer (soft delete)
    deleteCustomer: async (id) => {
        try {
            const response = await khachHangAPI.deleteKhachHang(id)
            return response
        } catch (error) {
            console.error('Service error deleting customer:', error)
            throw error
        }
    },

    // Restore customer
    restoreCustomer: async (id) => {
        try {
            const response = await khachHangAPI.restoreKhachHang(id)
            return response
        } catch (error) {
            console.error('Service error restoring customer:', error)
            throw error
        }
    },

    // Validate customer data
    validateCustomerData: (customerData, isUpdate = false) => {
        const errors = []

        if (!customerData.tenKhachHang || customerData.tenKhachHang.trim() === '') {
            errors.push('Tên khách hàng là bắt buộc')
        }

        if (!customerData.ngaySinh) {
            errors.push('Ngày sinh là bắt buộc')
        }

        if (!customerData.soDienThoai || customerData.soDienThoai.trim() === '') {
            errors.push('Số điện thoại là bắt buộc')
        } else if (!/^0\d{9}$/.test(customerData.soDienThoai)) {
            errors.push('Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số')
        }

        // Validate email if provided
        if (customerData.email && customerData.email.trim() !== '') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
            if (!emailRegex.test(customerData.email.trim())) {
                errors.push('Email không hợp lệ')
            }
        }

        // Validate name contains only letters and spaces
        const nameRegex = /^[\p{L}\s]+$/u
        if (customerData.tenKhachHang && !nameRegex.test(customerData.tenKhachHang.trim())) {
            errors.push('Tên khách hàng chỉ được chứa chữ cái và khoảng trắng')
        }

        // Validate age (must be at least 16 years old)
        if (customerData.ngaySinh) {
            const birthDate = new Date(customerData.ngaySinh)
            const today = new Date()
            let age = today.getFullYear() - birthDate.getFullYear()
            const monthDiff = today.getMonth() - birthDate.getMonth()

            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                age--
            }

            if (age < 16) {
                errors.push('Khách hàng phải từ 16 tuổi trở lên')
            }
        }

        // Validate CCCD if provided
        if (customerData.cccd && customerData.cccd.trim() !== '') {
            const cccdRegex = /^\d{12}$/
            if (!cccdRegex.test(customerData.cccd)) {
                errors.push('CCCD phải có 12 chữ số')
            }
        }

        return errors
    },

    // Format date for display
    formatDate: (dateString) => {
        if (!dateString) return ''
        const date = new Date(dateString)
        return date.toLocaleDateString('vi-VN')
    },

    // Format gender for display
    formatGender: (gioiTinh) => {
        return gioiTinh === true ? 'Nam' : 'Nữ'
    },

    // Format status for display
    formatStatus: (trangThai) => {
        return trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
    }
}

export default khachHangService
