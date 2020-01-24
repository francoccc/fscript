package com.franco.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * <h>IOUtil</h>
 *
 * @author franco
 */
public class IOUtil {

    /**
     * 取得当前类路径下的res完整路径
     * url.getPath()获取编码后url路径
     * URLDecode.decode(path）以utf-8的解码
     *
     * @param fileName
     * @return
     */
    public static String getResourcePath(String fileName) {
        URL url = IOUtil.class.getResource(fileName);
        String path = url.getPath();
        String decodePath = null;
        decodePath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        return decodePath;
    }

    /**
     * Close io stream gracefully
     *
     * @param io
     */
    public static void closeGracefully(java.io.Closeable io) {
        if(io == null) {
            return;
        }
        try {
            io.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
