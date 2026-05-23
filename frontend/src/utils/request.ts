import axios, { type AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
  code: string | null
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

function formatTime() {
  return new Date().toLocaleString('zh-CN', { hour12: false })
}

service.interceptors.request.use((config) => {
  const method = (config.method || 'GET').toUpperCase()
  const fullUrl = config.url?.startsWith('http') ? config.url : `${config.baseURL}${config.url}`
  console.log(`%c[请求] ${formatTime()} ${method} ${fullUrl}`, 'color: #32f08c; font-weight: bold', {
    params: config.params,
    data: config.data
  })
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    const method = (response.config.method || 'GET').toUpperCase()
    const fullUrl = response.config.url?.startsWith('http')
      ? response.config.url
      : `${response.config.baseURL}${response.config.url}`

    if (!body.success) {
      const message = body.message || '请求失败'
      console.error(`%c[响应错误] ${formatTime()} ${method} ${fullUrl}`, 'color: #f0c832; font-weight: bold', body)
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }

    console.log(`%c[响应] ${formatTime()} ${method} ${fullUrl}`, 'color: #3298f0; font-weight: bold', {
      data: body.data
    })
    return body.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    console.error(`%c[响应异常] ${formatTime()}`, 'color: #f05555; font-weight: bold', error)
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
