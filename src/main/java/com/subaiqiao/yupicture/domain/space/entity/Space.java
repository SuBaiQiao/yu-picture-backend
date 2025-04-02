package com.subaiqiao.yupicture.domain.space.entity;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.subaiqiao.yupicture.domain.space.valueobject.SpaceLevelEnum;
import com.subaiqiao.yupicture.domain.space.valueobject.SpaceTypeEnum;
import com.subaiqiao.yupicture.infrastructure.exception.ErrorCode;
import com.subaiqiao.yupicture.infrastructure.exception.ThrowUtils;
import lombok.Data;

/**
 * 空间
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public void validSpace(boolean add) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(this), ErrorCode.PARAMS_ERROR);

        String spaceName = this.getSpaceName();
        Integer spaceLevel = this.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);

        Integer spaceType = this.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);

        if (add) {
            ThrowUtils.throwIf(ObjUtil.isNull(spaceName), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(ObjUtil.isNull(spaceType), ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtils.throwIf(spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称 过长");
        }
        ThrowUtils.throwIf(ObjUtil.isNotNull(spaceLevel) && ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "空间级别不存在");
        ThrowUtils.throwIf(ObjUtil.isNotNull(spaceType) && ObjUtil.isNull(spaceTypeEnum), ErrorCode.PARAMS_ERROR, "空间类型不存在");
    }
}