package com.subaiqiao.yupicture.infrastructure.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;


/**
 * 文件上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) throws Exception{
        ((MultipartFile) inputSource).transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        return ((MultipartFile) inputSource).getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件不能大于 2MB");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀列表
        final List<String> ALLOW_SUFFIX_LIST = List.of("jpg", "jpeg", "png", "gif", "webp");
        ThrowUtils.throwIf(!ALLOW_SUFFIX_LIST.contains(fileSuffix.toLowerCase()), ErrorCode.PARAMS_ERROR, "不支持的文件格式");
    }
}
