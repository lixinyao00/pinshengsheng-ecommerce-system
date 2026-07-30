import axios from 'axios'

// 创建统一请求实例；请求仍由 Vite 的 /api 代理转发给 Gateway
const request = axios.create({
    timeout: 5000
})

// 每次请求前，从本地读取 Token 并自动放进请求头
request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

// 统一把后端返回的 { code, message, data } 交给页面处理
request.interceptors.response.use((response) => {
    return response.data
})

export default request