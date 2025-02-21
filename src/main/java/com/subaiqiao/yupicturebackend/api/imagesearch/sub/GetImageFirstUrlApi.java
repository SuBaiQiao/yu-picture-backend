package com.subaiqiao.yupicturebackend.api.imagesearch.sub;

import com.subaiqiao.yupicturebackend.exception.BusinessException;
import com.subaiqiao.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取图片列表接口的Api（step 2）
 */
@Slf4j
public class GetImageFirstUrlApi {
    public static String getImageFirstUrl(String url) {
        try {
            Document document = Jsoup.connect(url).timeout(5000).get();
            Elements scriptElements = document.getElementsByTag("script");
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if (scriptContent.contains("\"firstUrl\"")) {
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        // 处理文字转义
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未查询到有效URL");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未查询到有效URL");
        } catch (Exception e) {
            log.error("GetImageFirstUrlApi", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }
}
