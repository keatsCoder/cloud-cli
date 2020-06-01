package cn.keats.service_consumer.advice;

import cn.keats.service_consumer.exception.ExceptionEnum;
import cn.keats.service_consumer.exception.KeatsException;
import cn.keats.service_consumer.transformat.Result;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能：全局异常处理器，Controller异常直接抛出
 *
 * @return
 * @author Keats
 * @date 2019/11/30 10:28
 */
@Slf4j
@ControllerAdvice
public class KeatsExceptionAdvice {
    /**
     * 功能：其余非预先规避的异常返回错误
     *
     * @param e
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 10:08
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result ResponseException(Exception e) {
        if(!(e instanceof java.io.IOException)){
            // 屏蔽烦人的
            log.error("未知错误，错误信息：", e);
        }

        return Result.Error();
    }


    /**
     * 功能：其余非预先规避的异常返回错误
     *
     * @param e
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 10:08
     */
    @ExceptionHandler(value = RetryableException.class)
    @ResponseBody
    public Result ResponseException(RetryableException e) {
        log.error("远程服务调用失败: " + e);
        return Result.ReadTimeOut();
    }

    /**
     * 功能：捕捉到 因为 返回对应的消息
     *
     * @param e
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 10:07
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public Result paramException(MissingServletRequestParameterException e) {
        log.warn("必传参数没传: "+ e);
        return Result.Exception(new KeatsException(ExceptionEnum.PARAM_EXCEPTION), null);
    }

    /**
     * 功能：捕捉到 因为 返回对应的消息
     *
     * @param e
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 10:07
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Result paramException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法错误: " + e);
        return Result.Exception(new KeatsException(1, "请求方法错误！"), null);
    }

    /**
     * 功能：捕捉到 KeatsException 返回对应的消息
     *
     * @param e
     * @return woke.cloud.property.transformat.Result
     * @author Keats
     * @date 2019/11/30 10:07
     */
    @ExceptionHandler(value = KeatsException.class)
    @ResponseBody
    public Result myException(KeatsException e) {
        log.warn("返回自定义异常：异常代码：" + e.getCode() + "异常信息：" + e.getMsg(), e);
        return Result.Exception(e, null);
    }
}
