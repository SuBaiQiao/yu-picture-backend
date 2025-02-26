package com.subaiqiao.yupicturebackend.model.dto.space;

import com.subaiqiao.yupicturebackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间
 * @TableName space
 */
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * userId
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}