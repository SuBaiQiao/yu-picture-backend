package com.subaiqiao.yupicturebackend.api.imagesearch.model;

import lombok.Data;

/**
 * 图片搜索结果
 */
@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thumbUrl;
    /**
     * 原图地址
     */
    private String fromUrl;
}
