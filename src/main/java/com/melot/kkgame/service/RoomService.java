package com.melot.kkgame.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.RoomSubCatalogDao;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.kktv.util.Cache;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.HttpClient;
import com.melot.sdk.core.util.MelotBeanFactory;

public class RoomService {

    private static Logger logger = Logger.getLogger(RoomService.class);

    private static final Cache oneMiniteCahce = new Cache(60*1000);
    
    /**
     *  直播全站热门50位主播缓存key 
     */
    private static final String KKGAME_HOT_ACTOR_KEYS = "kkgame_hot_actors";
    
    /**
     *  获取热门主播 
     */
    @SuppressWarnings("unchecked")
    public static List<RoomInfo> getTopActors(RoomSubCatalogDao roomSubCatalogDao){
        List<RoomInfo> list = (List<RoomInfo>) oneMiniteCahce.getObject(KKGAME_HOT_ACTOR_KEYS);
        if(list != null){
            return list;
        }else{
            list = roomSubCatalogDao.getPartLiveRoomList(ConstantEnum.KKGAME_ALL_ACTORS_CATAID, 0, 50);
            oneMiniteCahce.insertObject(KKGAME_HOT_ACTOR_KEYS, list);
        }
        return list;
    }
    
    /**
     *  根据roomIds获取列表, roomIds以","作为分隔符 
     */
    public static List<RoomInfo> getRoomListByRoomIds(String roomIds){
      RoomInfoService roomInfoServie = (RoomInfoService)MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
      return roomInfoServie.getRoomListByRoomIds(roomIds);
    }
    
    /**
     * 发送消息到房间,通知
     * @param type
     * @param msg
     * @return
     */
    public static boolean sendMsgToRoom (int type, int roomId, int platform, JsonObject msg) {
        boolean result = false;
        Map<String, String>params =  new HashMap<String, String>();
        try {
            params.put("type",String.valueOf(type));
            params.put("msg", msg.toString());
            params.put("roomId", String.valueOf(roomId));
            if (platform > 0) {
            	params.put("platform", String.valueOf(platform));
            }
            String response = HttpClient.doGet(ConfigHelper.getRunwayUrl(), params);
            logger.info("sendRunwayMsg response: " + response);
        }catch (IOException e) {
            logger.error("sendRunwayMsg response error: " + params);
        }
        return result;
    }
    
}