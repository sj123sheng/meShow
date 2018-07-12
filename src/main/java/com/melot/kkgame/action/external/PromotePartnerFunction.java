/**
 * This document and its contents are protected by copyrightezu 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action.external;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.domain.PartnerPromote;
import com.melot.game.config.sdk.promote.service.PartnerPromoteService;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.HallRoomService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.util.ResultUtils;
import com.melot.kkgame.action.BaseAction;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.ChannelEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.commons.collections.CollectionUtils;

/**
 * Title: PromotePartnerFunction
 * <p>
 * Description: 
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-9-25 上午10:19:13
 */
public class PromotePartnerFunction extends BaseAction{

    @Resource
    private HallRoomService hallRoomService;

    /**
     * 合作推广信息查询
     * {functag:,20020081}
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getValiablePartnerPromotes(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
       
        int channelId;
        int cataId;
        int count;
        try {
            channelId = CommonUtil.getJsonParamInt(jsonObject, "c", ChannelEnum.DEFAUL_WEB_CHANNEL, TagCodeEnum.CHANNEL_MISSING, 0 , Integer.MAX_VALUE);
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, null, 0 , Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 1, null, 0 , Integer.MAX_VALUE);
         // 改接口下新增推荐房间, 用于推广房间   
         //   cataId = 356
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
           result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
           return result;
        }
        
        PartnerPromoteService partnerPromoteService = MelotBeanFactory.getBean("partnerPromoteService", PartnerPromoteService.class);
        JsonArray jsonArray = new JsonArray();            
        List<PartnerPromote> list = partnerPromoteService.getValiablePartnerPromotes(channelId, 1);
        Map<Integer, List<PartnerPromote>> map = groupMap(list);
        for (Map.Entry<Integer, List<PartnerPromote>>entry : map.entrySet()) {
            JsonObject partnerObj = new JsonObject();
            JsonArray array = new JsonArray();
            partnerObj.addProperty("type", entry.getKey());
            partnerObj.addProperty("title", getPromoteType(entry.getKey()));
            for (PartnerPromote partnerPromote : entry.getValue()) {
                JsonObject json = getPartnerPromoteJson(partnerPromote);
                array.add(json);
            }
            partnerObj.add("partnerList", array);
            jsonArray.add(partnerObj);
        }
        
        if (cataId > 0) {
            JsonArray roomArray = getPromoteRoomList(cataId, count);
            if (roomArray != null && roomArray.size() > 0) {
                result.add("roomList", roomArray);
            }
        }
        result.add("promoteList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        
        return result;
    }
    
    
    /***
     * 获取推广栏目在播房间
     * 一些第三方挂在的页面,除了挂载广告位,合作伙伴, 还需获取一定栏目下的在播房间
     */
    private JsonArray getPromoteRoomList( int cataId, int count){
        JsonArray roomArray = new JsonArray();
        Result<List<HallRoomInfoDTO>> partLiveRoomListResult = hallRoomService.getPartLiveRoomList(cataId, 0, count);
        if (ResultUtils.checkResultNotNull(partLiveRoomListResult)) {
            for (HallRoomInfoDTO roomInfo : partLiveRoomListResult.getData()) {
                roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, 1));
            }
        }
        return roomArray;
    }
    
    
    /**
     * 
     * @param partnerPromote
     * @return
     */
    private JsonObject getPartnerPromoteJson(PartnerPromote partnerPromote) {
        JsonObject json = new JsonObject();
        json.addProperty("id", partnerPromote.getId());
        json.addProperty("picPath", partnerPromote.getPicPath());
        json.addProperty("partnerTitle", partnerPromote.getTitle());
        if(!StringUtil.strIsNull(partnerPromote.getUrl())){
            json.addProperty("url", partnerPromote.getUrl());
        }
        json.addProperty("proType", partnerPromote.getProType());
        json.addProperty("sortIndex", partnerPromote.getSortIndex());
        json.addProperty("parnter", partnerPromote.getTitle());
        return json;
    }
    
    private Map<Integer, List<PartnerPromote>> groupMap(List<PartnerPromote>list){
        if(CollectionUtils.isEmpty(list)){
            return new LinkedHashMap<>();
        }
        Map<Integer,List<PartnerPromote>> matchMaps = new LinkedHashMap<Integer,  List<PartnerPromote>>();
        List<PartnerPromote> tempEventList;
        for (PartnerPromote object : list) {
            int type = object.getProType();
            if(!matchMaps.containsKey(type)){
                tempEventList = new ArrayList<>(16);
                tempEventList.add(object);
                matchMaps.put(type, tempEventList);
            }else{
                matchMaps.get(type).add(object);
            }
        }
        return matchMaps;
    }
    
    /**
     *   
     * 
     */
    private String getPromoteType(int type){
        String result;
        switch(type){
            case 1: result = "广告位";break;
            case 2: result = "合作媒体";break;
            case 3: result = "合作伙伴";break;
            default:result ="预留推荐";break;
        }
        return result;    
    }
    
}
