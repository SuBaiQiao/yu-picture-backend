package com.subaiqiao.yupicture.interfaces.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.common.BaseResponse;
import com.subaiqiao.yupicture.infrastructure.common.DeleteRequest;
import com.subaiqiao.yupicture.infrastructure.common.ResultUtils;
import com.subaiqiao.yupicture.domain.user.constant.UserConstant;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.dto.space.space.*;
import com.subaiqiao.yupicture.shared.auth.SpaceUserAuthManager;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceLevelEnum;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.application.service.SpaceApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 创建空间
     * @param spaceAddRequest 空间创建对象
     * @param request 请求头
     * @return 空间ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceAddRequest), ErrorCode.PARAMS_ERROR);
        User user = userApplicationService.getLoginUser(request);
        return ResultUtils.success(spaceApplicationService.addSpace(spaceAddRequest, user));
    }
    
    /**
     * 删除空间
     * @param deleteRequest 删除信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<String> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        spaceApplicationService.deleteSpace(deleteRequest.getId(), userApplicationService.getLoginUser(request));
        return ResultUtils.success("ok");
    }

    /**
     * 更新空间
     * @param spaceUpdateRequest 更新空间信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUpdateRequest) || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space oldSpace = spaceApplicationService.getSpaceById(spaceUpdateRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldSpace), ErrorCode.NOT_FOUND);
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        // 补充审核参数
        spaceApplicationService.fillSpaceBySpaceLevel(space);
        space.validSpace(false);
        boolean result = spaceApplicationService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 根据ID获取照片（管理员使用）
     * @param id ID
     * @param request 请求头信息
     * @return 未脱敏的照片信息
     */

    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(@RequestParam long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceApplicationService.getSpaceById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        return ResultUtils.success(space);
    }

    /**
     * 根据ID获取照片
     * @param id ID
     * @return 脱敏的照片信息
     */

    @PostMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(@RequestParam long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceApplicationService.getSpaceById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userApplicationService.getLoginUser(request));
        SpaceVO spaceVO = spaceApplicationService.getSpaceVO(space, request);
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间列表
     * @param spaceQueryRequest 查询条件
     * @param request 请求头信息
     * @return 空间列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceQueryRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceApplicationService.listSpaceByPage(spaceQueryRequest));
    }


    /**
     * 更新空间
     * @param spaceEditRequest 更新空间信息
     * @return 更新成功
     */
    @PostMapping("/edit")
    public BaseResponse<String> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceEditRequest) || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = userApplicationService.getLoginUser(request);
        Space oldSpace = spaceApplicationService.getSpaceById(spaceEditRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldSpace), ErrorCode.NOT_FOUND);
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        // 补充审核参数
        spaceApplicationService.fillSpaceBySpaceLevel(space);
        space.validSpace(false);
        spaceApplicationService.checkSpaceAuth(user, oldSpace);
        boolean result = spaceApplicationService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 分页获取空间列表
     * @param spaceQueryRequest 查询条件
     * @param request 请求头信息
     * @return 空间列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceQueryRequest), ErrorCode.PARAMS_ERROR);
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫每次最大获取20条
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 分页查询数据库
        Page<Space> spacePage = spaceApplicationService.listSpaceByPage(spaceQueryRequest);
        // 封装VO
        return ResultUtils.success(spaceApplicationService.getSpaceVOPage(spacePage, request));
    }

    /**
     * 获取空间等级信息
     * @return 空间等级信息集合
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        return ResultUtils.success(
                Arrays.stream(SpaceLevelEnum.values())
                        .map(spaceLevelEnum ->
                                new SpaceLevel(
                                        spaceLevelEnum.getValue(),
                                        spaceLevelEnum.getText(),
                                        spaceLevelEnum.getMaxCount(),
                                        spaceLevelEnum.getMaxSize())
                        ).collect(Collectors.toList()));
    }
}
