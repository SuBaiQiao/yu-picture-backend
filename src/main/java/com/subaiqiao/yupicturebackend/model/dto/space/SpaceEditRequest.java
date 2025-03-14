package com.subaiqiao.yupicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {
    private static final long serialVersionUID = -2964989559819079784L;
    private String spaceName;
    private Long id;
}
