package com.subaiqiao.yupicture.interfaces.dto.picture;

import com.subaiqiao.yupicture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    private static final long serialVersionUID = -4290885532457123868L;
    private Long pictureId;
    private CreateOutPaintingTaskRequest.Parameters parameters;

}

