import request from '../utils/request'

// 用户在指定日期完成签到
export function signIn(userId, day) {
  return request.post(`/api/auth/sign/${userId}/day/${day}`)
}

// 查询用户某一天是否已签到
export function getSignStatus(userId, day) {
  return request.get(`/api/auth/sign/${userId}/day/${day}`)
}

// 查询用户本月累计签到天数
export function getSignCount(userId) {
  return request.get(`/api/auth/sign/${userId}/count`)
}
