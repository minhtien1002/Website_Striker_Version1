# Cách sử dụng Loading Component

## 1. Import Component

```javascript
import Loading from '@/components/Loading.vue'
```

## 2. Sử dụng trong Template

### Fullscreen Loading (Toàn màn hình)
```vue
<template>
  <div>
    <!-- Nội dung của bạn -->
    <Loading 
      :show="isLoading" 
      text="Đang tải dữ liệu..." 
      subtext="Vui lòng đợi trong giây lát"
    />
  </div>
</template>
```

### Inline Loading (Trong container)
```vue
<template>
  <div class="data-container" style="position: relative;">
    <!-- Nội dung -->
    <div v-if="!isLoading">
      <!-- Dữ liệu của bạn -->
    </div>
    
    <Loading 
      :show="isLoading" 
      text="Đang tải..." 
      :fullscreen="false"
      size="medium"
    />
  </div>
</template>
```

## 3. Tích hợp với API Calls

### Ví dụ trong HoaDon.vue
```javascript
<script setup>
import { ref } from 'vue'
import Loading from '@/components/Loading.vue'
import { getInvoices } from '@/services/api'

const isLoading = ref(false)
const invoices = ref([])

const loadInvoices = async () => {
  try {
    isLoading.value = true
    const response = await getInvoices()
    invoices.value = response.data
  } catch (error) {
    console.error('Lỗi tải hóa đơn:', error)
  } finally {
    isLoading.value = false
  }
}

// Gọi khi component mount
onMounted(() => {
  loadInvoices()
})
</script>

<template>
  <div>
    <!-- Nội dung hóa đơn -->
    <div v-if="!isLoading">
      <!-- Danh sách hóa đơn -->
    </div>
    
    <Loading 
      :show="isLoading" 
      text="Đang tải danh sách hóa đơn..." 
      subtext="Vui lòng đợi trong giây lát"
    />
  </div>
</template>
```

## 4. Props của Loading Component

| Prop | Type | Default | Mô tả |
|------|------|---------|-------|
| `show` | Boolean | `true` | Hiển thị/ẩn loading |
| `text` | String | `'Đang tải...'` | Text chính |
| `subtext` | String | `''` | Text phụ |
| `size` | String | `'medium'` | Kích thước: 'small', 'medium', 'large' |
| `fullscreen` | Boolean | `true` | Toàn màn hình hay inline |

## 5. Ví dụ sử dụng trong các trường hợp khác nhau

### Refresh Data (Làm mới dữ liệu)
```javascript
const refreshData = async () => {
  isLoading.value = true
  try {
    // Gọi API để tải lại dữ liệu
    await loadData()
  } finally {
    isLoading.value = false
  }
}
```

### Form Submit
```javascript
const isSubmitting = ref(false)

const submitForm = async () => {
  isSubmitting.value = true
  try {
    await submitAPI(formData.value)
    // Xử lý thành công
  } catch (error) {
    // Xử lý lỗi
  } finally {
    isSubmitting.value = false
  }
}
```

### Multiple Loading States
```javascript
const loadingStates = ref({
  data: false,
  submit: false,
  delete: false
})

// Sử dụng
<Loading :show="loadingStates.data" text="Đang tải dữ liệu..." />
<Loading :show="loadingStates.submit" text="Đang lưu..." />
<Loading :show="loadingStates.delete" text="Đang xóa..." />
```

## 6. Tích hợp với Header.vue

Để thay thế alert trong hàm `refreshData` của Header.vue:

```javascript
// Trong Header.vue
import Loading from './Loading.vue'

const isRefreshing = ref(false)

const refreshData = async () => {
  isRefreshing.value = true
  
  // Simulate loading time
  setTimeout(() => {
    window.location.reload()
  }, 1500)
}

// Template
<Loading 
  :show="isRefreshing" 
  text="Đang làm mới dữ liệu..." 
  subtext="Trang sẽ được tải lại sau giây lát"
/>
```
