package com.subaiqiao.yupicture.domain.picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicture.domain.picture.entity.Picture;
import com.subaiqiao.yupicture.domain.picture.repository.PictureRepository;
import com.subaiqiao.yupicture.domain.picture.service.PictureDomainService;
import com.subaiqiao.yupicture.domain.picture.valueobject.PictureReviewStatusEnum;
import com.subaiqiao.yupicture.infrastructure.api.CosManager;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.AliYunAiApi;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import com.subaiqiao.yupicture.infrastructure.utils.ColorSimilarUtils;
import com.subaiqiao.yupicture.interfaces.dto.picture.*;
import com.subaiqiao.yupicture.interfaces.vo.picture.PictureVO;
import com.subaiqiao.yupicture.infrastructure.manager.upload.FilePictureUpload;
import com.subaiqiao.yupicture.infrastructure.manager.upload.PictureUploadTemplate;
import com.subaiqiao.yupicture.infrastructure.manager.upload.UrlPictureUpload;
import com.subaiqiao.yupicture.infrastructure.manager.upload.dto.file.UploadPictureResult;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import com.subaiqiao.yupicture.application.service.SpaceApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 12947
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-15 13:48:27
*/
@Slf4j
@Service
public class PictureDomainServiceImpl implements PictureDomainService {

    @Resource
    private PictureRepository repository;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload uploadPicture;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture), ErrorCode.PARAMS_ERROR);

        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (ObjUtil.isNotNull(spaceId)) {
            Space space = spaceApplicationService.getSpaceById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND, "空间不存在");
            // 校验是否有空间权限，仅空间管理员可以上传
//            if (!space.getUserId().equals(loginUser.getId())) {
//                throw new BusinessException(ErrorCode.ACCESS_DENIED);
//            }
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间容量不足");
            }
        }
        // 判断新增还是修改
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新则判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = repository.getById(pictureId);
            ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND, "图片不存在");
//            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userApplicationService.isAdmin(loginUser)) {
//                throw new BusinessException(ErrorCode.ACCESS_DENIED);
//            }
            // 校验空间是否一致
            // 没传spaceId则复用原有图片的spaceId
            if (ObjUtil.isNull(spaceId)) {
                spaceId = oldPicture.getSpaceId();
            } else {
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间不一致");
                }
            }
        }
        // 上传图片
        // 按照用户ID划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (ObjUtil.isNull(spaceId)) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据inputSource的类型区分文件上传的方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = uploadPicture;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造入库的图片信息
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);
        // 指定空间ID
        picture.setSpaceId(spaceId);
        // 支持外层传递名称进行修改图片名称
        if (ObjUtil.isNotNull(pictureUploadRequest) && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setName(pictureUploadRequest.getPicName());
        }
        // 补充审核参数
        fillReviewParams(picture, loginUser);
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 修改图片信息
            boolean result = repository.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            // 更新空间的使用额度
            if (ObjUtil.isNotEmpty(finalSpaceId)) {
                boolean update = spaceApplicationService.lambdaUpdate().eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1").update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userApplicationService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0 && pictureVO != null) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            if (userVO != null) {
                pictureVO.setUser(userVO);
            }
        }
        return pictureVO;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (ObjUtil.isNull(pictureQueryRequest)) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)).or().like("introduction", searchText);
        }

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picWidth", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picWidth", picScale);

        // >= 开始时间
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < 结束时间
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(ObjUtil.isNull(pictureReviewRequest), ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (ObjUtil.isEmpty(id) || ObjUtil.isNull(reviewStatusEnum) || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断图片是否存在
        Picture oldPicture = repository.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND);
        // 判断图片是否已经审核过，如果审核过，则不允许再次审核
        if (reviewStatus.equals(oldPicture.getReviewStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片已经审核过，请勿重复操作");
        }
        // 数据库操作
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = repository.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 获取数据库操作对象
     * @param loginUser 登录用户
     * @param uploadPictureResult 上传图片结果
     * @param pictureId 图片ID
     * @return 图片数据库对象
     */
    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        // 设置文件名称
        picture.setName(uploadPictureResult.getPicName());
        // 设置文件大小
        picture.setPicSize(uploadPictureResult.getPicSize());
        // 设置宽度
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        // 设置高度
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        // 设置宽高比
        picture.setPicScale(uploadPictureResult.getPicScale());
        // 设置文件格式
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        // 设置缩略图地址
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 设置上传用户
        picture.setUserId(loginUser.getId());
        // 图片主色调
        picture.setPicColor(uploadPictureResult.getPicColor());
        // 操作数据库
        // 如果pictureId不为空，表示更新，否则就是新增
        picture.setId(pictureId);
        picture.setEditTime(new Date());
        return picture;
    }

    /**
     * 填充审核参数
     * @param picture 图片信息
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (loginUser.isAdmin()) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());

        }
    }

    @Override
    public int uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "每次爬取最多30条");
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = document.select("img.mimg");
        // 遍历元素，依次上传图片
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前连接为空，已跳过 {}", fileUrl);
                continue;
            }
            // 处理图片的地址，防止转义和对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                // 上传图片
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("{} 图片上传成功，id = {}", fileUrl, pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("{} 图片上传失败", fileUrl, e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 清理图片文件
     * @param picture 需要删除的图片
     */
    @Async
    @Override
    public void clearPicture(Picture picture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = picture.getUrl();
        long count = repository.lambdaQuery().eq(Picture::getUrl, pictureUrl)
                .count();
        if (count > 1) {
            return;
        }
        cosManager.deleteObject(pictureUrl);
        String thumbnailUrl = picture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl) && !thumbnailUrl.equals(pictureUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    /**
     * 校验空间图片的权限
     * @param loginUser 登录用户
     * @param picture 图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (ObjUtil.isNull(spaceId)) {
            // 公共图库，仅管理员和本人可以操作
            if (!picture.getUserId().equals(loginUserId) && !loginUser.isAdmin()) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            // 私有空间，仅管理员可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 登录用户
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.OPERATION_ERROR);
        Picture picture = repository.getById(pictureId);
        ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND);
        // 改为使用注解鉴权
//        this.checkPictureAuth(loginUser, picture);
        transactionTemplate.execute(status -> {
            // 修改图片信息
            boolean result = repository.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 更新空间的使用额度
            if (ObjUtil.isNotEmpty(picture.getSpaceId())) {
                boolean update = spaceApplicationService.lambdaUpdate().eq(Space::getId, picture.getSpaceId())
                        .setSql("totalSize = totalSize - " + picture.getPicSize())
                        .setSql("totalCount = totalCount - 1").update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        // 清理图片资源
        this.clearPicture(picture);
    }

    /**
     * 编辑图片
     * @param pictureEditRequest 编辑请求内容
     * @param loginUser 登录用户
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        Picture oldPicture = repository.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isNull(oldPicture), ErrorCode.NOT_FOUND);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        this.validPicture(picture);
        // 改为使用注解鉴权
//        this.checkPictureAuth(loginUser, oldPicture);
        boolean result = repository.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(ObjUtil.isNull(spaceId) || 0L == spaceId, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);
        // 校验权限
        Space space = spaceApplicationService.getSpaceById(spaceId);
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.ACCESS_DENIED);
        // 查询该空间下的所有图片（必须要有主色调）
        List<Picture> pictureList = repository.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片则直接返回
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        // 将颜色字符串转换为主色调
        Color targetColor = Color.decode(picColor);
        // 计算相似度后排序
        List<Picture> sortedPictureList = pictureList.stream().sorted(Comparator.comparingDouble(picture -> {
            String hexColor = picture.getPicColor();
            // 没有主色调的图片默认排序到最后
            if (StrUtil.isBlank(hexColor)) {
                return Double.MAX_VALUE;
            }
            Color color = Color.decode(hexColor);
            // 计算相似度
            return -ColorSimilarUtils.calculateSimilarity(targetColor, color);
        })).limit(12).collect(Collectors.toList());
        // 转换封装类后返回
        return sortedPictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        // 获取和校验参数
        ThrowUtils.throwIf(ObjUtil.isNull(pictureEditByBatchRequest), ErrorCode.PARAMS_ERROR);
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        String nameRule = pictureEditByBatchRequest.getNameRule();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);
        // 校验权限
        if (ObjUtil.isNotEmpty(spaceId)) {
            Space space = spaceApplicationService.getSpaceById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND);
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.ACCESS_DENIED);
        }
        // 查询指定图片（仅查询需要的字段）
        List<Picture> pictureList = repository.lambdaQuery().select(Picture::getId, Picture::getSpaceId).in(Picture::getId, pictureIdList).list();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureList), ErrorCode.NOT_FOUND);
        // 更新分类和标签
        pictureList.forEach(picture -> {
            if (ObjUtil.isNotEmpty(spaceId)) {
                picture.setSpaceId(spaceId);
            }
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        // 批量重命名
        fillPictureWithNameRule(pictureList, nameRule);
        boolean result = repository.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        ThrowUtils.throwIf(ObjUtil.isNull(createPictureOutPaintingTaskRequest) || createPictureOutPaintingTaskRequest.getPictureId() <= 0L, ErrorCode.PARAMS_ERROR);
        Picture picture = Optional.ofNullable(repository.getById(createPictureOutPaintingTaskRequest.getPictureId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "图片不存在"));
        // 权限校验
        // 改为使用注解鉴权
//        checkPictureAuth(loginUser, picture);
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }

    /**
     * 根据规则批量重命名 格式：图片{序号}
     * @param pictureList 图片列表
     * @param nameRule 命名规则
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重命名异常");
        }
    }
}




