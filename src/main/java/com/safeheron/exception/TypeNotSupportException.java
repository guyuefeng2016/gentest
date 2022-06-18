package com.safeheron.exception;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-02-16 17:07
 **/
public class TypeNotSupportException extends RuntimeException {
    private static final long serialVersionUID = -8820712790737723733L;
    private String errorMsg;
    private Integer code = 500;
    private String errorTitle;
    private Object[] args;

    public TypeNotSupportException(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public TypeNotSupportException(String errorMsg, Integer code) {
        this.errorMsg = errorMsg;
        this.code = code;
    }

    public TypeNotSupportException(String errorMsg, Integer code, String errorTitle) {
        this.errorMsg = errorMsg;
        this.code = code;
        this.errorTitle = errorTitle;
    }

    public TypeNotSupportException(String errorMsg, Throwable cause) {
        super(cause);
        this.errorMsg = errorMsg;
    }

    public TypeNotSupportException() {
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrorTitle() {
        return this.errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
