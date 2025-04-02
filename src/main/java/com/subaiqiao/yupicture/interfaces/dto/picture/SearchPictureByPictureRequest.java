package com.subaiqiao.yupicture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {
    private static final long serialVersionUID = 3446593203790627057L;
    private Long pictureId;

}
