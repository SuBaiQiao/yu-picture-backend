package com.subaiqiao.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.subaiqiao.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.subaiqiao.yupicturebackend.model.dto.picture.*;
import com.subaiqiao.yupicturebackend.model.dto.user.UserQueryRequest;
import com.subaiqiao.yupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 12947
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-15 13:48:27
*/
public interface PictureService extends IService<Picture> {
    /**
     * 校验图片
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     * @param inputSource 文件输入源
     * @param pictureUploadRequest 参数信息
     * @param loginUser 上传用户
     * @return 上传结果对象
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 分页获取脱敏图片信息
     * @param picturePage 原数据
     * @param request 请求头
     * @return 脱敏后的图片分页信息
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取托名后的图片对象
     * @param picture 原图片对象
     * @return 脱敏后的图片对象
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取查询条件
     * @param pictureQueryRequest 查询请求
     * @return 转换后的查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 图片审核
     * @param pictureReviewRequest 图片审核信息
     * @param loginUser 登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     * @param picture 图片信息
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量爬取图片
     * @param pictureUploadByBatchRequest 图片上传请求
     * @param loginUser 登录用户
     * @return 成功条数
     */
    int uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 清理图片文件
     * @param picture 需要删除的图片
     */
    void clearPicture(Picture picture);

    /**
     * 校验空间图片的权限
     * @param loginUser 登录用户
     * @param picture 图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 登录用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     * @param pictureEditRequest 编辑请求内容
     * @param loginUser 登录用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据图片颜色搜索图片
     * @param spaceId 空间ID
     * @param picColor 需要匹配的颜色
     * @param loginUser 登录用户
     * @return 所查询到符合结果的图片信息
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片信息
     * @param pictureEditByBatchRequest 批量编辑请求
     * @param loginUser 登录用户
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     * @param createPictureOutPaintingTaskRequest 创建信息
     * @param loginUser 登录用户
     * @return 创建结果
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
