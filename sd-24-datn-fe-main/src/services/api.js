import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Local backend invoice operations
export const getInvoices = () => api.get('/invoices')
export const updateInvoice = (id, data) => api.patch(`/invoices/${id}`, data)
export const deleteInvoice = (id) => api.delete(`/invoices/${id}`)
export const createInvoice = (data) => api.post('/invoices', data)

// Online orders from Mokky
export const getOnlineOrders = () => axios.get('https://a5687b208ca7ac57.mokky.dev/orders')