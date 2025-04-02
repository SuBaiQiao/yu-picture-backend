package com.subaiqiao.yupicture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片
 * @TableName picture
 */
@Data
public class PictureReviewRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 审核状态：0-待审核, 1-审核通过, 2-审核不通过
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}