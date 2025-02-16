package com.subaiqiao.yupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.subaiqiao.yupicturebackend.config.CosClientConfig;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import com.subaiqiao.yupicturebackend.manager.CosManager;
import com.subaiqiao.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource 输入源
     * @param file 本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 获取输入源的原始文件名
     * @param inputSource 输入源
     * @return 文件原始名称
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验输入源（本地文件或URL）
     * @param inputSource 输入源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 上传图片
     * @param inputSource 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return 图片信息
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验图片
        validPicture(inputSource);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增加安全性
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        // 解析结果并返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取压缩之后得到的文件信息
                CIObject compressCiObject = objectList.get(0);
                CIObject thumbnailCiObject = compressCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originalFilename, compressCiObject, thumbnailCiObject);
            }
            return buildResult(imageInfo, uploadPath, originalFilename, file);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 临时文件清理
            this.deleteTempFile(file);
        }
    }

    /**
     * 封装文件上传对象
     * @param originalFilename 文件原名称
     * @param compressCiObject 压缩后的对象
     * @return 文件上传对象
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressCiObject, CIObject thumbnailCiObject) {
        // 获取宽高，计算比例
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // 压缩后的原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());
        // 缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 封装文件上传对象
     * @param imageInfo 图片信息对象
     * @param uploadPath 上传地址
     * @param originalFilename 文件名称
     * @param file 文件
     * @return 文件上传对象
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
        // 获取宽高，计算比例
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 清理临时文件
     * @param tempFile 临时文件
     */
    public static void deleteTempFile(File tempFile) {
        if (ObjUtil.isNull(tempFile)) {
            return;
        }
        boolean deleteResult = tempFile.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", tempFile.getAbsolutePath());
        }
    }

}
