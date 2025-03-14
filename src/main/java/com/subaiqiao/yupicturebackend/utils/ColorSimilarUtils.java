package com.subaiqiao.yupicturebackend.utils;

import java.awt.*;

public class ColorSimilarUtils {

    private ColorSimilarUtils(){}

    /**
     * 计算两个颜色之间的相似度
     * @param color1 第一个颜色
     * @param color2 第二个颜色
     * @return 相似度（0-1，1为完全相同）
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    /**
     * 根据十六进制颜色代码计算相似度
     * @param hexColor1 第一个颜色的十六进制代码（如0xFF0000）
     * @param hexColor2 第二个颜色的十六进制代码（如0xFE0101）
     * @return 相似度（0-1，1为完全相同）
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        return calculateSimilarity(color1, color2);
    }
}
