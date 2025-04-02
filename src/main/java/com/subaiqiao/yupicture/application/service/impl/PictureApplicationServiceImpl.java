package com.subaiqiao.yupicture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicture.application.service.PictureApplicationService;
import com.subaiqiao.yupicture.application.service.UserApplicationService;
import com.subaiqiao.yupicture.domain.picture.service.PictureDomainService;
import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.interfaces.dto.picture.*;
import com.subaiqiao.yupicture.interfaces.vo.user.UserVO;
import com.subaiqiao.yupicture.infrastructure.manager.upload.dto.file.UploadPictureResult;
import com.subaiqiao.yupicture.domain.picture.entity.Picture;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.interfaces.vo.picture.PictureVO;
import com.subaiqiao.yupicture.infrastructure.mapper.PictureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 12947
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-15 13:48:27
*/
@Slf4j
@Service
public class PictureApplicationServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureApplicationService {

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private UserApplicationService userApplicationService;

    @Override
    public void validPicture(Picture picture) {
        pictureDomainService.validPicture(picture);
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return pictureDomainService.uploadPicture(inputSource, pictureUploadRequest, loginUser);
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
        return pictureDomainService.getQueryWrapper(pictureQueryRequest);
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        pictureDomainService.doPictureReview(pictureReviewRequest, loginUser);
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
        pictureDomainService.fillReviewParams(picture, loginUser);
    }

    @Override
    public int uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        return pictureDomainService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
    }

    /**
     * 清理图片文件
     * @param picture 需要删除的图片
     */
    @Async
    @Override
    public void clearPicture(Picture picture) {
        pictureDomainService.clearPicture(picture);
    }

    /**
     * 校验空间图片的权限
     * @param loginUser 登录用户
     * @param picture 图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        pictureDomainService.checkPictureAuth(loginUser, picture);
    }

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 登录用户
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        pictureDomainService.deletePicture(pictureId, loginUser);
    }

    /**
     * 编辑图片
     * @param pictureEditRequest 编辑请求内容
     * @param loginUser 登录用户
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        pictureDomainService.editPicture(pictureEditRequest, loginUser);
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        return pictureDomainService.searchPictureByColor(spaceId, picColor, loginUser);
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        pictureDomainService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        return pictureDomainService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
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




