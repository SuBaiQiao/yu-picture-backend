package com.subaiqiao.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = -7953305185256045180L;
    private String searchText;
    private Integer count = 10;
    private String namePrefix;

}
