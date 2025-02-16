package com.subaiqiao.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = -7953305185256045180L;
    private Long id;
    private String fileUrl;
    private String picName;
    private Long spaceId;

}
