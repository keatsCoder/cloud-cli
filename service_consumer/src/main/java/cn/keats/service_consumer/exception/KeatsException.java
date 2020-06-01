package cn.keats.service_consumer.exception;

import lombok.Getter;

/**
 * 功能：物业系统自定义异常类。继承自RuntimeException，方便Spring进行事务回滚
 *
 * @author Keats
 * @date 2019/11/29 18:50
 */
@Getter
public class KeatsException extends RuntimeException{
    private Integer code;
    private String msg;

    public KeatsException(ExceptionEnum eEnum) {
        this.code = eEnum.getCode();
        this.msg = eEnum.getMsg();
    }

    public KeatsException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public KeatsException(String msg) {
        this.msg = msg;
    }
}
