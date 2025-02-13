package com.subaiqiao.yupicturebackend.common;

import lombok.Data;

/**
 * 分页对象
 */
@Data
public class PageRequest {

    /**
     * 当前页码
     */
    private int current = 1;
    /**
     * 页面大小
     */
    private int pageSize = 10;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";

}
