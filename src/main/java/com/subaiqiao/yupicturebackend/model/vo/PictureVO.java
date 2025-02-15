package com.subaiqiao.yupicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.subaiqiao.yupicturebackend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {
    private static final long serialVersionUID = -2332566483960628369L;
    private Long id;
    private String url;
    private String name;
    private String introduction;
    private List<String> tags;
    private String category;
    private Long picSize;
    private Integer picWidth;
    private Integer picHeight;
    private Double picScale;
    private String picFormat;
    private Long userId;
    private Date createTime;
    private Date editTime;
    private Date updateTime;
    private UserVO user;

    /**
     * 封装类转对象
     * @param pictureVO 封装类
     * @return 对象信息
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (ObjUtil.isNull(pictureVO)) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        // 类型转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     * @param picture 对象信息
     * @return 封装对象信息
     */
    public static PictureVO objToVo(Picture picture) {
        if (ObjUtil.isNull(picture)) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
