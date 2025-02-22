package com.subaiqiao.yupicturebackend.api.aliyunai;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.subaiqiao.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.subaiqiao.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.subaiqiao.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * AI 扩图任务创建
     * @param createOutPaintingTaskRequest 创建信息
     * @return AI扩图任务ID
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (ObjUtil.isEmpty(createOutPaintingTaskRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        try (HttpResponse response = httpRequest.execute()) {
            if (!response.isOk()) {
                log.error("请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(response.body(), CreateOutPaintingTaskResponse.class);
            if (createOutPaintingTaskResponse.getCode() != null) {
                String message = createOutPaintingTaskResponse.getMessage();
                log.error("请求异常：{}", message);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败. " + message);
            }
            return createOutPaintingTaskResponse;
        }
    }

    /**
     * 查询创建的任务结果
     * @param taskId 任务ID
     * @return 任务结果
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!response.isOk()) {
                log.error("请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(response.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
