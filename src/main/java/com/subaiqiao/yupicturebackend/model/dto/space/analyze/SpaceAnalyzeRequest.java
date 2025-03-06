package com.subaiqiao.yupicturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = -3139999893815471441L;
    private Long spaceId;
    private boolean queryPublic;
    private boolean queryAll;

}
