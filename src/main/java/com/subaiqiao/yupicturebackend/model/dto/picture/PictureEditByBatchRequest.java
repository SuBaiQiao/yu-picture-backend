package com.subaiqiao.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量编辑请求
 */
@Data
public class PictureEditByBatchRequest implements Serializable {
    private static final long serialVersionUID = -7953305185256045180L;
    private List<Long> pictureIdList;

    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private String nameRule;
}
