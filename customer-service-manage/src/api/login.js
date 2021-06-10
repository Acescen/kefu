import request from '@/utils/request'

export default {
  /**
   * 登录
   * @param {string} username 账号
   * @param {string} password 密码
   */
  login(username, password) {
    return request({
      url: '/auth/login',
      method: 'post',
      data: { username, password }
    })
  }
}
