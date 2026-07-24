import { defineStore } from 'pinia'

// 集中管理当前登录用户的身份信息
export const useUserStore = defineStore('user', {
    // 页面刷新后，优先从浏览器缓存恢复登录状态
    state: () => ({
        token: localStorage.getItem('token') || '',
        username: localStorage.getItem('username') || '',
        role: localStorage.getItem('role') || ''
    }),

    actions: {
        // 登录成功后，同时更新 Pinia 和浏览器缓存
        setLoginInfo(loginData) {
            this.token = loginData.token
            this.username = loginData.username
            this.role = loginData.role

            localStorage.setItem('token', loginData.token)
            localStorage.setItem('username', loginData.username)
            localStorage.setItem('role', loginData.role)
        },

        // 退出登录时，清空内存和浏览器中的身份信息
        clearLoginInfo() {
            this.token = ''
            this.username = ''
            this.role = ''

            localStorage.removeItem('token')
            localStorage.removeItem('username')
            localStorage.removeItem('role')
        }
    }
})