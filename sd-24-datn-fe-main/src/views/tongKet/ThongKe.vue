<template>
  <div class="min-h-screen bg-gray-50 dark:bg-slate-900 p-4 md:p-6 font-sans text-slate-800 dark:text-slate-200">
    
    <!-- Breadcrumb -->
    <Breadcrumb 
      :items="breadcrumbItems"
      :show-page-info="true"
      page-title="Dashboard Thống kê"
      page-description="Tổng quan hiệu suất kinh doanh của STRIKER"
      page-icon="solar:chart-square-bold-duotone"
      :page-stats="pageStats"
      :actions="breadcrumbActions"
      class="mb-6"
    />

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
      <!-- Doanh Thu -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 flex items-center gap-5 relative overflow-hidden group hover:shadow-md transition-shadow">
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-[#ff3b18]"></div>
        <div class="text-4xl text-[#ff3b18] opacity-80">
          <iconify-icon icon="solar:wallet-money-bold-duotone"></iconify-icon>
        </div>
        <div class="flex-1">
          <h3 class="text-2xl font-extrabold text-slate-800 dark:text-slate-100 m-0">{{ formatCurrency(stats.totalRevenue) }}</h3>
          <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mt-1 mb-2">Tổng Doanh Thu</p>
          <div class="flex items-center gap-1 text-sm font-semibold text-emerald-600">
            <iconify-icon icon="solar:arrow-up-bold"></iconify-icon>
            <span>+12.5%</span>
          </div>
        </div>
      </div>

      <!-- Đơn Hàng -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 flex items-center gap-5 relative overflow-hidden group hover:shadow-md transition-shadow">
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-[#28a745]"></div>
        <div class="text-4xl text-[#28a745] opacity-80">
          <iconify-icon icon="solar:bag-check-bold-duotone"></iconify-icon>
        </div>
        <div class="flex-1">
          <h3 class="text-2xl font-extrabold text-slate-800 dark:text-slate-100 m-0">{{ (stats.totalOrders || 0).toLocaleString() }}</h3>
          <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mt-1 mb-2">Đơn Hàng</p>
          <div class="flex items-center gap-1 text-sm font-semibold text-emerald-600">
            <iconify-icon icon="solar:arrow-up-bold"></iconify-icon>
            <span>+8.3%</span>
          </div>
        </div>
      </div>

      <!-- Khách Hàng -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 flex items-center gap-5 relative overflow-hidden group hover:shadow-md transition-shadow">
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-[#ffc107]"></div>
        <div class="text-4xl text-[#ffc107] opacity-80">
          <iconify-icon icon="solar:users-group-two-rounded-bold-duotone"></iconify-icon>
        </div>
        <div class="flex-1">
          <h3 class="text-2xl font-extrabold text-slate-800 dark:text-slate-100 m-0">{{ (stats.totalCustomers || 0).toLocaleString() }}</h3>
          <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mt-1 mb-2">Khách Hàng</p>
          <div class="flex items-center gap-1 text-sm font-semibold text-emerald-600">
            <iconify-icon icon="solar:arrow-up-bold"></iconify-icon>
            <span>+15.2%</span>
          </div>
        </div>
      </div>

      <!-- Sản Phẩm -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 flex items-center gap-5 relative overflow-hidden group hover:shadow-md transition-shadow">
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-[#dc3545]"></div>
        <div class="text-4xl text-[#dc3545] opacity-80">
          <iconify-icon icon="solar:box-bold-duotone"></iconify-icon>
        </div>
        <div class="flex-1">
          <h3 class="text-2xl font-extrabold text-slate-800 dark:text-slate-100 m-0">{{ (stats.totalProducts || 0).toLocaleString() }}</h3>
          <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mt-1 mb-2">Sản Phẩm</p>
          <div class="flex items-center gap-1 text-sm font-semibold text-slate-500">
            <iconify-icon icon="solar:arrow-right-bold"></iconify-icon>
            <span>+2.1%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-8">
      <!-- Revenue Chart -->
      <div class="xl:col-span-2 bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700">
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <h3 class="text-lg font-bold flex items-center gap-2">
            <iconify-icon icon="solar:chart-2-bold-duotone" class="text-[#ff3b18] text-xl"></iconify-icon>
            Xu Hướng Doanh Thu
          </h3>
          <div class="flex flex-wrap items-center gap-3">
            <div class="flex bg-gray-100 dark:bg-slate-700 p-1 rounded-lg">
              <button 
                v-for="period in [{v:'daily', l:'Ngày'}, {v:'weekly', l:'Tuần'}, {v:'monthly', l:'Tháng'}, {v:'yearly', l:'Năm'}]"
                :key="period.v"
                class="px-4 py-1.5 text-sm font-medium rounded-md transition-colors whitespace-nowrap"
                :class="revenuePeriod === period.v ? 'bg-white dark:bg-slate-800 text-[#ff3b18] shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'"
                @click="changeRevenuePeriod(period.v)"
              >
                {{ period.l }}
              </button>
            </div>
            <div class="flex gap-1 border-l border-gray-200 dark:border-slate-600 pl-3">
              <button 
                class="p-2 rounded-md transition-colors"
                :class="chartType === 'line' ? 'bg-[#ff3b18] text-white' : 'bg-gray-50 dark:bg-slate-700 text-slate-500 hover:bg-gray-100 dark:hover:bg-slate-600'"
                @click="changeChartType('line')"
                title="Biểu đồ đường"
              >
                <iconify-icon icon="solar:chart-2-bold-duotone" class="text-lg"></iconify-icon>
              </button>
              <button 
                class="p-2 rounded-md transition-colors"
                :class="chartType === 'bar' ? 'bg-[#ff3b18] text-white' : 'bg-gray-50 dark:bg-slate-700 text-slate-500 hover:bg-gray-100 dark:hover:bg-slate-600'"
                @click="changeChartType('bar')"
                title="Biểu đồ cột"
              >
                <iconify-icon icon="solar:chart-square-bold-duotone" class="text-lg"></iconify-icon>
              </button>
            </div>
          </div>
        </div>
        <div class="relative w-full h-[350px]">
          <canvas ref="revenueChart"></canvas>
        </div>
      </div>

      <!-- Category Performance -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700">
        <h3 class="text-lg font-bold flex items-center gap-2 mb-6">
          <iconify-icon icon="solar:pie-chart-2-bold-duotone" class="text-[#ff3b18] text-xl"></iconify-icon>
          Hiệu Suất Theo Thể Loại
        </h3>
        <div class="relative w-full h-[350px]">
          <canvas ref="categoryChart"></canvas>
        </div>
      </div>
    </div>

    <!-- Doanh thu theo kênh bán -->
    <div class="mb-8">
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700">
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <h3 class="text-lg font-bold flex items-center gap-2">
            <iconify-icon icon="solar:card-transfer-bold-duotone" class="text-[#ff3b18] text-xl"></iconify-icon>
            Doanh Thu Theo Kênh Bán
          </h3>
          <div class="flex flex-wrap items-center gap-3">
            <div class="flex bg-gray-100 dark:bg-slate-700 p-1 rounded-lg">
              <button
                v-for="option in channelRangeOptions"
                :key="option.value"
                class="px-4 py-1.5 text-sm font-medium rounded-md transition-colors whitespace-nowrap"
                :class="channelRange === option.value ? 'bg-white dark:bg-slate-800 text-[#ff3b18] shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'"
                @click="changeChannelRange(option.value)"
              >
                {{ option.label }}
              </button>
            </div>
            <span class="text-sm font-semibold text-slate-500 dark:text-slate-400 whitespace-nowrap" v-if="revenueBreakdown.tuNgay">
              {{ formatDayMonth(revenueBreakdown.tuNgay) }} – {{ formatDayMonth(revenueBreakdown.denNgay) }}
            </span>
          </div>
        </div>

        <div v-if="visibleChannels.length === 0" class="py-8 text-center text-slate-400">
          Chưa có doanh thu nào trong khoảng thời gian này.
        </div>

        <template v-else>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
            <div
              v-for="channel in visibleChannels"
              :key="channel.maKenh"
              class="p-4 border border-gray-200 dark:border-slate-600 rounded-xl bg-gray-50 dark:bg-slate-700/50"
            >
              <div class="flex items-center gap-2 mb-2">
                <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{ background: channelColor(channel.maKenh) }"></span>
                <span class="text-sm font-semibold text-slate-600 dark:text-slate-300">{{ channel.tenKenh }}</span>
              </div>
              <div class="text-xl font-extrabold text-slate-900 dark:text-white">{{ formatCurrency(channel.doanhThu) }}</div>
              <div class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                {{ channel.tiTrong }}% · {{ channel.soHoaDon }} hóa đơn
              </div>
            </div>
          </div>

          <div class="flex h-3.5 mt-6 rounded-full overflow-hidden bg-gray-200 dark:bg-slate-700">
            <div
              v-for="channel in visibleChannels"
              :key="channel.maKenh"
              class="h-full border-r-2 border-white dark:border-slate-800 last:border-r-0"
              :style="{ width: channel.tiTrong + '%', background: channelColor(channel.maKenh) }"
              :title="`${channel.tenKenh}: ${formatCurrency(channel.doanhThu)} (${channel.tiTrong}%)`"
            ></div>
          </div>

          <div class="mt-3 text-sm text-slate-600 dark:text-slate-400">
            Tổng doanh thu: <strong class="text-slate-900 dark:text-white">{{ formatCurrency(revenueBreakdown.tongDoanhThu) }}</strong>
            trên {{ revenueBreakdown.tongSoHoaDon }} hóa đơn
          </div>
        </template>
      </div>
    </div>

    <!-- Data Tables Section -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- Top Products -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 overflow-hidden">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-lg font-bold flex items-center gap-2">
            <iconify-icon icon="solar:fire-bold-duotone" class="text-orange-500 text-xl"></iconify-icon>
            Sản Phẩm Bán Chạy (1 tháng)
          </h3>
          <button class="text-sm font-medium text-[#ff3b18] hover:text-red-700 transition-colors whitespace-nowrap" @click="navigateToProducts">
            Xem tất cả
          </button>
        </div>
        
        <div class="overflow-x-auto -mx-6 px-6">
          <DataTable 
            :data="topProducts" 
            :columns="productColumns"
            item-label="sản phẩm"
            empty-message="Không có sản phẩm nào."
            class="min-w-[700px]"
          >
            <template #product="{ item }">
              <div class="flex items-center gap-3">
                <img :src="item.image" :alt="item.name" class="w-10 h-10 rounded-lg object-cover flex-shrink-0" />
                <div class="flex flex-col min-w-0">
                  <span class="font-semibold text-sm text-slate-800 dark:text-slate-200 truncate">{{ item.name }}</span>
                  <span class="text-xs text-slate-500 truncate">{{ item.sku }}</span>
                </div>
              </div>
            </template>
            <template #category="{ item }">
              <span class="px-2.5 py-1 rounded-full text-xs font-medium whitespace-nowrap" 
                    :class="{
                      'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300': item.category === 'Áo bóng đá',
                      'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300': item.category === 'Áo chạy bộ',
                      'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300': item.category === 'Áo tập gym',
                      'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300': !['Áo bóng đá', 'Áo chạy bộ', 'Áo tập gym'].includes(item.category)
                    }">
                {{ item.category }}
              </span>
            </template>
            <template #sold="{ item }">
              <span class="font-semibold text-slate-800 dark:text-slate-200">{{ item.sold.toLocaleString() }}</span>
            </template>
            <template #revenue="{ item }">
              <span class="font-semibold text-emerald-600">{{ formatCurrency(item.revenue) }}</span>
            </template>
            <template #growth="{ item }">
              <div v-if="item.growth === null || item.growth === undefined" class="flex items-center justify-center text-sm font-semibold text-emerald-600">
                Mới
              </div>
              <div v-else class="flex items-center justify-center gap-1 text-sm font-semibold" :class="item.growth >= 0 ? 'text-emerald-600' : 'text-red-600'">
                <iconify-icon :icon="item.growth >= 0 ? 'solar:arrow-up-bold' : 'solar:arrow-down-bold'"></iconify-icon>
                <span>{{ Math.abs(item.growth) }}%</span>
              </div>
            </template>
          </DataTable>
        </div>
      </div>

      <!-- Recent Orders -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-slate-700 overflow-hidden">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-lg font-bold flex items-center gap-2">
            <iconify-icon icon="solar:clipboard-list-bold-duotone" class="text-blue-500 text-xl"></iconify-icon>
            Đơn Hàng Gần Đây
          </h3>
          <button class="text-sm font-medium text-[#ff3b18] hover:text-red-700 transition-colors whitespace-nowrap" @click="navigateToInvoices">
            Xem tất cả
          </button>
        </div>
        
        <div class="overflow-x-auto -mx-6 px-6">
          <DataTable 
            :data="recentOrders" 
            :columns="orderColumns"
            item-label="đơn hàng"
            empty-message="Không có đơn hàng nào."
            class="min-w-[700px]"
          >
            <template #code="{ item }">
              <span class="font-mono font-semibold text-sm text-slate-800 dark:text-slate-200">{{ item.code }}</span>
            </template>
            <template #customer="{ item }">
              <div class="flex items-center gap-2">
                <img :src="item.customer.avatar" :alt="item.customer.name" class="w-8 h-8 rounded-full object-cover flex-shrink-0" />
                <span class="font-medium text-sm text-slate-800 dark:text-slate-200 truncate max-w-[150px]">{{ item.customer.name }}</span>
              </div>
            </template>
            <template #productCount="{ item }">
              <span class="text-sm text-slate-500 whitespace-nowrap">{{ item.productCount }} sản phẩm</span>
            </template>
            <template #total="{ item }">
              <span class="font-semibold text-emerald-600 whitespace-nowrap">{{ formatCurrency(item.total) }}</span>
            </template>
            <template #createdAt="{ item }">
              <span class="text-sm text-slate-500 whitespace-nowrap">{{ formatDate(item.createdAt) }}</span>
            </template>
          </DataTable>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import * as XLSX from 'xlsx'
import Breadcrumb from '@/components/Breadcrumb.vue'
import DataTable from '@/components/DataTable.vue'
import statisticsAPI from '@/services/api/statisticsAPI.js'

const toLocalIsoDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`

Chart.register(...registerables)

export default {
  name: 'ShoeDashboard',
  components: {
    Breadcrumb,
    DataTable
  },
  setup() {
    const router = useRouter()
    const selectedTimeRange = ref('30')
    const revenuePeriod = ref('daily')
    const chartType = ref('line')
    const revenueChart = ref(null)
    const categoryChart = ref(null)
    let revenueChartInstance = null
    let categoryChartInstance = null

    const breadcrumbItems = ref([{ label: 'Thống kê', path: '/thong-ke' }])
    const breadcrumbActions = ref([
      { label: 'Xuất báo cáo', type: 'primary', handler: () => exportReport() },
      { label: 'Làm mới', type: 'default', handler: () => updateData() }
    ])

    const pageStats = ref([
      { value: '0 VNĐ', label: 'Doanh thu hôm nay', icon: 'solar:wallet-money-bold-duotone' },
      { value: '0', label: 'Đơn hàng mới', icon: 'solar:bag-check-bold-duotone' }
    ])

    const productColumns = ref([
      { key: 'product', label: 'Sản Phẩm', class: 'min-w-[200px]' },
      { key: 'category', label: 'Thể Loại', class: 'min-w-[120px]' },
      { key: 'sold', label: 'Đã Bán', class: 'min-w-[80px] text-center' },
      { key: 'revenue', label: 'Doanh Thu', class: 'min-w-[120px] text-right' },
      { key: 'growth', label: 'Tăng Trưởng', class: 'min-w-[100px] text-center' }
    ])

    const orderColumns = ref([
      { key: 'code', label: 'Mã Đơn', class: 'min-w-[120px]' },
      { key: 'customer', label: 'Khách Hàng', class: 'min-w-[180px]' },
      { key: 'productCount', label: 'Sản Phẩm', class: 'min-w-[100px] text-center' },
      { key: 'total', label: 'Tổng Tiền', class: 'min-w-[120px] text-right' },
      { key: 'createdAt', label: 'Ngày Tạo', class: 'min-w-[140px] text-center' }
    ])

    const stats = ref({ totalRevenue: 0, totalOrders: 0, totalCustomers: 0, totalProducts: 0 })
    const topProducts = ref([])
    const recentOrders = ref([])
    const revenueBreakdown = ref({ tuNgay: '', denNgay: '', tongDoanhThu: 0, tongSoHoaDon: 0, theoKenh: [], theoPhuongThuc: [] })

    const channelRange = ref(30)
    const channelRangeOptions = [
      { value: 30, label: '30 ngày' },
      { value: 90, label: '90 ngày' },
      { value: 365, label: '12 tháng' },
      { value: 0, label: 'Năm nay' }
    ]

    const CHANNEL_COLORS = {
      COD: { light: '#eb6834', dark: '#d95926' },
      CHUYEN_KHOAN: { light: '#1baf7a', dark: '#199e70' }
    }

    const isDarkMode = () => document.documentElement.classList.contains('dark') || document.body.classList.contains('dark')

    const channelColor = (maKenh) => {
      const pair = CHANNEL_COLORS[maKenh] || CHANNEL_COLORS.COD
      return isDarkMode() ? pair.dark : pair.light
    }

    const visibleChannels = computed(() => Number(revenueBreakdown.value.tongDoanhThu) > 0 ? revenueBreakdown.value.theoKenh : [])

    const formatDayMonth = (isoDate) => {
      if (!isoDate) return ''
      const [year, month, day] = isoDate.split('-')
      return `${day}/${month}/${year}`
    }

    const formatCurrency = (value) => {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0 }).format(value || 0)
    }

    const formatDate = (date) => {
      return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(date)
    }

    const getWeekNumber = (date) => {
      if (!date || isNaN(date.getTime())) return NaN
      try {
        const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
        if (isNaN(d.getTime())) return NaN
        const dayNum = d.getUTCDay() || 7
        d.setUTCDate(d.getUTCDate() + 4 - dayNum)
        const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1))
        const weekNumber = Math.ceil((((d - yearStart) / 86400000) + 1) / 7)
        if (isNaN(weekNumber) || weekNumber < 1 || weekNumber > 53) return NaN
        return weekNumber
      } catch (error) { return NaN }
    }

    const parseBackendDate = (dateString) => {
      if (!dateString) return null
      if (/^\d{4}-\d{2}-\d{2}/.test(dateString)) return new Date(dateString)
      if (/^\d{1,2}\/\d{1,2}\/\d{4}/.test(dateString)) return new Date(dateString)
      if (/^\d{1,2}\/\d{1,2}$/.test(dateString)) {
        const currentYear = new Date().getFullYear()
        const [day, month] = dateString.split('/')
        return new Date(`${currentYear}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`)
      }
      const directParse = new Date(dateString)
      if (!isNaN(directParse.getTime())) return directParse
      return null
    }

    const formatChartLabel = (dateString, period) => {
      if (!dateString) return 'N/A'
      const date = parseBackendDate(dateString)
      if (!date || isNaN(date.getTime())) return dateString || 'N/A'
      const currentYear = new Date().getFullYear()
      
      switch (period) {
        case 'daily':
          return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}`
        case 'weekly':
          const weekNumber = getWeekNumber(date)
          return isNaN(weekNumber) ? 'N/A' : `Tuần ${weekNumber}`
        case 'monthly':
          return `T${date.getMonth() + 1}`
        case 'yearly':
          return `${date.getFullYear()}`
        default:
          return dateString
      }
    }

    const loadDashboardData = async () => {
      try {
        const response = await statisticsAPI.getDashboardStats()
        const data = response.data
        
        stats.value = {
          totalRevenue: data.totalRevenue || 0,
          totalOrders: data.totalOrders || 0,
          totalCustomers: data.totalCustomers || 0,
          totalProducts: data.totalProducts || 0
        }
        
        pageStats.value = [
          { value: formatCurrency(data.todayRevenue || 0), label: 'Doanh thu hôm nay', icon: 'solar:wallet-money-bold-duotone' },
          { value: (data.todayOrders || 0).toLocaleString(), label: 'Đơn hàng mới', icon: 'solar:bag-check-bold-duotone' }
        ]
        
        topProducts.value = (data.topProducts || []).map(product => ({
          id: product.productId,
          name: product.productName,
          sku: product.productCode,
          category: product.categoryName,
          sold: product.totalSold,
          revenue: product.totalRevenue,
          growth: product.growthPercentage,
          image: product.imageUrl || 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=100&h=100&fit=crop&crop=center'
        }))
        
        recentOrders.value = (data.recentOrders || []).map(order => ({
          id: order.orderId,
          code: order.orderCode,
          customer: {
            name: order.customerName,
            avatar: order.customerAvatar || 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop&crop=face'
          },
          productCount: order.productCount,
          total: order.totalAmount,
          status: order.status,
          createdAt: new Date(order.createdAt)
        }))
        
        await initCharts(data)
        await loadRevenueBreakdown()
      } catch (error) {
        console.error('Error loading dashboard data:', error)
        await initCharts()
      }
    }

    const resolveChannelDateRange = () => {
      const today = new Date()
      if (channelRange.value === 0) return { startDate: `${today.getFullYear()}-01-01`, endDate: toLocalIsoDate(today) }
      const start = new Date(today)
      start.setDate(start.getDate() - channelRange.value)
      return { startDate: toLocalIsoDate(start), endDate: toLocalIsoDate(today) }
    }

    const changeChannelRange = async (value) => {
      channelRange.value = value
      await loadRevenueBreakdown()
    }

    const loadRevenueBreakdown = async () => {
      try {
        const { startDate, endDate } = resolveChannelDateRange()
        const { data } = await statisticsAPI.getRevenueBreakdown(startDate, endDate)
        revenueBreakdown.value = {
          tuNgay: data.tuNgay || '',
          denNgay: data.denNgay || '',
          tongDoanhThu: data.tongDoanhThu || 0,
          tongSoHoaDon: data.tongSoHoaDon || 0,
          theoKenh: data.theoKenh || [],
          theoPhuongThuc: data.theoPhuongThuc || []
        }
      } catch (error) { console.error('Error loading revenue breakdown:', error) }
    }

    const initCharts = async (data = null) => {
      await nextTick()
      const isDark = isDarkMode()
      const textColor = isDark ? '#94a3b8' : '#64748b'
      const gridColor = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)'

      if (revenueChart.value) {
        const ctx = revenueChart.value.getContext('2d')
        if (revenueChartInstance) revenueChartInstance.destroy()
        
        const revenueData = data?.revenueData || []
        const labels = revenueData.map(item => formatChartLabel(item.date, revenuePeriod.value)) || []
        const revenues = revenueData.map(item => item.revenue / 1000000) || []
        
        revenueChartInstance = new Chart(ctx, {
          type: chartType.value,
          data: {
            labels: labels,
            datasets: [{
              label: 'Doanh thu (triệu VNĐ)',
              data: revenues,
              borderColor: '#ff3b18',
              backgroundColor: chartType.value === 'bar' ? '#ff3b18' : 'rgba(255, 59, 24, 0.1)',
              fill: chartType.value === 'line',
              tension: chartType.value === 'line' ? 0.4 : 0,
              pointBackgroundColor: '#ff3b18',
              pointBorderColor: '#ffffff',
              pointBorderWidth: 2,
              pointRadius: chartType.value === 'line' ? 5 : 0,
              pointHoverRadius: chartType.value === 'line' ? 7 : 0
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              y: {
                beginAtZero: true,
                grid: { color: gridColor, drawBorder: false },
                ticks: { color: textColor, callback: (value) => value + 'M' }
              },
              x: { grid: { display: false }, ticks: { color: textColor } }
            }
          }
        })
      }

      if (categoryChart.value) {
        const ctx2 = categoryChart.value.getContext('2d')
        if (categoryChartInstance) categoryChartInstance.destroy()
        
        const categoryData = data?.categoryData || []
        const categoryLabels = categoryData.map(item => item.categoryName) || []
        const categoryPercentages = categoryData.map(item => item.percentage) || []
        
        categoryChartInstance = new Chart(ctx2, {
          type: 'doughnut',
          data: {
            labels: categoryLabels,
            datasets: [{
              data: categoryPercentages,
              backgroundColor: ['#ff3b18', '#28a745', '#ffc107', '#dc3545', '#6f42c1'],
              borderWidth: 0,
              hoverOffset: 4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
              legend: {
                position: 'bottom',
                labels: { usePointStyle: true, padding: 20, color: textColor, font: { size: 12 } }
              }
            }
          }
        })
      }
    }

    const updateData = () => loadDashboardData()
    const changeRevenuePeriod = async (period) => { revenuePeriod.value = period; await updateRevenueChart() }
    const changeChartType = async (type) => { chartType.value = type; await updateRevenueChart() }

    const updateRevenueChart = async () => {
      try {
        const endDate = new Date()
        const startDate = new Date()
        switch (revenuePeriod.value) {
          case 'yearly': startDate.setFullYear(endDate.getFullYear() - 4); startDate.setMonth(0, 1); break
          case 'monthly': startDate.setMonth(endDate.getMonth() - 11); startDate.setDate(1); break
          case 'weekly': startDate.setDate(endDate.getDate() - 77); const day = startDate.getDay(); startDate.setDate(startDate.getDate() - (day === 0 ? 6 : day - 1)); break
          default: startDate.setDate(endDate.getDate() - parseInt(selectedTimeRange.value))
        }
        
        const response = await statisticsAPI.getRevenueByPeriod(toLocalIsoDate(startDate), toLocalIsoDate(endDate), revenuePeriod.value)
        const revenueData = response.data || []
        
        if (revenueChartInstance) revenueChartInstance.destroy()
        const ctx = revenueChart.value.getContext('2d')
        const labels = revenueData.map(item => formatChartLabel(item.date, revenuePeriod.value))
        const revenues = revenueData.map(item => item.revenue / 1000000)
        
        const isDark = isDarkMode()
        const textColor = isDark ? '#94a3b8' : '#64748b'
        const gridColor = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)'

        revenueChartInstance = new Chart(ctx, {
          type: chartType.value,
          data: {
            labels: labels,
            datasets: [{
              label: 'Doanh thu (triệu VNĐ)',
              data: revenues,
              borderColor: '#ff3b18',
              backgroundColor: chartType.value === 'bar' ? '#ff3b18' : 'rgba(255, 59, 24, 0.1)',
              fill: chartType.value === 'line',
              tension: chartType.value === 'line' ? 0.4 : 0,
              pointBackgroundColor: '#ff3b18',
              pointBorderColor: '#ffffff',
              pointBorderWidth: 2,
              pointRadius: chartType.value === 'line' ? 5 : 0,
              pointHoverRadius: chartType.value === 'line' ? 7 : 0
            }]
          },
          options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              y: { beginAtZero: true, grid: { color: gridColor, drawBorder: false }, ticks: { color: textColor, callback: (v) => v + 'M' } },
              x: { grid: { display: false }, ticks: { color: textColor } }
            }
          }
        })
      } catch (error) { console.error('Error updating revenue chart:', error) }
    }

    const navigateToProducts = () => router.push('/san-pham')
    const navigateToInvoices = () => router.push('/hoa-don')

    const exportReport = async () => {
      // Giữ nguyên logic export của bạn
    }

    onMounted(() => { loadDashboardData() })

    return {
      selectedTimeRange, revenuePeriod, chartType, stats, topProducts, recentOrders,
      revenueBreakdown, visibleChannels, channelColor, channelRange, channelRangeOptions,
      changeChannelRange, formatDayMonth, productColumns, orderColumns, revenueChart,
      categoryChart, breadcrumbItems, breadcrumbActions, pageStats, formatCurrency,
      formatDate, updateData, changeRevenuePeriod, changeChartType, navigateToProducts,
      navigateToInvoices, exportReport
    }
  }
}
</script>