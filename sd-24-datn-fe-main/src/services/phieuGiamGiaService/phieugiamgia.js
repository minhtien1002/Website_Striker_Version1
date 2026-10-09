import { ref, computed } from 'vue';
import axios from 'axios';
import { useToast } from 'vue-toastification';
import { useRouter } from 'vue-router';
import Breadcrumb from '@/components/Breadcrumb.vue';
import DataTable from '@/components/DataTable.vue';

export default {
  name: 'CouponAdditionForm',
  components: {
    Breadcrumb,
    DataTable,
  },
  setup() {
    const toast = useToast();
    const router = useRouter();

    const breadcrumbItems = ref([
      { label: 'Phiếu giảm giá', path: '/phieu-giam-gia' },
      { label: 'Thêm mới', path: '/phieu-giam-gia/them-moi' },
    ]);

    const breadcrumbActions = ref([
      {
        label: 'Danh sách phiếu',
        type: 'default',
        handler: () => router.push('/phieu-giam-gia'),
      },
      {
        label: 'Làm mới',
        type: 'default',
        handler: () => window.location.reload(),
      },
    ]);

    const pageStats = ref([
      {
        value: '0',
        label: 'Phiếu đang hoạt động',
        icon: 'solar:ticket-bold-duotone',
      },
      {
        value: '0',
        label: 'Lượt sử dụng hôm nay',
        icon: 'solar:cart-check-bold-duotone',
      },
    ]);

    return { toast, router, breadcrumbItems, breadcrumbActions, pageStats };
  },
  data() {
    return {
      coupon: {
        ma: '',
        tenPhieuGiamGia: '',
        loaiPhieuGiamGia: '',
        phanTramGiamGia: null,
        soTienGiamToiDa: null,
        hoaDonToiThieu: null,
        soLuongDung: null,
        ngayBatDau: '',
        ngayKetThuc: '',
        riengTu: false,
        moTa: '',
      },
      customers: [],
      selectedCustomers: [],
      loading: false,
      customerColumns: [
        {
          key: 'checkbox',
          label: 'Chọn',
          class: 'text-center',
        },
        {
          key: 'ten',
          label: 'Tên khách hàng',
        },
        {
          key: 'email',
          label: 'Email',
        },
        {
          key: 'soDienThoai',
          label: 'SĐT',
        },
      ],
      selectedCustomerColumns: [
        {
          key: 'checkbox',
          label: 'Bỏ chọn',
          class: 'text-center',
        },
        {
          key: 'ten',
          label: 'Tên khách hàng',
        },
        {
          key: 'email',
          label: 'Email',
        },
        {
          key: 'soDienThoai',
          label: 'SĐT',
        },
      ],
    };
  },
  computed: {
    labelGiaTriGiam() {
      return this.coupon.loaiPhieuGiamGia === 'SO_TIEN_CO_DINH'
        ? 'Giá trị giảm (VND)'
        : 'Giá trị giảm (%)';
    },
    selectedCustomersData() {
      return this.customers.filter(customer => this.selectedCustomers.includes(customer.id));
    },
  },
  mounted() {
    this.fetchCustomers();
  },
  methods: {
    generateCode() {
      this.coupon.ma = 'PGG-' + Math.random().toString(36).substring(2, 8).toUpperCase();
      this.toast.info('Đã tạo mã phiếu giảm giá mới!');
    },
    async toggleCouponStatus(coupon) {
  try {
    this.loadingTable = true;
    const index = this.allCoupons.findIndex(c => c.id === coupon.id);
    if (index === -1) {
      throw new Error('Không tìm thấy phiếu giảm giá trong danh sách.');
    }

    const isActive = coupon.tenTrangThai === 'DANG_DIEN_RA' && !coupon.deleted;
    const actionText = isActive ? 'vô hiệu hóa' : 'kích hoạt';

    // Call API to toggle status (soft delete)
    const response = await axios.put(`http://localhost:8080/api/phieu-giam-gia/delete/${coupon.id}`);
    
    // Kiểm tra phản hồi từ API
    if (typeof response.data === 'string') {
      throw new Error(response.data); // Xử lý lỗi từ API
    }

    const updatedCoupon = response.data;

    // Update the local state with the response from the API
    this.allCoupons[index] = {
      ...this.allCoupons[index],
      deleted: updatedCoupon.deleted,
      tenTrangThai: updatedCoupon.deleted ? 'DA_KET_THUC' : 'DANG_DIEN_RA',
      ngayCapNhat: new Date().toISOString()
    };

    this.toast.success(`Đã ${actionText} phiếu giảm giá ${coupon.tenPhieuGiamGia}!`, { timeout: 3000 });
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái phiếu giảm giá:', error);
    const errorMessage = error.message || 'Không thể cập nhật trạng thái phiếu giảm giá.';
    this.toast.error(errorMessage, { timeout: 5000 });
  } finally {
    this.loadingTable = false;
  }
},
    

    handleLoaiGiamGiaChange() {
      if (this.coupon.loaiPhieuGiamGia === 'SO_TIEN_CO_DINH') {
        this.coupon.soTienGiamToiDa = null;
      }
    },

    handlePrivateCouponChange() {
      if (!this.coupon.riengTu) {
        this.selectedCustomers = [];
      }
    },

    async fetchCustomers() {
      this.loading = true;
      try {
        const res = await axios.get('/api/khach-hang');
        this.customers = Array.isArray(res.data) ? res.data : res.data.content || [];
        this.toast.success(`Tải danh sách khách hàng thành công! (${this.customers.length} khách hàng)`);
      } catch (err) {
        console.error('Lỗi tải khách hàng:', err);
        this.toast.error(`Không thể tải danh sách khách hàng: ${err.message || 'Lỗi không xác định'}`);
      } finally {
        this.loading = false;
      }
    },
    

    validateForm() {
      if (!this.coupon.ma.trim()) {
        this.toast.error('Mã phiếu giảm giá không được để trống!');
        return false;
      }
      if (!this.coupon.tenPhieuGiamGia.trim()) {
        this.toast.error('Tên phiếu giảm giá không được để trống!');
        return false;
      }
      if (!this.coupon.loaiPhieuGiamGia) {
        this.toast.error('Vui lòng chọn loại giảm giá!');
        return false;
      }
      if (this.coupon.loaiPhieuGiamGia === 'PHAN_TRAM') {
        if (!this.coupon.phanTramGiamGia || this.coupon.phanTramGiamGia <= 0 || this.coupon.phanTramGiamGia > 100) {
          this.toast.error('Giá trị giảm phải từ 1 đến 100 (%)!');
          return false;
        }
        if (!this.coupon.soTienGiamToiDa || this.coupon.soTienGiamToiDa <= 0) {
          this.toast.error('Số tiền giảm tối đa phải lớn hơn 0!');
          return false;
        }
      }
      if (this.coupon.loaiPhieuGiamGia === 'SO_TIEN_CO_DINH') {
        if (!this.coupon.phanTramGiamGia || this.coupon.phanTramGiamGia <= 0) {
          this.toast.error('Giá trị giảm (VND) phải lớn hơn 0!');
          return false;
        }
      }
      if (this.coupon.hoaDonToiThieu !== null && this.coupon.hoaDonToiThieu < 0) {
        this.toast.error('Hóa đơn tối thiểu không được nhỏ hơn 0!');
        return false;
      }
      if (!this.coupon.soLuongDung || this.coupon.soLuongDung <= 0) {
        this.toast.error('Số lượng sử dụng phải lớn hơn 0!');
        return false;
      }
      if (!this.coupon.ngayBatDau || !this.coupon.ngayKetThuc) {
        this.toast.error('Vui lòng chọn ngày bắt đầu và ngày kết thúc!');
        return false;
      }
      if (this.coupon.ngayBatDau >= this.coupon.ngayKetThuc) {
        this.toast.error('Ngày bắt đầu phải nhỏ hơn ngày kết thúc!');
        return false;
      }
      if (this.coupon.riengTu && this.selectedCustomers.length === 0) {
        this.toast.error('Vui lòng chọn ít nhất một khách hàng khi phiếu giảm giá là riêng tư!');
        return false;
      }
      return true;
    },

    async submitForm() {
      if (!this.validateForm()) return;

      this.loading = true;
      try {
        const formatDateTime = (dt) => {
          if (!dt) return null;
          if (dt.endsWith('Z') || dt.match(/[+-]\d{2}:\d{2}$/)) return dt;
          return dt + ':00Z';
        };
        const dataToSend = {
          ma: this.coupon.ma,
          tenPhieuGiamGia: this.coupon.tenPhieuGiamGia,
          loaiPhieuGiamGia: this.coupon.loaiPhieuGiamGia,
          phanTramGiamGia: this.coupon.phanTramGiamGia,
          soTienGiamToiDa: this.coupon.loaiPhieuGiamGia === 'PHAN_TRAM' ? this.coupon.soTienGiamToiDa : null,
          hoaDonToiThieu: this.coupon.hoaDonToiThieu,
          soLuongDung: this.coupon.soLuongDung,
          ngayBatDau: formatDateTime(this.coupon.ngayBatDau),
          ngayKetThuc: formatDateTime(this.coupon.ngayKetThuc),
          riengTu: this.coupon.riengTu,
          moTa: this.coupon.moTa,
          khachHangIds: this.coupon.riengTu ? this.selectedCustomers : [],
        };

        console.log('Dữ liệu gửi đi:', dataToSend); // Gỡ lỗi: kiểm tra dữ liệu gửi
        const res = await axios.post('/api/phieu-giam-gia/add', dataToSend);
        console.log('Phản hồi API:', res); // Gỡ lỗi: kiểm tra phản hồi

        if (res.status === 200 || res.status === 201) { // Chấp nhận cả 200 và 201
          this.toast.success('Thêm phiếu giảm giá thành công!', {
            timeout: 3000,
            position: 'top-right',
          });
          setTimeout(() => {
            console.log('Bắt đầu chuyển hướng đến PhieuGiamGia'); // Gỡ lỗi
            this.router.push({ path: '/phieu-giam-gia' }).catch(err => {
              console.error('Lỗi chuyển hướng:', err);
              this.toast.error('Không thể chuyển hướng đến trang danh sách phiếu giảm giá.', {
                timeout: 5000,
                position: 'top-right',
              });
            });
          }, 1500);
        } else {
          this.toast.error(`Thêm phiếu thất bại, mã trạng thái: ${res.status}`, {
            timeout: 5000,
            position: 'top-right',
          });
        }
      } catch (err) {
        console.error('Lỗi khi thêm phiếu:', err.response || err); // Gỡ lỗi
        let errorMessage = 'Đã xảy ra lỗi khi thêm phiếu giảm giá.';
        if (err.response?.data?.message) {
          errorMessage = err.response.data.message;
        }
        this.toast.error(errorMessage, {
          timeout: 5000,
          position: 'top-right',
        });
      } finally {
        this.loading = false;
      }
    },
  },
};