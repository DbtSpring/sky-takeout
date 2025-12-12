package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice  //ControllerAdvice是全局增强注解，对所有的Controller生效
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    //增加新员工的时候，key重复
    //eg：Duplicate entry 'test1' for key 'employee.idx_username'
    //注意：优先精确匹配原则，如果这个匹配了，还有其他的Exception的Handler，则不触发
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        log.error("sql数据库异常：{}",message);
        if(message.contains("Duplicate entry")){
            String[] words = message.split(" ");
            String returnMessage = words[2] + MessageConstant.EMPLOYEE_ALREADY_EXISTS;
            return Result.error(returnMessage);

        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }


}
