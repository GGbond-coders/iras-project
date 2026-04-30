import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const email = ref(localStorage.getItem('email') || '')

  function setUser(userData) {
    token.value = userData.token
    username.value = userData.username
    email.value = userData.email || ''
    localStorage.setItem('token', userData.token)
    localStorage.setItem('username', userData.username)
    localStorage.setItem('email', userData.email || '')
  }

  function logout() {
    token.value = ''
    username.value = ''
    email.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  return { token, username, email, setUser, logout }
})
