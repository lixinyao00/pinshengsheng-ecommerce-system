import request from '../utils/request'

// 商城和后台共用认证服务，登录结果由页面按角色决定去向
export function login(data) {
  return request.post('/api/auth/login', data)
}

// 页面刷新后可用这个接口确认当前 Token 是否仍然有效
export function getProfile() {
  return request.get('/api/auth/profile')
}
