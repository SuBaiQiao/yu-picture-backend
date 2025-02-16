package com.subaiqiao.yupicturebackend.model.dto.picture;

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