package com.subaiqiao.yupicturebackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.subaiqiao.yupicturebackend.annotation.AuthCheck;
import com.subaiqiao.yupicturebackend.common.BaseResponse;
import com.subaiqiao.yupicturebackend.common.ResultUtils;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.exception.ThrowUtils;
import com.subaiqiao.yupicturebackend.model.dto.space.analyze.*;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;
import com.subaiqiao.yupicturebackend.model.vo.space.analyze.*;
import com.subaiqiao.yupicturebackend.service.SpaceAnalyzeService;
import com.subaiqiao.yupicturebackend.service.SpaceService;
import com.subaiqiao.yupicturebackend.service.UserService;
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
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 获取空间使用情况分析
     * @param spaceUsageAnalyzeRequest 查询条件
     * @param request 请求头信息
     * @return 统计结果
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUsageAnalyzeRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
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
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser));
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
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser));
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
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser));
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
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser));
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
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser));
    }
}
