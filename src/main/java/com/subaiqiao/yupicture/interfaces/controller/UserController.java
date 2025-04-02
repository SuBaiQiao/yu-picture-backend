package com.subaiqiao.yupicture.interfaces.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.user.constant.UserConstant;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.common.BaseResponse;
import com.subaiqiao.yupicture.infrastructure.common.DeleteRequest;
import com.subaiqiao.yupicture.infrastructure.common.ResultUtils;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.assembler.UserAssembler;
import com.subaiqiao.yupicture.interfaces.dto.user.*;
import com.subaiqiao.yupicture.interfaces.vo.user.LoginUserVO;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 注册
     * @param userRegisterRequest 注册信息
     * @return 注册成功后的用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(userRegisterRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userApplicationService.userRegister(userRegisterRequest));
    }

    /**
     * 用户登录
     * @param userLoginRequest 登录信息
     * @param request 请求头信息
     * @return 登录存储的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(userLoginRequest), ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtils.success(userApplicationService.userLogin(userAccount, userPassword, request));
    }

    /**
     * 获取登录用户
     * @param request 请求头信息
     * @return 登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userApplicationService.getLoginUserVO(userApplicationService.getLoginUser(request)));
    }


    /**
     * 退出登录
     * @param request 请求头信息
     * @return 退出登录状态
     */
    @PostMapping("/get/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userApplicationService.userLogout(request));
    }

    /**
     * 添加用户
     * @param userAddRequest 用户信息
     * @return 注册成功后的用户ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(userAddRequest), ErrorCode.PARAMS_ERROR);
        User user = UserAssembler.toUserEntity(userAddRequest);
        return ResultUtils.success(userApplicationService.addUser(user));
    }

    /**
     * 根据id获取用户
     * @param id ID
     * @return 用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam long id){
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userApplicationService.getUserById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(user), ErrorCode.NOT_FOUND);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户VO
     * @param id ID
     * @return 用户VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam long id){
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userApplicationService.getUserById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(user), ErrorCode.NOT_FOUND);
        return ResultUtils.success(userApplicationService.getUserVO(user));
    }

    /**
     * 删除用户
     * @param deleteRequest 删除信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteUser(@RequestBody DeleteRequest deleteRequest){
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userApplicationService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 更新用户
     * @param userUpdateRequest 更新用户信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(ObjUtil.isNull(userUpdateRequest) || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = UserAssembler.toUserEntity(userUpdateRequest);
        boolean result = userApplicationService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 分页获取用户列表
     * @param userQueryRequest 查询条件
     * @return 用户列表
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest){
        return ResultUtils.success(userApplicationService.listUserByPage(userQueryRequest));
    }

}
