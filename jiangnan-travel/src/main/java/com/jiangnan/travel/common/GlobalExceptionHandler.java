package com.jiangnan.travel.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBindException(BindException e) {
        String msg = extractFieldErrors(e.getBindingResult().getFieldErrors()
                .stream().map(err -> err.getField() + ": " + err.getDefaultMessage()));
        log.warn("参数校验异常: {}", msg);
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = extractFieldErrors(e.getBindingResult().getFieldErrors()
                .stream().map(err -> err.getField() + ": " + err.getDefaultMessage()));
        log.warn("请求体校验失败: {}", msg);
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    private String extractFieldErrors(java.util.stream.Stream<String> fieldErrors) {
        return fieldErrors.reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("路径参数类型错误: name={}, value={}", e.getName(), e.getValue());
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), "请求参数类型错误: " + e.getName());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
