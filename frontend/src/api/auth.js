// 引入统一的 Axios 请求对象
import request from '../utils/request'

// 调用后端登录接口
export function login(data) {
    return request.post('/api/auth/login', data)
}

// 调用商城注册接口
export function register(data) {
    return request.post('/api/auth/register', data)
}

// 查询当前登录用户信息
export function getProfile() {
    return request.get('/api/auth/profile')
}
