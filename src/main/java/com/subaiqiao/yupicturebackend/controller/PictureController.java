package com.subaiqiao.yupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.subaiqiao.yupicturebackend.annotation.AuthCheck;
import com.subaiqiao.yupicturebackend.common.BaseResponse;
import com.subaiqiao.yupicturebackend.common.DeleteRequest;
import com.subaiqiao.yupicturebackend.common.ResultUtils;
import com.subaiqiao.yupicturebackend.constant.UserConstant;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.exception.ThrowUtils;
import com.subaiqiao.yupicturebackend.manager.CosManager;
import com.subaiqiao.yupicturebackend.model.dto.picture.*;
import com.subaiqiao.yupicturebackend.model.dto.user.UserQueryRequest;
import com.subaiqiao.yupicturebackend.model.dto.user.UserUpdateRequest;
import com.subaiqiao.yupicturebackend.model.entity.Picture;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.subaiqiao.yupicturebackend.model.vo.PictureTagCategory;
import com.subaiqiao.yupicturebackend.model.vo.PictureVO;
import com.subaiqiao.yupicturebackend.model.vo.UserVO;
import com.subaiqiao.yupicturebackend.service.PictureService;
import com.subaiqiao.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 图片上传
     * @param multipartFile 文件信息
     * @param pictureUploadRequest 业务信息
     * @param request 请求头
     * @return 文件上传结果
     */
    @PostMapping("/uploadPicture")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        return ResultUtils.success(pictureService.uploadPicture(multipartFile, pictureUploadRequest, userService.getLoginUser(request)));
    }

    /**
     * URL图片上传
     * @param pictureUploadRequest 业务信息
     * @param request 请求头
     * @return 文件上传结果
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        return ResultUtils.success(pictureService.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, userService.getLoginUser(request)));
    }


    /**
     * 删除图片
     * @param deleteRequest 删除信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<String> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        Picture picture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        if (!picture.getUserId().equals(user.getId()) && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        boolean result = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 更新用户
     * @param pictureUpdateRequest 更新用户信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureUpdateRequest) || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 补充审核参数
        pictureService.fillReviewParams(picture, userService.getLoginUser(request));
        pictureService.validPicture(picture);
        boolean result = pictureService.updateById(picture);
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
    public BaseResponse<Picture> getPictureById(@RequestParam long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        return ResultUtils.success(picture);
    }

    /**
     * 根据ID获取照片
     * @param id ID
     * @return 脱敏的照片信息
     */

    @PostMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页获取用户列表
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }


    /**
     * 更新用户
     * @param pictureEditRequest 更新用户信息
     * @return 更新成功
     */
    @PostMapping("/edit")
    public BaseResponse<String> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureEditRequest) || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        // 补充审核参数
        pictureService.fillReviewParams(picture, user);
        pictureService.validPicture(picture);
        if (!oldPicture.getUserId().equals(user.getId()) && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success("ok");
    }

    /**
     * 分页获取用户列表
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫每次最大获取20条
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 图片用户只允许看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 分页查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取用户列表（有缓存）
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫每次最大获取20条
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 图片用户只允许看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询缓存，缓存中没有在去查询数据库
        // 构建缓存的key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = String.format("yupicture:listPictureVOByPage:%s", hashKey);
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String cacheValue = opsForValue.get(redisKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cacheValue, Page.class, true);
            return ResultUtils.success(pictureVOPage);
        }
        // 分页查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 写入缓存中，5-10分钟有效期，防止缓存雪崩
        if (pictureVOPage.getTotal() > 0) {
            int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
            opsForValue.set(redisKey, JSONUtil.toJsonStr(pictureVOPage), cacheExpireTime, TimeUnit.SECONDS);
        }
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 获取标签和分类信息
     * @return 标签和分类信息
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = List.of("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = List.of("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     * @param pictureReviewRequest 审核信息
     * @param request 请求头信息
     * @return 审核完成
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> doReviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(pictureReviewRequest), ErrorCode.PARAMS_ERROR);
        pictureService.doPictureReview(pictureReviewRequest, userService.getLoginUser(request));
        return ResultUtils.success("ok");
    }

    /**
     * 批量爬取图片
     * @param pictureUploadByBatchRequest 批量配置信息
     * @param request 请求体
     * @return 插入数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(pictureUploadByBatchRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, userService.getLoginUser(request)));
    }

}
