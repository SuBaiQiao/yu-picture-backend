package com.subaiqiao.yupicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {
    private static final long serialVersionUID = -2964989559819079784L;
    private String spaceName;
    private Integer spaceLevel;
    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;

}
