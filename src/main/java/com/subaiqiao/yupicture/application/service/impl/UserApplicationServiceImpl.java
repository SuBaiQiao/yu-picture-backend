package com.subaiqiao.yupicture.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.domain.user.service.UserDomainService;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.user.UserQueryRequest;
import com.subaiqiao.yupicture.interfaces.dto.user.UserRegisterRequest;
import com.subaiqiao.yupicture.interfaces.dto.user.VipExchangeRequest;
import com.subaiqiao.yupicture.interfaces.vo.user.LoginUserVO;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author 12947
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-14 10:14:15
*/
@Service
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService domainService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户信息
     * @return 新用户ID
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        User.validUserRegister(userAccount, userPassword, checkPassword);
        return domainService.userRegister(userAccount, userPassword, checkPassword);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验
        User.validUserLogin(userAccount, userPassword);
        return domainService.userLogin(userAccount, userPassword, request);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return domainService.getLoginUser(request);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        return domainService.userLogout(request);
    }

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return domainService.getLoginUserVO(user);

    }

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        return domainService.getUserVO(user);
    }

    /**
     * 获取脱敏后的用户信息列表
     * @param list 脱敏前用户信息列表
     * @return 脱敏后用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> list) {
        return domainService.getUserVOList(list);
    }

    /**
     * 获取加密密码
     * @param userPassword 原密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return domainService.getEncryptPassword(userPassword);
    }

    /**
     * 获取查询条件
     * @param userQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return domainService.getQueryWrapper(userQueryRequest);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return domainService.listByIds(userIdSet);
    }

    @Override
    public User getUserById(Long userId) {
        return domainService.getUserById(userId);
    }

    @Override
    public long addUser(User user) {
        return domainService.addUser(user);
    }

    @Override
    public boolean removeById(long id) {
        return domainService.removeById(id);
    }

    @Override
    public boolean updateById(User user) {
        return domainService.updateById(user);
    }

    @Override
    public Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest) {
        return domainService.listUserByPage(userQueryRequest);
    }

    @Override
    public boolean exchangeVip(VipExchangeRequest vipExchangeRequest, HttpServletRequest httpServletRequest) {
        String vipCode = vipExchangeRequest.getVipCode();
        User loginUser = getLoginUser(httpServletRequest);
        return domainService.exchangeVip(loginUser, vipCode);
    }

}




