package com.subaiqiao.yupicture.domain.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.application.service.SpaceUserApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.space.entity.SpaceUser;
import com.subaiqiao.yupicture.domain.space.repository.SpaceRepository;
import com.subaiqiao.yupicture.domain.space.repository.SpaceUserRepository;
import com.subaiqiao.yupicture.domain.space.service.SpaceDomainService;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceLevelEnum;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceRoleEnum;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceTypeEnum;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceQueryRequest;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import com.subaiqiao.yupicture.shared.sharding.DynamicShardingManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 12947
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-02-16 17:38:31
*/
@Service
public class SpaceDomainServiceImpl implements SpaceDomainService {

    @Resource
    private SpaceRepository repository;

    @Resource
    private SpaceUserRepository spaceUserRepository;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

//    @Resource
//    @Lazy
    private DynamicShardingManager dynamicShardingManager;

    /**
     * Spring 所提供的编程式事务
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(Space space, Integer spaceType, Long userId) {
        // 控制同一个用户只有一个私有空间 加锁，以及一个团队空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            // 编程式事务
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = repository.lambdaQuery().eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每位用户每类空间只有拥有一个私人空间");
                // 写入数据库
                boolean result = repository.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间失败");
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceType) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    boolean result_space_user = spaceUserRepository.save(spaceUser);
                    ThrowUtils.throwIf(!result_space_user, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                // 为了方便测试和部署，暂时不启用
//                dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();
            });
            ThrowUtils.throwIf(ObjUtil.isNull(newSpaceId), ErrorCode.OPERATION_ERROR, "保存空间失败");
            return newSpaceId;
        }
    }

    /**
     * 分页获取脱敏信息
     * @param spacePage 原数据
     * @param request 请求头
     * @return 脱敏后的图片分页信息
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userApplicationService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 获取托名后的对象
     * @param space 原对象
     * @return 脱敏后的对象
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0 && spaceVO != null) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            if (userVO != null) {
                spaceVO.setUser(userVO);
            }
        }
        return spaceVO;
    }

    /**
     * 获取查询条件
     * @param spaceQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (ObjUtil.isNull(spaceQueryRequest)) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);

        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    /**
     * 根据空间等级设置容量
     * @param space 空间
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (ObjUtil.isNotNull(enumByValue)) {
            long maxSize = enumByValue.getMaxSize();
            if (ObjUtil.isNull(space.getMaxSize()) || space.getMaxSize() == 0) {
                space.setMaxSize(maxSize);
            }
            long maxCount = enumByValue.getMaxCount();
            if (ObjUtil.isNull(space.getMaxCount()) || space.getMaxCount() == 0) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (!space.getUserId().equals(loginUser.getId()) && !loginUser.isAdmin()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    public Space getSpaceById(Long spaceId) {
        return repository.getById(spaceId);
    }

    @Override
    public List<Space> listByIds(Set<Long> spaceIdSet) {
        return repository.listByIds(spaceIdSet);
    }

    @Override
    public LambdaUpdateChainWrapper<Space> lambdaUpdate() {
        return repository.lambdaUpdate();
    }

    @Override
    public boolean removeById(Long spaceId) {
        return repository.removeById(spaceId);
    }

    @Override
    public boolean updateById(Space space) {
        return repository.updateById(space);
    }

    @Override
    public Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        return repository.page(new Page<>(current, size), getQueryWrapper(spaceQueryRequest));
    }

    @Override
    public List<Space> list(QueryWrapper<Space> queryWrapper) {
        return repository.list(queryWrapper);
    }

    @Override
    public LambdaQueryChainWrapper<Space> lambdaQuery() {
        return repository.lambdaQuery();
    }
}




