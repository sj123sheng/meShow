package com.melot.kktv.model.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.util.StringUtil;
import com.melot.resource.domain.Resource;
import com.melot.singlechat.driver.domain.SingleChatLabel;
import com.melot.singlechat.driver.domain.SingleChatServer;

/**
 * Title: SingleChatServerTF
 * <p>
 * Description: 组装客户端需要信息
 * </p>
 * 
 * @author 魏安稳<a href="mailto:anwen.wei@melot.cn"/>
 * @version V1.0
 * @since 2017年8月25日 上午11:28:41
 */
public class SingleChatServerTF {
    
    private SingleChatServerTF() {}
    
    public static JsonObject serverInfoToJson(SingleChatServer server) {
        JsonObject result = new JsonObject();
        result.addProperty("serverId", server.getServerId());
        result.addProperty("actorId", server.getUserId());
        result.addProperty("typeId", server.getTypeId());
        result.addProperty("typeName", server.getTypeName());
        result.addProperty("content", server.getContent());
        result.addProperty("state", server.getState());
        result.addProperty("checkContent", server.getCheckContent());
        result.addProperty("price", server.getPrice());
        result.addProperty("unit", server.getUnit());
        result.addProperty("totalOrderCount", server.getTotalOrderCount());
        
        JsonArray resVideos = new JsonArray();
        if (server.getVideoResources() != null && !server.getVideoResources().isEmpty()) {
            for (Resource resource : server.getVideoResources()) {
                if (StringUtil.strIsNull(resource.getSpecificUrl())) {
                    continue;
                }
                JsonObject videoJson = new JsonObject();
                videoJson.addProperty("videoUrl", resource.getSpecificUrl());
                videoJson.addProperty("videoDur", resource.getDuration());
                videoJson.addProperty("videoFrom", resource.getTitle());
                videoJson.addProperty("videoWidth", resource.getFileWidth());
                videoJson.addProperty("videoHeight", resource.getFileHeight());
                if (!StringUtil.strIsNull(resource.getImageUrl())) {
                    videoJson.addProperty("imgUrl_400", resource.getImageUrl() + "?imageView2/1/w/400/h/225");
                    videoJson.addProperty("imgUrl", resource.getImageUrl());
                }else {
                    videoJson.addProperty("imgUrl_400", "");
                    videoJson.addProperty("imgUrl", "");
                }
                
                videoJson.addProperty("checkState", stateTF(resource.getState()));
                
                resVideos.add(videoJson);
            }
        }
        result.add("resVideos", resVideos);
        
        JsonArray resAudios = new JsonArray();
        if (server.getAudioResources() != null && !server.getAudioResources().isEmpty()) {
            for (Resource resource : server.getAudioResources()) {
                if (StringUtil.strIsNull(resource.getSpecificUrl())) {
                    continue;
                }
                JsonObject audioJson = new JsonObject();
                audioJson.addProperty("audioUrl", resource.getSpecificUrl());
                audioJson.addProperty("imgUrl_400", resource.getImageUrl() + "!400");
                audioJson.addProperty("checkState", stateTF(resource.getState()));
                
                resAudios.add(audioJson);
            }
        }
        result.add("resAudios", resAudios);
        
        JsonArray resImages = new JsonArray();
        if (server.getImageResources() != null && !server.getImageResources().isEmpty()) {
            for (Resource resource : server.getImageResources()) {
                if (StringUtil.strIsNull(resource.getImageUrl())) {
                    continue;
                }
                JsonObject imageJson = new JsonObject();
                imageJson.addProperty("imgUrl", resource.getImageUrl());
                imageJson.addProperty("imgUrl_1280", resource.getImageUrl() + "!1280");
                imageJson.addProperty("imgUrl_720", resource.getImageUrl() + "!720");
                imageJson.addProperty("imgUrl_640", resource.getImageUrl() + "!640");
                imageJson.addProperty("checkState", stateTF(resource.getState()));
                
                resImages.add(imageJson);
            }
        }
        result.add("resImages", resImages);
        
        JsonArray labels = new JsonArray();
        if (server.getLabelInfos() !=null && !server.getLabelInfos().isEmpty()) {
            for (SingleChatLabel label : server.getLabelInfos()) {
                JsonObject labelJson = new JsonObject();
                labelJson.addProperty("id", label.getId());
                labelJson.addProperty("name", label.getName());
                labels.add(labelJson);
            }
        }
        result.add("labels", labels);
        return result;
    }
    
    /**
     * 将资源状态转化为1v1服务技能状态一致
     * @param resourceState
     * @return
     */
    private static int stateTF(Integer resourceState) {
        if (resourceState == null) {
            return -1;
        }
        switch (resourceState) {
        case 0:// 用户删除
            return -1;
        case 1:// 通过
            return 1;
        case 2:// 不通过
            return 2;
        case 3:// 待审核
            return 0;

        default:
            return resourceState;
        }
    }
}
