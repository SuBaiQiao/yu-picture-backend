package com.subaiqiao.yupicture.infrastructure.aop;

import cn.hutool.core.util.ObjUtil;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.domain.user.valueobject.UserRoleEnum;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserApplicationService userApplicationService;

    /**
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ThrowUtils.throwIf(ObjUtil.isNull(requestAttributes), ErrorCode.NOT_LOGIN_ERROR);
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userApplicationService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (ObjUtil.isEmpty(mustRoleEnum)) {
            return joinPoint.proceed();
        }
        // 以下的代码，必须有权限才会通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (ObjUtil.isEmpty(userRoleEnum)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        // 要求角色为管理员，但是当前用户角色不是管理员
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return joinPoint.proceed();
    }
}
