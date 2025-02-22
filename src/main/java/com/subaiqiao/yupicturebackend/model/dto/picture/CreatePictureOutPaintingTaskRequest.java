package com.subaiqiao.yupicturebackend.model.dto.picture;

import com.subaiqiao.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    private static final long serialVersionUID = -4290885532457123868L;
    private Long pictureId;
    private CreateOutPaintingTaskRequest.Parameters parameters;

}

