package com.subaiqiao.yupicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页对象
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private long id;

    private static final long serialVersionUID = 1L;

}
