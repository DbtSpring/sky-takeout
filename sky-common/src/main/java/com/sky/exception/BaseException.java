package com.sky.exception;

/**
 * 业务异常
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);     //让父类（RuntimeException）初始化时保存错误信息 msg
    }

}
