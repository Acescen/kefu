package com.kefu.common.exception.user;

/**
 * 用户不存在异常
 * @author jurui
 * @date 2020-05-28
 */
public class UserNotExistsException extends RuntimeException {

    private static final long serialVersionUID = -6523935380310578412L;

    public UserNotExistsException(String message) {
        super(message);
    }
}
