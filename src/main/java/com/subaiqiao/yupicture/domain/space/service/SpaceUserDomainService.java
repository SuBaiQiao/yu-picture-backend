package com.subaiqiao.yupicture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicture.domain.space.entity.SpaceUser;
import com.subaiqiao.yupicture.interfaces.dto.space.spaceuser.SpaceUserAddRequest;
import com.subaiqiao.yupicture.interfaces.dto.space.spaceuser.SpaceUserQueryRequest;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceUserVO;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Caozhaoyu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-22 09:51:30
*/
public interface SpaceUserDomainService {

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    long addSpaceUser(SpaceUser spaceUser);

    SpaceUser getSpaceUserById(long id);

    boolean updateById(SpaceUser spaceUser);

    SpaceUser getOne(QueryWrapper<SpaceUser> queryWrapper);

    List<SpaceUser> list(QueryWrapper<SpaceUser> queryWrapper);

    boolean removeById(long id);

    LambdaQueryChainWrapper<SpaceUser> lambdaQuery();
}
