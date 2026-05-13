/**
 * @file user.js
 * @description 用户状态管理模块（Pinia Store）。
 *              使用 Pinia 管理全局用户状态，包括 Token、用户名、邮箱。
 *              状态数据持久化到 localStorage，确保页面刷新后登录状态不丢失。
 *
 * @author IRAS Team
 * @since 1.0
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态 Store。
 * <p>
 * 使用 Pinia 的组合式 API（Composition API）风格定义。
 * 管理以下状态：
 * <ul>
 *   <li>token - JWT 认证令牌</li>
 *   <li>username - 用户名</li>
 *   <li>email - 邮箱</li>
 * </ul>
 * 提供两个操作方法：
 * <ul>
 *   <li>setUser - 设置用户信息（登录/注册成功后调用）</li>
 *   <li>logout - 清除用户信息（退出登录时调用）</li>
 * </ul>
 * </p>
 */
export const useUserStore = defineStore('user', () => {
  /**
   * JWT Token，从 localStorage 初始化（页面刷新时恢复登录状态）。
   * @type {import('vue').Ref<string>}
   */
  const token = ref(localStorage.getItem('token') || '')

  /**
   * 用户名，从 localStorage 初始化。
   * @type {import('vue').Ref<string>}
   */
  const username = ref(localStorage.getItem('username') || '')

  /**
   * 邮箱，从 localStorage 初始化。
   * @type {import('vue').Ref<string>}
   */
  const email = ref(localStorage.getItem('email') || '')

  /**
   * 设置用户信息（登录/注册成功后调用）。
   * <p>
   * 同时更新内存状态和 localStorage 持久化存储。
   * </p>
   *
   * @param {Object} userData - 用户数据对象
   * @param {string} userData.token - JWT Token
   * @param {string} userData.username - 用户名
   * @param {string} [userData.email] - 邮箱（可选）
   */
  function setUser(userData) {
    // 更新内存状态
    token.value = userData.token
    username.value = userData.username
    email.value = userData.email || ''
    // 持久化到 localStorage
    localStorage.setItem('token', userData.token)
    localStorage.setItem('username', userData.username)
    localStorage.setItem('email', userData.email || '')
  }

  /**
   * 退出登录，清除所有用户信息。
   * <p>
   * 同时清除内存状态和 localStorage 中的持久化数据。
   * </p>
   */
  function logout() {
    // 清除内存状态
    token.value = ''
    username.value = ''
    email.value = ''
    // 清除 localStorage 持久化数据
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  // 返回需要暴露的状态和方法
  return { token, username, email, setUser, logout }
})
