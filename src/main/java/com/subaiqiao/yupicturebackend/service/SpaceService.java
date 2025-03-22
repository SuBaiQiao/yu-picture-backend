package com.subaiqiao.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 12947
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-16 17:38:31
*/
public interface SpaceService extends IService<Space> {
    /**
     * 创建空间
     * @param spaceAddRequest 创建空间信息
     * @param user 登录用户
     * @return 创建好的空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User user);
    /**
     * 校验
     * @param space 信息
     * @param add 是否为新增
     */
    void validSpace(Space space, boolean add);

    /**
     * 分页获取脱敏信息
     * @param spacePage 原数据
     * @param request 请求头
     * @return 脱敏后的图片分页信息
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取托名后的对象
     * @param space 原对象
     * @return 脱敏后的对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取查询条件
     * @param spaceQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间等级设置容量
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     * @param loginUser 登录用户
     * @param space 空间
     */
    void checkSpaceAuth(User loginUser, Space space);
}
