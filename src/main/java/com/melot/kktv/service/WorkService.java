package com.melot.kktv.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.melot.kktv.domain.WorkVideoInfo;
import com.melot.kktv.util.ConfigHelper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @description: WorkService
 * @author: shengjian
 * @date: 2018/8/13
 * @copyright: Copyright (c)2018
 * @company: melot
 * <p>
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2018/8/13           shengjian     1.0
 */
public class WorkService {

    private static Logger logger = Logger.getLogger(StatisticsServices.class);

    public static WorkVideoInfo getVideoInfoByHttp(String videoUrl) {
        videoUrl = new StringBuilder().append(ConfigHelper.getVideoURL()).append(videoUrl).append("?avinfo").toString();
        WorkVideoInfo videoInfo = null;
        @SuppressWarnings("deprecation")
        CloseableHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(videoUrl);
        HttpResponse res = null;
        try {
            res = httpClient.execute(get);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(res.getEntity());
                JSONObject jObject = JSON.parseObject(result);
                if (jObject.containsKey("streams")) {
                    JSONArray jArray = jObject.getJSONArray("streams");
                    if (jArray.size() > 0) {
                        for (Object temp : jArray) {
                            JSONObject js = (JSONObject) temp;
                            if (js.containsKey("codec_name") && js.get("codec_name").equals("h264")) {
                                videoInfo = new WorkVideoInfo();
                                videoInfo.setWidth((int) js.get("width"));
                                videoInfo.setHeight((int) js.get("height"));
                                if (js.containsKey("tags")) {
                                    JSONObject tag = js.getJSONObject("tags");
                                    if (tag.containsKey("rotate")) {
                                        String rotate = (String) tag.get("rotate");
                                        if (rotate.equals("90") || rotate.equals("270")) {
                                            // 是否旋转
                                            videoInfo.setHeight((int) js.get("width"));
                                            videoInfo.setWidth((int) js.get("height"));
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
        } catch (IllegalStateException e) {
            logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
                }
            }
        }
        return videoInfo;
    }

}
