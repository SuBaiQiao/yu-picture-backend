package com.subaiqiao.yupicturebackend.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.subaiqiao.yupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetImageListApi {

    public static List<ImageSearchResult> getImageList(String url) {
        try {
            HttpResponse response = HttpUtil.createGet(url).execute();
            int statusCode = response.getStatus();
            String body = response.body();
            if (statusCode == 200) {
                return processResponse(body);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    public static List<ImageSearchResult> processResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResult.class);
    }
}
