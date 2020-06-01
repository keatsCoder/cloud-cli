package cn.keats.service_consumer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能：异常枚举
 *
 * @author Keats
 * @date 2019/11/29 18:49
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    PARAM_EXCEPTION(10001,"参数异常"),
    NUM_LESS_THAN_MIN(10002,"数值小于最小值");

    private Integer code;
    private String msg;

}
