package com.subaiqiao.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subaiqiao.yupicturebackend.model.dto.user.UserQueryRequest;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicturebackend.model.vo.LoginUserVO;
import com.subaiqiao.yupicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 12947
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-14 10:14:15
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 密码
     * @param request 请求信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取登录用户
     * @param request 请求头信息
     * @return 登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     * @param request 请求头信息
     * @return 登录用户信息
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param list 脱敏前用户信息列表
     * @return 脱敏后用户信息列表
     */
    List<UserVO> getUserVOList(List<User> list);

    /**
     * 获取加密后的密码
     * @param userPassword 原密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取查询条件
     * @param userQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断用户是否为管理员
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}
