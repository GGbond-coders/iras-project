/**
 * @file index.js
 * @description 前端 API 模块。
 *              基于 axios 封装 HTTP 请求客户端，提供统一的请求/响应拦截、
 *              Token 自动注入、错误处理等功能。
 *              导出三个 API 模块：authApi（认证）、jobApi（职位）、difyApi（AI 服务）。
 *
 * @author IRAS Team
 * @since 1.0
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * 创建 axios 实例。
 * <p>
 * 配置项：
 * <ul>
 *   <li>baseURL: '/iras' - 请求路径前缀，与后端 context-path 一致</li>
 *   <li>timeout: 300000 - 请求超时时间 5 分钟（Dify AI 推理耗时较长）</li>
 *   <li>headers: 默认 Content-Type 为 application/json</li>
 * </ul>
 * </p>
 */
const api = axios.create({
  baseURL: '/iras',
  timeout: 300000, // 5 分钟超时（Dify AI 推理需要时间）
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器 - 自动在请求头中添加 JWT Token。
 * <p>
 * 从 localStorage 中读取 Token，如果存在则添加到 Authorization 请求头。
 * 格式为：Bearer <token>
 * </p>
 */
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应拦截器 - 统一处理响应数据和错误。
 * <p>
 * 成功响应处理：
 * - 检查业务状态码（code），非 200 时弹出错误提示
 * - 返回响应数据体（data）
 *
 * 错误响应处理：
 * - 401 状态码：清除本地登录信息，跳转到登录页
 * - 其他错误：弹出错误提示消息
 * </p>
 */
api.interceptors.response.use(
  response => {
    const { data } = response
    // 检查业务状态码
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  error => {
    // 处理 401 未授权错误（Token 过期或无效）
    if (error.response?.status === 401) {
      // 清除本地存储的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('email')
      // 跳转到登录页
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      // 其他错误弹出提示
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// ============ 认证 API ============

/**
 * 认证相关 API。
 * <p>
 * 包含用户注册和登录两个接口。
 * </p>
 */
export const authApi = {
  /**
   * 用户注册。
   * @param {Object} data - 注册信息（username, password, email）
   * @returns {Promise} 注册结果（包含 Token 和用户信息）
   */
  register(data) {
    return api.post('/api/auth/register', data)
  },
  /**
   * 用户登录。
   * @param {Object} data - 登录信息（username, password）
   * @returns {Promise} 登录结果（包含 Token 和用户信息）
   */
  login(data) {
    return api.post('/api/auth/login', data)
  }
}

// ============ 职位 API ============

/**
 * 职位相关 API。
 * <p>
 * 包含职位搜索和职位详情查询两个接口。
 * </p>
 */
export const jobApi = {
  /**
   * 搜索职位（支持多条件 + 分页）。
   * @param {Object} params - 搜索参数（jobName, city, salaryMin, salaryMax, page, size）
   * @returns {Promise} 搜索结果（包含职位列表和分页信息）
   */
  search(params) {
    return api.get('/api/jobs/search', { params })
  },
  /**
   * 获取职位详情。
   * @param {number|string} id - 职位 ID
   * @returns {Promise} 职位详情信息
   */
  getById(id) {
    return api.get(`/api/jobs/${id}`)
  }
}

// ============ Dify AI API ============

/**
 * Dify AI 服务相关 API。
 * <p>
 * 包含职能画像分析和简历智能诊断两个接口。
 * 这两个接口的响应时间较长（2-3 分钟）。
 * </p>
 */
export const difyApi = {
  /**
   * 获取职位的职能画像。
   * @param {string} jobName - 职位名称
   * @returns {Promise} AI 生成的职能画像 JSON
   */
  getJobProfile(jobName) {
    return api.post('/api/dify/job-profile', { job_name: jobName })
  },
  /**
   * 上传简历进行智能诊断。
   * @param {File} file - 简历文件对象
   * @returns {Promise} AI 生成的诊断报告 JSON
   */
  diagnoseResume(file) {
    // 构建 FormData 对象用于文件上传
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/dify/diagnose', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }  // 覆盖默认的 Content-Type
    })
  }
}

// 导出 axios 实例（供其他模块直接使用）
export default api
