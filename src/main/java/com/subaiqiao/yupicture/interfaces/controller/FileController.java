package com.subaiqiao.yupicture.interfaces.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.subaiqiao.yupicture.infrastructure.annotation.AuthCheck;
import com.subaiqiao.yupicture.infrastructure.common.BaseResponse;
import com.subaiqiao.yupicture.infrastructure.common.ResultUtils;
import com.subaiqiao.yupicture.domain.user.constant.UserConstant;
import com.subaiqiao.yupicture.infrastructure.exception.BusinessException;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.api.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File tempFile = null;
        try {
            tempFile = File.createTempFile(filepath, null);
            multipartFile.transferTo(tempFile);
            cosManager.putObject(filepath, tempFile);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (tempFile != null) {
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载
     * @param filepath 文件路径
     * @param response 响应对象
     * @throws IOException 异常信息
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(@RequestParam("filepath") String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream objectContent = null;
        try {
            COSObject object = cosManager.getObject(filepath);
            objectContent = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectContent);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
        }
    }
}
