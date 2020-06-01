package cn.keats.service_consumer.transformat;

import cn.keats.service_consumer.exception.KeatsException;
import lombok.Data;

/**
 * 功能：统一返回结果，直接调用对应的工厂方法
 *
 * @author Keats
 * @date 2019/11/29 18:20
 */
@Data
public class Result<T>  {
    private Integer code;
    private String msg;
    private T data;

    /**
     * 如果定义为 private 则 Json 格式化会出错，故使用公有构造方法
     */
    public Result() {
    }

    /**
     * 功能：响应成功
     *
     * @param data 响应的数据
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 8:54
     */
    public static <T> Result<T> OK(T data){
        return new Result<>(0, "响应成功", data);
    }

    private static Result errorResult;
    private static Result timeOut;
    /**
     * 功能：返回错误，此错误不可定制，全局唯一。一般是代码出了问题，需要修改代码
     *
     * @param
     * @return Result
     * @author Keats
     * @date 2019/11/30 8:55
     */
    public static Result Error(){
        if(errorResult == null){
            synchronized (Result.class){
                if(errorResult == null){
                    synchronized (Result.class){
                        errorResult = new Result<>(-1, "未知错误", null);
                    }
                }
            }
        }
        return errorResult;
    }


    /**
     * 功能：返回错误，微服务调用失败
     *
     * @param
     * @return Result
     * @author Keats
     * @date 2019/11/30 8:55
     */
    public static Result ReadTimeOut(){
        if(timeOut == null){
            synchronized (Result.class){
                if(timeOut == null){
                    synchronized (Result.class){
                        timeOut = new Result<>(4004, "远程服务调用失败", null);
                    }
                }
            }
        }
        return timeOut;
    }

    /**
     * 功能：返回异常，直接甩自定义异常类进来
     *
     * @param e 自定义异常类
	 * @param data 数据，如果没有填入 null 即可
     * @return woke.cloud.property.transformat.Result<T>
     * @author Keats
     * @date 2019/11/30 8:55
     */
    public static <T> Result<T> Exception(KeatsException e, T data){
        return new Result<>(e.getCode(), e.getMsg(), data);
    }
    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 功能：为了方便使用，使用静态工厂方法创建对象。如需新的构造方式，请添加对应的静态工厂方法
     *
     * @author Keats
     * @date 2019/11/30 8:56
     */
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


}
