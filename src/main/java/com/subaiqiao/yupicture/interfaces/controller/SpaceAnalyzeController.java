package com.subaiqiao.yupicture.interfaces.controller;

import cn.hutool.core.util.ObjUtil;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.common.BaseResponse;
import com.subaiqiao.yupicture.infrastructure.common.ResultUtils;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.space.space.analyze.*;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.interfaces.vo.space.space.analyze.*;
import com.subaiqiao.yupicture.application.service.SpaceAnalyzeApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/space/analyze")
@Slf4j
public class SpaceAnalyzeController {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceAnalyzeApplicationService spaceAnalyzeApplicationService;

    /**
     * 获取空间使用情况分析
     * @param spaceUsageAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUsageAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeApplicationService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }

    /**
     * 获取空间图片分类分析
     * @param spaceCategoryAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceCategoryAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeApplicationService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间图片标签分析
     * @param spaceTagAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceTagAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeApplicationService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间图片大小分析
     * @param spaceSizeAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeApplicationService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间用户图片上传分析
     * @param spaceUserAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeApplicationService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间使用排行分析
     * @param spaceRankAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/rank")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<SpaceVO>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceRankAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeApplicationService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser));
    }
}
