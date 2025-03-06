package com.subaiqiao.yupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.exception.ThrowUtils;
import com.subaiqiao.yupicturebackend.mapper.SpaceMapper;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.analyze.*;
import com.subaiqiao.yupicturebackend.model.entity.Picture;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.enums.SpaceLevelEnum;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;
import com.subaiqiao.yupicturebackend.model.vo.UserVO;
import com.subaiqiao.yupicturebackend.model.vo.space.analyze.*;
import com.subaiqiao.yupicturebackend.service.PictureService;
import com.subaiqiao.yupicturebackend.service.SpaceAnalyzeService;
import com.subaiqiao.yupicturebackend.service.SpaceService;
import com.subaiqiao.yupicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
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
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    /**
     * 校验空间分析权限
     * @param spaceAnalyzeRequest 请求参数
     * @param loginUser 登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        // 只有管理员可以分析全部图片或公共图库图片
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.ACCESS_DENIED);
        } else {
            // 分析某一个空间
            ThrowUtils.throwIf(ObjUtil.isNull(spaceId), ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
            spaceService.checkSpaceAuth(loginUser, space);
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
        }
    }


    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            return;
        }
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        if (ObjUtil.isNotEmpty(spaceId)) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        // 校验参数
        // 全空间或公共图库，需要从Picture表中查询
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // 校验权限,仅管理员可访问
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(obj -> Long.parseLong(obj.toString())).sum();
            long usedCount = pictureObjList.size();
            // 封装返回结果
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(usedSize);
            response.setUsedCount(usedCount);
            return response;
        } else {
            // 特定空间可以直接从Space表查询
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(ObjUtil.isNull(spaceId) || spaceId <= 0L, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
            // 权限校验
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            // 封装返回结果
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setUsedCount(space.getTotalCount());
            response.setMaxCount(space.getMaxCount());
            response.setMaxSize(space.getMaxSize());
            response.setSizeUsageRatio(NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue());
            response.setCountUsageRatio(NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue());
            return response;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceCategoryAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // 分组查询
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = (String) result.get("category");
            Long count = (Long) result.get("count");
            Long totalSize = (Long) result.get("totalSize");
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User user) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceTagAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, user);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        // 解析标签并且统计
        Map<String, Long> tagCountMap = tagsJsonList.stream().flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 转换为响应对象，按照使用次数进行排序
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .collect(Collectors.toList());
        // 定义分段范围，注意需要使用有序的Map
        Map<String, Long> sizeRangeMap = new LinkedHashMap<>();
        sizeRangeMap.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRangeMap.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRangeMap.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRangeMap.put(">1MB", picSizeList.stream().filter(size -> size >= 1024 * 1024).count());
        return sizeRangeMap.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        if (ObjUtil.isNotEmpty(userId)) {
            queryWrapper.eq("userId", userId);
        }
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("date_format(createTime, '%Y-%m-%d') as period, count(*) as count");
                break;
            case "week":
                queryWrapper.select("yearweek(createTime) as period, count(*) as count");
                break;
            case "month":
                queryWrapper.select("date_format(createTime, '%Y-%m') as period, count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度错误");
        }
        // 分组排序
        queryWrapper.groupBy("period").orderByAsc("period");
        // 查询并封装结果
        List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return queryResult.stream().map(result -> {
            String period = (String) result.get("period");
            Long count = (Long) result.get("count");
            return new SpaceUserAnalyzeResponse(period, count);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceVO> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceRankAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.ACCESS_DENIED);

        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());
        return spaceService.list(queryWrapper).stream().map(SpaceVO::objToVo).collect(Collectors.toList());
    }
}




