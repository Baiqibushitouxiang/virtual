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

    // 成功且无数据返回
    public static Result success() {
        return new Result(Constants.CODE_200, "操作成功", null);
    }

    // 成功并返回数据
    public static Result success(Object data) {
        return new Result(Constants.CODE_200, "操作成功", data);
    }

    // 错误返回，带有错误码和详细信息
    public static Result error(String code, String msg) {
        return new Result(code, msg, null);
    }

    // 错误返回，带有默认错误码
    public static Result error(String msg) {
        return new Result(Constants.CODE_500, msg, null);
    }

    // 错误返回，带有默认错误码和系统错误信息
    public static Result error() {
        return new Result(Constants.CODE_500, "系统错误", null);
    }

    // 添加警告信息的返回
    public static Result warning(String msg) {
        return new Result(Constants.CODE_400, msg, null);
    }

    // 其他类型的返回方法
}
