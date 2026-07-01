package com.jiangnan.travel.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.entity.OperationLog;
import com.jiangnan.travel.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint point, LogOperation logOperation) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();

        OperationLog entity = new OperationLog();
        entity.setOperation(logOperation.value().isEmpty() ? signature.getMethod().getName() : logOperation.value());
        entity.setMethod(getRequestMethod());
        entity.setPath(getRequestPath());
        entity.setIp(getClientIp());
        entity.setRequestParams(logOperation.saveRequestParams() ? getRequestParams(point) : null);
        entity.setCreateTime(LocalDateTime.now());

        fillUserInfo(entity);

        Object result;
        try {
            result = point.proceed();
            entity.setStatus(1);
        } catch (Throwable e) {
            entity.setStatus(0);
            entity.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : null);
            throw e;
        } finally {
            entity.setDurationMs((int) (System.currentTimeMillis() - start));
            try {
                operationLogMapper.insert(entity);
            } catch (Exception ex) {
                log.warn("操作日志记录失败", ex);
            }
        }
        return result;
    }

    private void fillUserInfo(OperationLog entity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }
        try {
            entity.setUserId((Long) auth.getPrincipal());
            if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                entity.setRole(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
            }
        } catch (Exception e) {
            log.debug("获取操作人信息失败", e);
        }
    }

    private String getRequestMethod() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getMethod() : null;
    }

    private String getRequestPath() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURI() : null;
    }

    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

    // ponytail: parameter-level sanitization; full DTO field walker if false-positives become a problem
    private static final Set<String> SENSITIVE_PARAM_PATTERNS = Set.of(
        "password", "oldpassword", "newpassword", "pwd", "passwd",
        "secret", "token", "apikey", "code"
    );

    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            String[] paramNames = ((MethodSignature) point.getSignature()).getParameterNames();
            Object[] sanitized = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    sanitized[i] = null;
                } else if (paramNames != null && i < paramNames.length && isSensitive(paramNames[i])) {
                    sanitized[i] = "***";
                } else {
                    sanitized[i] = args[i];
                }
            }
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSensitive(String paramName) {
        String lower = paramName.toLowerCase().replace("_", "");
        return SENSITIVE_PARAM_PATTERNS.stream().anyMatch(lower::contains);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
