package com.subaiqiao.yupicture.domain.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.domain.user.repository.UserRepository;
import com.subaiqiao.yupicture.domain.user.service.UserDomainService;
import com.subaiqiao.yupicture.domain.user.constant.UserConstant;
import com.subaiqiao.yupicture.domain.user.valueobject.UserRoleEnum;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.user.UserQueryRequest;
import com.subaiqiao.yupicture.interfaces.vo.user.LoginUserVO;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import com.subaiqiao.yupicture.shared.auth.StpKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 12947
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-14 10:14:15
*/
@Service
@Slf4j
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository repository;

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 检查账户是否在数据库中已经重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = repository.getBaseMapper().selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名大侠");
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 插入数据到数据库中
        boolean save = repository.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败");
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询数据库中用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount).eq("userPassword", encryptPassword);
        User user = repository.getBaseMapper().selectOne(queryWrapper);
        // 不存在，返回错误信息
        if (ObjUtil.isEmpty(user)) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 保存用户登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 记录用户登录状态到Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的用户信息保持一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (ObjUtil.isEmpty(currentUser) || ObjUtil.isEmpty(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询后返回用户信息
        Long userId = currentUser.getId();
        currentUser = repository.getById(userId);
        if (ObjUtil.isEmpty(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否登录
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (ObjUtil.isEmpty(attribute)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (ObjUtil.isEmpty(user)) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;

    }

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前用户信息
     * @return 脱敏后用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (ObjUtil.isEmpty(user)) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     * @param list 脱敏前用户信息列表
     * @return 脱敏后用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取加密密码
     * @param userPassword 原密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex(("subaiqiao" + userPassword).getBytes());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (ObjUtil.isNull(userQueryRequest)) {
            return queryWrapper;
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(ObjUtil.isNotEmpty(userName), "userName", userName);
        queryWrapper.like(ObjUtil.isNotEmpty(userAccount), "userAccount", userAccount);
        queryWrapper.like(ObjUtil.isNotEmpty(userProfile), "userProfile", userProfile);
        queryWrapper.eq(ObjUtil.isNotEmpty(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return repository.listByIds(userIdSet);
    }

    @Override
    public User getUserById(Long userId) {
        return repository.getById(userId);
    }

    @Override
    public long addUser(User user) {
        // 默认密码
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(getEncryptPassword(DEFAULT_PASSWORD));
        boolean result = repository.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    @Override
    public boolean removeById(long id) {
        return repository.removeById(id);
    }

    @Override
    public boolean updateById(User user) {
        return repository.updateById(user);
    }

    @Override
    public Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(userQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = repository.page(new Page<>(current, size), getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVOList = getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

}




