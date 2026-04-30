import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const api = axios.create({
  baseURL: '/iras',
  timeout: 300000, // 5分钟（Dify推理需要时间）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 自动添加 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('email')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// ============ 认证 API ============
export const authApi = {
  register(data) {
    return api.post('/api/auth/register', data)
  },
  login(data) {
    return api.post('/api/auth/login', data)
  }
}

// ============ 职位 API ============
export const jobApi = {
  search(params) {
    return api.get('/api/jobs/search', { params })
  },
  getById(id) {
    return api.get(`/api/jobs/${id}`)
  }
}

// ============ Dify API ============
export const difyApi = {
  getJobProfile(jobName) {
    return api.post('/api/dify/job-profile', { job_name: jobName })
  },
  diagnoseResume(resumeText) {
    return api.post('/api/dify/diagnose', { resume_text: resumeText })
  }
}

export default api
