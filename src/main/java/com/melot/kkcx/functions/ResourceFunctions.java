package com.melot.kkcx.functions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kk.module.resource.domain.Resource;
import com.melot.kk.module.resource.domain.ResourceUpLoadConf;
import com.melot.kk.module.resource.domain.ResourceUploadConfParam;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.*;
import com.melot.kktv.service.ConfigService;
import com.melot.qiniu.common.QiniuService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2017/9/21.
 */
public class ResourceFunctions {

    @Autowired
    private ConfigService configService;
    /**
     * 52080101 获取资源上传配置
     */
    public JsonObject getUpLoadConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        String tagCode_prefix = "52080101";
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (rtJO != null) {
            return rtJO;
        }
        JsonObject result = new JsonObject();
        try {
            Integer userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, tagCode_prefix+"01", 1, Integer.MAX_VALUE);
            Integer abroad = CommonUtil.getJsonParamInt(jsonObject, "abroad", 1, null, 1, Integer.MAX_VALUE);
            Integer resType = CommonUtil.getJsonParamInt(jsonObject, "resType", 0, tagCode_prefix+"02", 0, Integer.MAX_VALUE);
            Integer mimeType = CommonUtil.getJsonParamInt(jsonObject, "mimeType", 0, tagCode_prefix+"03", 1, Integer.MAX_VALUE);
            String  suffix = CommonUtil.getJsonParamString(jsonObject, "suffix", null, tagCode_prefix+"04", 1, 500);
            Integer appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            Integer resumeUp = CommonUtil.getJsonParamInt(jsonObject, "resumeUp", 0, null, 1, Integer.MAX_VALUE);
            Integer vframeSeconds = CommonUtil.getJsonParamInt(jsonObject, "vframeSeconds", 1, null, 1, Integer.MAX_VALUE);
            Integer transcoding = CommonUtil.getJsonParamInt(jsonObject, "transcoding", 0, null, 0, 1);
            ResourceNewService resourceNewService = (ResourceNewService) MelotBeanFactory.getBean("resourceNewService");
            ResourceUploadConfParam resourceUploadConfParam = new ResourceUploadConfParam();
            resourceUploadConfParam.setUserId(userId);
            resourceUploadConfParam.setAbroad(abroad);
            resourceUploadConfParam.setResType(resType);
            resourceUploadConfParam.setMimeType(mimeType);
            resourceUploadConfParam.setSuffix(suffix);
            resourceUploadConfParam.setAppId(appId);
            resourceUploadConfParam.setResumeUp(resumeUp);
            resourceUploadConfParam.setVframeSeconds(vframeSeconds);
            resourceUploadConfParam.setTranscoding(transcoding);
            Result<ResourceUpLoadConf> conf = resourceNewService.getUpLoadConf(resourceUploadConfParam);
            if(conf != null && conf.getCode() != null && conf.getCode().equals(CommonStateCode.SUCCESS)){
                ResourceUpLoadConf config = conf.getData();
                if(configService.getResourceType().contains(","+ resType+",")){
                    config.setResUpload(1);
                }
                else {
                    config.setResUpload(0);
                }
                result.add("config",new Gson().toJsonTree(config));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
            else {
                result.addProperty("TagCode", tagCode_prefix + "05");
            }
            return result;
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e){
            result.addProperty("TagCode",tagCode_prefix + "05");
            return result;
        }
    }

    /**
     * 52080102 添加资源
     */
    public JsonObject addResource(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        String tagCode_prefix = "52080102";
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if (rtJO != null) {
//            return rtJO;
//        }
        JsonObject result = new JsonObject();
        try{
            Integer userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, tagCode_prefix+"01", 0, Integer.MAX_VALUE);
            Integer resType = CommonUtil.getJsonParamInt(jsonObject, "resType", 0, tagCode_prefix+"02", 0, Integer.MAX_VALUE);
            Integer mimeType = CommonUtil.getJsonParamInt(jsonObject, "mimeType", 0, tagCode_prefix+"03", 1, Integer.MAX_VALUE);
            Integer eCloudType = CommonUtil.getJsonParamInt(jsonObject, "eCloudType", 0, tagCode_prefix+"04", 1, Integer.MAX_VALUE);
            String fileUrl = CommonUtil.getJsonParamString(jsonObject, "fileUrl", null, tagCode_prefix+"05", 1, 500);
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setResType(resType);
            resource.setMimeType(mimeType);
            resource.seteCloudType(eCloudType);
            if(mimeType == 2){
                resource.setImageUrl(fileUrl);
            }
            else {
                resource.setSpecificUrl(fileUrl);
                QiniuService qiniuService = new QiniuService(ConfigHelper.getAccessKey(), ConfigHelper.getSecretKey());
                resource.setDuration(qiniuService.getVideoDuration(ConfigHelper.getDomainURL(), fileUrl));
                String coverUrl =  fileUrl.substring(0,fileUrl.indexOf(".") + 1) + "jpg";
                resource.setImageUrl(coverUrl);
                VideoInfo videoInfo = getVideoInfoByHttp(fileUrl);
                resource.setFileWidth(videoInfo.getWidth());
                resource.setFileHeight(videoInfo.getHeight());
            }
            ResourceNewService resourceNewService = (ResourceNewService) MelotBeanFactory.getBean("resourceNewService");
            Result<Integer> resId = resourceNewService.addResource(resource);
            if(resId.getCode().equals(CommonStateCode.SUCCESS)){
                result.addProperty("resId",resId.getData());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
            else {
                result.addProperty("TagCode", tagCode_prefix + "06");
            }
            return result;
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e){
            result.addProperty("TagCode",tagCode_prefix + "06");
            return result;
        }

    }

    private static VideoInfo getVideoInfoByHttp(String videoUrl) {
        videoUrl = new StringBuilder().append(ConfigHelper.getVideoURL()).append(videoUrl).append("?avinfo").toString();
        VideoInfo videoInfo = null;
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
                                videoInfo = new VideoInfo();
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
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return videoInfo;
    }

    static class VideoInfo {

        private int width;

        private int height;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static void main(String[] args) {
        String fileUrl = "xxxx/xx.mp4";
    }

}
