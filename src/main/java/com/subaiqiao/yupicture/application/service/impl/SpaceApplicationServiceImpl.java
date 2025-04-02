package com.subaiqiao.yupicture.application.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.application.service.SpaceApplicationService;
import com.subaiqiao.yupicture.application.service.SpaceUserApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.space.service.SpaceDomainService;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceLevelEnum;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceTypeEnum;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.assembler.SpaceAssembler;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceAddRequest;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceQueryRequest;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.shared.sharding.DynamicShardingManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author 12947
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-02-16 17:38:31
*/
@Service
public class SpaceApplicationServiceImpl implements SpaceApplicationService {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private SpaceDomainService domainService;

//    @Resource
//    @Lazy
    private DynamicShardingManager dynamicShardingManager;

    /**
     * Spring 所提供的编程式事务
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User user) {
        // 填充参数默认值
        Space space = SpaceAssembler.toSpaceEntity(spaceAddRequest);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (ObjUtil.isNull(space.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (ObjUtil.isNull(space.getSpaceType())) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        // 校验参数
        space.validSpace(true);
        // 校验权限，非管理员只能创建普通空间
        Long userId = user.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !user.isAdmin()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return domainService.addSpace(space, spaceAddRequest.getSpaceType(), userId);
    }


    /**
     * 分页获取脱敏信息
     * @param spacePage 原数据
     * @param request 请求头
     * @return 脱敏后的图片分页信息
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        return domainService.getSpaceVOPage(spacePage, request);
    }

    /**
     * 获取托名后的对象
     * @param space 原对象
     * @return 脱敏后的对象
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        return domainService.getSpaceVO(space, request);
    }

    /**
     * 获取查询条件
     * @param spaceQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        return domainService.getQueryWrapper(spaceQueryRequest);
    }

    /**
     * 根据空间等级设置容量
     * @param space 空间
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        domainService.fillSpaceBySpaceLevel(space);
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        domainService.checkSpaceAuth(loginUser, space);
    }

    @Override
    public Space getSpaceById(Long spaceId) {
        return domainService.getSpaceById(spaceId);
    }

    @Override
    public List<Space> listByIds(Set<Long> spaceIdSet) {
        return domainService.listByIds(spaceIdSet);
    }

    @Override
    public LambdaUpdateChainWrapper<Space> lambdaUpdate() {
        return domainService.lambdaUpdate();
    }

    @Override
    public void deleteSpace(Long spaceId, User user) {
        Space space = getSpaceById(spaceId);
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        checkSpaceAuth(user, space);
        boolean result = domainService.removeById(spaceId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public boolean updateById(Space space) {
        return domainService.updateById(space);
    }

    @Override
    public Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest) {
        return domainService.listSpaceByPage(spaceQueryRequest);
    }

    @Override
    public List<Space> list(QueryWrapper<Space> queryWrapper) {
        return domainService.list(queryWrapper);
    }

    @Override
    public LambdaQueryChainWrapper<Space> lambdaQuery() {
        return domainService.lambdaQuery();
    }

}




