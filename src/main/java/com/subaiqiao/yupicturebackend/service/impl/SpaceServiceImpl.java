package com.subaiqiao.yupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.exception.ThrowUtils;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.enums.SpaceLevelEnum;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;
import com.subaiqiao.yupicturebackend.model.vo.UserVO;
import com.subaiqiao.yupicturebackend.service.SpaceService;
import com.subaiqiao.yupicturebackend.mapper.SpaceMapper;
import com.subaiqiao.yupicturebackend.service.UserService;
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
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    /**
     * Spring 所提供的编程式事务
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User user) {
        // 填充参数默认值
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (ObjUtil.isNull(space.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        // 校验参数
        this.validSpace(space, true);
        // 校验权限，非管理员只能创建普通空间
        Long userId = user.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        // 控制同一个用户只有一个私有空间 加锁
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            // 编程式事务
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每位用户只有拥有一个私人空间");
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间失败");
                return space.getId();
            });
            ThrowUtils.throwIf(ObjUtil.isNull(newSpaceId), ErrorCode.OPERATION_ERROR, "保存空间失败");
            return newSpaceId;
        }
    }

    /**
     * 校验
     * @param space 信息
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(space), ErrorCode.PARAMS_ERROR);

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);

        if (add) {
            ThrowUtils.throwIf(ObjUtil.isNull(spaceName), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtils.throwIf(spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称 过长");
        }
        ThrowUtils.throwIf(ObjUtil.isNotNull(spaceLevel) && ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "空间级别不存在");
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
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
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
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0 && spaceVO != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
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
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);

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
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}




