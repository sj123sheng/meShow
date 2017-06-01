package com.melot.kkcx.transform;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.driver.domain.FriendEmoticon;
import com.melot.common.driver.domain.FriendEmoticonResult;
import com.melot.kktv.util.PlatformEnum;

public class FriendEmoticonTF {

    /**
     * 根据平台组装交友房的表情信息
     * @param friendEmoticon
     * @param platform
     * @return
     */
    public static JsonObject toJsonObject(FriendEmoticon friendEmoticon, int platform) {
        JsonObject jsonObject = new JsonObject();
        
        Integer id = friendEmoticon.getEmoticonId();
        String name = friendEmoticon.getEmoticonName();
        Long version = friendEmoticon.getVersion();
        
        String icon;
        String resource;
        
        if (PlatformEnum.ANDROID == platform) {
            
            icon = friendEmoticon.getIconApp();
            resource = friendEmoticon.getResourceApp();
            
        }else if (PlatformEnum.IPHONE == platform) {
            
            icon = friendEmoticon.getIconApp();
            resource = friendEmoticon.getResourceApp();
            
        }else if (PlatformEnum.IPAD == platform) {
            
            icon = friendEmoticon.getIconApp();
            resource = friendEmoticon.getResourceApp();
            
        }else {
            icon = friendEmoticon.getIconWeb();
            resource = friendEmoticon.getResourceWeb();
            
            List<FriendEmoticonResult> results = friendEmoticon.getSubResoutces();
            
            if (results != null && !results.isEmpty()) {
                JsonArray subResoutces = new JsonArray();
                
                for (FriendEmoticonResult result : results) {
                    JsonObject resultJson = new JsonObject();
                    
                    if (result.getResult() == null || result.getResourceWeb() == null) {
                        continue;
                    }
                    resultJson.addProperty("result", result.getResult());
                    resultJson.addProperty("subResource", result.getResourceWeb());
                    
                    subResoutces.add(resultJson);
                }
                
                if (subResoutces.size() > 0) {
                    jsonObject.add("subResoutces", subResoutces);
                }
            }
        }
        
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("icon", icon);
        jsonObject.addProperty("resource", resource);
        jsonObject.addProperty("version", version);
        
        return jsonObject;
    }
}
