package com.subaiqiao.yupicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.subaiqiao.yupicturebackend.model.entity.Picture;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SpaceVO implements Serializable {
    private static final long serialVersionUID = -2332566483960628369L;
    /**
     * id
     */
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

    private UserVO user;

    /**
     * 封装类转对象
     * @param spaceVO 封装类
     * @return 对象信息
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (ObjUtil.isNull(spaceVO)) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 对象转封装类
     * @param space 对象信息
     * @return 封装对象信息
     */
    public static SpaceVO objToVo(Space space) {
        if (ObjUtil.isNull(space)) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }
}
