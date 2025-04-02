package com.subaiqiao.yupicture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.subaiqiao.yupicture.domain.space.entity.SpaceUser;
import com.subaiqiao.yupicture.domain.space.repository.SpaceUserRepository;
import com.subaiqiao.yupicture.domain.space.service.SpaceUserDomainService;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.space.spaceuser.SpaceUserQueryRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author Caozhaoyu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-03-22 09:51:30
*/
@Service
public class SpaceUserDomainServiceImpl implements SpaceUserDomainService {

    @Resource
    private SpaceUserRepository repository;

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    @Override
    public long addSpaceUser(SpaceUser spaceUser) {
        // 数据库操作
        boolean result = repository.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public SpaceUser getSpaceUserById(long id) {
        return repository.getById(id);
    }

    @Override
    public boolean updateById(SpaceUser spaceUser) {
        return repository.updateById(spaceUser);
    }

    @Override
    public SpaceUser getOne(QueryWrapper<SpaceUser> queryWrapper) {
        return repository.getOne(queryWrapper);
    }

    @Override
    public List<SpaceUser> list(QueryWrapper<SpaceUser> queryWrapper) {
        return repository.list(queryWrapper);
    }

    @Override
    public boolean removeById(long id) {
        return repository.removeById(id);
    }

    @Override
    public LambdaQueryChainWrapper<SpaceUser> lambdaQuery() {
        return repository.lambdaQuery();
    }

}




