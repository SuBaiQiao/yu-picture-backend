package com.subaiqiao.yupicture.domain.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicture.application.service.SpaceApplicationService;
import com.subaiqiao.yupicture.application.service.SpaceUserApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.space.entity.SpaceUser;
import com.subaiqiao.yupicture.domain.space.repository.SpaceUserRepository;
import com.subaiqiao.yupicture.domain.space.service.SpaceUserDomainService;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceRoleEnum;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.infrastructure.mapper.SpaceUserMapper;
import com.subaiqiao.yupicture.interfaces.dto.space.spaceuser.SpaceUserAddRequest;
import com.subaiqiao.yupicture.interfaces.dto.space.spaceuser.SpaceUserQueryRequest;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceUserVO;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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




