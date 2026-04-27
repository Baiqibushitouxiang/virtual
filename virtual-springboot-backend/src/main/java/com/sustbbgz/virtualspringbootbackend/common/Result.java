package com.sustbbgz.virtualspringbootbackend.common;

public class Result {

    private String code;  // 状态码
    private String msg;   // 提示信息
    private Object data;  // 返回的数据

    public Result() {
    }

    public Result(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static Result success() {
        return new Result(Constants.CODE_200, "操作成功", null);
    }

    public static Result success(Object data) {
        return new Result(Constants.CODE_200, "操作成功", data);
    }

    public static Result error(String code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result error(String msg) {
        return new Result(Constants.CODE_500, msg, null);
    }

    public static Result error() {
        return new Result(Constants.CODE_500, "系统错误", null);
    }

    public static Result warning(String msg) {
        return new Result(Constants.CODE_400, msg, null);
    }
}
