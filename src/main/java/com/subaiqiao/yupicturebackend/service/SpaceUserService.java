package com.subaiqiao.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.subaiqiao.yupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.subaiqiao.yupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.subaiqiao.yupicturebackend.model.entity.SpaceUser;
import com.subaiqiao.yupicturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Caozhaoyu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-22 09:51:30
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 创建空间
     * @param spaceUserAddRequest 创建空间信息
     * @return 创建好的空间id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);
    /**
     * 校验
     * @param spaceUser 信息
     * @param add 是否为新增
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取托名后的对象
     * @param spaceUser 原对象
     * @return 脱敏后的对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类（列表）
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
