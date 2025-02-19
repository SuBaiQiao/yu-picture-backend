package com.subaiqiao.yupicturebackend.model.dto.picture;

import com.baomidou.mybatisplus.annotation.*;
import com.subaiqiao.yupicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片
 * @TableName picture
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 审核状态：0-待审核, 1-审核通过, 2-审核不通过
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;
    /**
     * 审核人ID
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private Date reviewTime;

    private String searchText;

    private Long spaceId;

    private boolean nullSpaceId;
    /**
     * 开始时间
     */
    private Date startEditTime;
    /**
     * 结束时间
     */
    private Date endEditTime;


    private static final long serialVersionUID = 1L;
}