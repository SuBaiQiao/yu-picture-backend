package com.subaiqiao.yupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicturebackend.annotation.AuthCheck;
import com.subaiqiao.yupicturebackend.common.BaseResponse;
import com.subaiqiao.yupicturebackend.common.DeleteRequest;
import com.subaiqiao.yupicturebackend.common.ResultUtils;
import com.subaiqiao.yupicturebackend.constant.UserConstant;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.exception.ThrowUtils;
import com.subaiqiao.yupicturebackend.manager.auth.SpaceUserAuthManager;
import com.subaiqiao.yupicturebackend.model.dto.space.*;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.enums.SpaceLevelEnum;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;
import com.subaiqiao.yupicturebackend.service.SpaceService;
import com.subaiqiao.yupicturebackend.service.UserService;
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
    private SpaceService spaceService;

    @Resource
    private UserService userService;

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
        User user = userService.getLoginUser(request);
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, user));
    }
    
    /**
     * 删除空间
     * @param deleteRequest 删除信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<String> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        Space space = spaceService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        spaceService.checkSpaceAuth(user, space);
        boolean result = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
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
        Space oldSpace = spaceService.getById(spaceUpdateRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldSpace), ErrorCode.NOT_FOUND);
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        // 补充审核参数
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);
        boolean result = spaceService.updateById(space);
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
        Space space = spaceService.getById(id);
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
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userService.getLoginUser(request));
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
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
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }


    /**
     * 更新空间
     * @param spaceEditRequest 更新空间信息
     * @return 更新成功
     */
    @PostMapping("/edit")
    public BaseResponse<String> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceEditRequest) || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        Space oldSpace = spaceService.getById(spaceEditRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldSpace), ErrorCode.NOT_FOUND);
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        // 补充审核参数
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);
        spaceService.checkSpaceAuth(user, oldSpace);
        boolean result = spaceService.updateById(space);
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
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫每次最大获取20条
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 分页查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest));
        // 封装VO
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
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
