package com.github.wensimin.ashioarae.service.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * http工具类
 */
public class HttpUtils {

    public static Map<String, String> cookie2map(String cookies) {
        Map<String, String> map = new HashMap<>();
        String[] cookiesArray = cookies.split(";");
        for (String c : cookiesArray) {
            String[] cookie = c.split("=");
            map.put(cookie[0].trim(), cookie[1].trim());
        }
        return map;
    }

}
