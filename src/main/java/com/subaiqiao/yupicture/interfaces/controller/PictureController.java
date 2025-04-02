package com.subaiqiao.yupicture.interfaces.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.subaiqiao.yupicture.application.service.PictureApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.picture.entity.Picture;
import com.subaiqiao.yupicture.domain.picture.valueobject.PictureReviewStatusEnum;
import com.subaiqiao.yupicture.domain.user.constant.UserConstant;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.AliYunAiApi;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.subaiqiao.yupicture.infrastructure.api.imagesearch.ImageSearchApiFacade;
import com.subaiqiao.yupicture.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.subaiqiao.yupicture.infrastructure.common.BaseResponse;
import com.subaiqiao.yupicture.infrastructure.common.DeleteRequest;
import com.subaiqiao.yupicture.infrastructure.common.ResultUtils;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.interfaces.assembler.PictureAssembler;
import com.subaiqiao.yupicture.interfaces.dto.picture.*;
import com.subaiqiao.yupicture.interfaces.vo.picture.PictureTagCategory;
import com.subaiqiao.yupicture.interfaces.vo.picture.PictureVO;
import com.subaiqiao.yupicture.shared.auth.SpaceUserAuthManager;
import com.subaiqiao.yupicture.shared.auth.StpKit;
import com.subaiqiao.yupicture.shared.auth.annotation.SaSpaceCheckPermission;
import com.subaiqiao.yupicture.shared.auth.model.SpaceUserPermissionConstant;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.application.service.SpaceApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L) // 最大1w条
            .expireAfterWrite(Duration.ofMinutes(5)) // 过期时间 5分钟后过期
            .build();

    /**
     * 图片上传
     * @param multipartFile 文件信息
     * @param pictureUploadRequest 业务信息
     * @param request 请求头
     * @return 文件上传结果
     */
    @PostMapping("/uploadPicture")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        return ResultUtils.success(pictureApplicationService.uploadPicture(multipartFile, pictureUploadRequest, userApplicationService.getLoginUser(request)));
    }

    /**
     * URL图片上传
     * @param pictureUploadRequest 业务信息
     * @param request 请求头
     * @return 文件上传结果
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        return ResultUtils.success(pictureApplicationService.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, userApplicationService.getLoginUser(request)));
    }


    /**
     * 删除图片
     * @param deleteRequest 删除信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<String> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        pictureApplicationService.deletePicture(deleteRequest.getId(), userApplicationService.getLoginUser(request));
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
        Picture oldPicture = pictureApplicationService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND);
        Picture picture = PictureAssembler.toPictureEntity(pictureUpdateRequest);
        // 补充审核参数
        pictureApplicationService.fillReviewParams(picture, userApplicationService.getLoginUser(request));
        pictureApplicationService.validPicture(picture);
        boolean result = pictureApplicationService.updateById(picture);
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
        Picture picture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        return ResultUtils.success(picture);
    }

    /**
     * 根据ID获取照片
     * @param id ID
     * @return 脱敏的照片信息
     */

    @PostMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        // 改为使用注解鉴权
//        pictureApplicationService.checkPictureAuth(userApplicationService.getLoginUser(request), picture);
        Long spaceId = picture.getSpaceId();
        Space space = null;
        User loginUser = null;
        if (ObjUtil.isNotNull(spaceId)) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.ACCESS_DENIED);
            space = spaceApplicationService.getSpaceById(spaceId);
            loginUser = userApplicationService.getLoginUser(request);
        }
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = pictureApplicationService.getPictureVO(picture);
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);
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
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }


    /**
     * 更新用户
     * @param pictureEditRequest 更新用户信息
     * @return 更新成功
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<String> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureEditRequest) || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        pictureApplicationService.editPicture(pictureEditRequest, userApplicationService.getLoginUser(request));
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
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (ObjUtil.isNull(spaceId)) {
            // 图片用户只允许看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            // 公开图片
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
//            User loginUser = userApplicationService.getLoginUser(request);
            Space space = spaceApplicationService.getSpaceById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND, "空间不存在");
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.ACCESS_DENIED);
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.ACCESS_DENIED);
//            }
        }
        // 分页查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        return ResultUtils.success(pictureApplicationService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取用户列表（有多级缓存）
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page/vo/cache/multi")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageCacheMulti(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫每次最大获取20条
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (ObjUtil.isNull(spaceId)) {
            // 图片用户只允许看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            // 公开图片
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userApplicationService.getLoginUser(request);
            Space space = spaceApplicationService.getSpaceById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
        // 查询缓存，缓存中没有在去查询数据库
        // 构建缓存的key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("yupicture:listPictureVOByPage:%s", hashKey);
        // 查询本地缓存
        String cacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cacheValue, Page.class, true);
            return ResultUtils.success(pictureVOPage);
        }
        // 查询分布式缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cacheValue = opsForValue.get(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cacheValue, Page.class, true);
            // 更新本地缓存
            LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(pictureVOPage));
            return ResultUtils.success(pictureVOPage);
        }
        // 分页查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
        // 写入本地缓存和Redis缓存中，5分钟本地缓存有效期，5-10分钟分布式缓存有效期，防止Redis缓存雪崩
        // 不管数据有没有值，全部都放入缓存，防止缓存穿透
//        if (pictureVOPage.getTotal() > 0) {
        LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(pictureVOPage));
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, JSONUtil.toJsonStr(pictureVOPage), cacheExpireTime, TimeUnit.SECONDS);
//        }
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取用户列表（有Redis缓存）
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page/vo/cache/redis")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageCacheRedis(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
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
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
        // 写入缓存中，5-10分钟有效期，防止缓存雪崩
        if (pictureVOPage.getTotal() > 0) {
            int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
            opsForValue.set(redisKey, JSONUtil.toJsonStr(pictureVOPage), cacheExpireTime, TimeUnit.SECONDS);
        }
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取用户列表（有Caffeine缓存）
     * @param pictureQueryRequest 查询条件
     * @param request 请求头信息
     * @return 用户列表
     */
    @PostMapping("/list/page/vo/cache/caffeine")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageCacheCaffeine(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
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
        String cacheKey = String.format("yupicture:listPictureVOByPage:%s", hashKey);
        String cacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cacheValue, Page.class, true);
            return ResultUtils.success(pictureVOPage);
        }
        // 分页查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        // 封装VO
        Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
        // 写入缓存中，5分钟有效期
        if (pictureVOPage.getTotal() > 0) {
            LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(pictureVOPage));
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
        pictureApplicationService.doPictureReview(pictureReviewRequest, userApplicationService.getLoginUser(request));
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
        return ResultUtils.success(pictureApplicationService.uploadPictureByBatch(pictureUploadByBatchRequest, userApplicationService.getLoginUser(request)));
    }

    /**
     * 以图搜图
     * @param searchPictureByPictureRequest 搜索条件
     * @param request 请求头信息
     * @return 以图搜图所查询到的图片信息
     */
    @PostMapping("/search/picture")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(searchPictureByPictureRequest) || searchPictureByPictureRequest.getPictureId() <= 0, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        Picture picture = pictureApplicationService.getById(pictureId);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        return ResultUtils.success(ImageSearchApiFacade.searchImage(picture.getUrl()));
    }

    /**
     * 根据颜色搜索图片
     * @param searchPictureByColorRequest 搜索条件
     * @param request 请求头信息
     * @return 颜色搜索所查询到的图片信息
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(searchPictureByColorRequest) || StrUtil.isBlank(searchPictureByColorRequest.getPicColor()), ErrorCode.PARAMS_ERROR);
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        return ResultUtils.success(pictureApplicationService.searchPictureByColor(spaceId, picColor, userApplicationService.getLoginUser(request)));
    }

    /**
     * 批量修改图片信息
     * @param pictureEditByBatchRequest 修改图片信息
     * @param request 请求头信息
     * @return 修改完成
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<String> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(pictureEditByBatchRequest), ErrorCode.PARAMS_ERROR);
        pictureApplicationService.editPictureByBatch(pictureEditByBatchRequest, userApplicationService.getLoginUser(request));
        return ResultUtils.success("ok");
    }

    /**
     * 创建AI扩图任务
     * @param createPictureOutPaintingTaskRequest 扩图任务信息
     * @param request 请求头信息
     * @return 创建任务信息
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(createPictureOutPaintingTaskRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureApplicationService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, userApplicationService.getLoginUser(request)));
    }

    /**
     * 查询AI扩图任务
     * @param taskId 任务ID
     * @param request 请求头信息
     * @return 任务详情
     */
    @PostMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(@RequestParam String taskId, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(aliYunAiApi.getOutPaintingTask(taskId));
    }
}
