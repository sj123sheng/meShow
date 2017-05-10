package com.melot.kkgame.model.transform;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.opus.domain.UserNews;

public class UserNewsTF {

	private static Logger logger = Logger.getLogger(UserNewsTF.class);
	
	/**
     * @param userNews
     * @return
     */
    public static JsonObject toOPusJsonObject(UserNews userNews, int platform) {    
        JsonObject jObject = new JsonObject();
        try {
            jObject.addProperty("newsId", userNews.getNewsId());
            jObject.addProperty("title", userNews.getMediaTitle());
            JsonObject mediaSourceJson = new JsonParser().parse(userNews.getMediaSource()).getAsJsonObject();
            if (mediaSourceJson.has("mediaUrl") && !mediaSourceJson.get("mediaUrl").isJsonNull()) {
                jObject.addProperty("mediaUrl", mediaSourceJson.get("mediaUrl").getAsString());
            }
            if (mediaSourceJson.has("imageUrl") && !mediaSourceJson.get("imageUrl").isJsonNull()) {
                if(platform == PlatformEnum.WEB) {
                    jObject.addProperty("imageUrl_290", mediaSourceJson.get("imageUrl").getAsString() + "?imageView2/1/w/290/h/163");
                }else{
                    jObject.addProperty("imageUrl_272", mediaSourceJson.get("imageUrl").getAsString() + "?imageView2/1/w/272/h/204");
                }
            }
            if (!StringUtil.strIsNull(userNews.getContent())) {
                jObject.addProperty("content", userNews.getContent());
            }
            if (platform == PlatformEnum.WEB) {
                jObject.addProperty("state", userNews.getState());
                jObject.addProperty("publishTime", userNews.getPublishedTime().getTime());
                if (userNews.getState() == 3) { // 资源审核状态1:未审核,2:审核通过,3:审核不通过,4:用户删除,5:官方录制,6:后台删除
                    jObject.addProperty("reason", userNews.getReason());
                }
            }
            Integer appreciateCount = userNews.getAppreciateCount();
            appreciateCount = appreciateCount != null ? appreciateCount : 0;
            jObject.addProperty("appreciateCount", appreciateCount);
            jObject.addProperty("viewTimes", userNews.getViewTimes());
        } catch (Exception e) {
            logger.error("UserNewsTF.toOPusJsonObject exception, resId : " + userNews.getNewsId(), e);
            return null;
        }
        
        return jObject;
    }

}
