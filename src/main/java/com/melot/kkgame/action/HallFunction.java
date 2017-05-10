/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.RoomSubCatalogDao;
import com.melot.api.menu.sdk.dao.SysMenuDao;
import com.melot.api.menu.sdk.dao.domain.HomePage;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.api.menu.sdk.service.PartService;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.api.menu.sdk.service.RoomSubCatalogService;
import com.melot.api.menu.sdk.service.impl.DefaultPartServiceImpl;
import com.melot.api.menu.sdk.utils.RandomUtils;
import com.melot.blacklist.service.BlacklistService;
import com.melot.content.config.game.service.GameTagService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kkgame.redis.HallPartSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.service.HallPageService;
import com.melot.kkgame.service.RoomService;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.model.Room;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.Cache;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: HallFunction
 * <p>
 * Description:新版大厅接口
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-6-12 上午11:25:29
 */
public class HallFunction extends BaseAction{
    
    private static Cache cache = new Cache(60 * 1000);
    
    private static final Logger logger = Logger.getLogger(HallFunction.class);
    private final int MAX_COUNT_OF_LIVING_ROOM = 100;   
    
    private HallPageService hallPageService;
    private HallPartSource hallPartSource;
    private RoomSubCatalogService roomSubCatalogService;
    
    public void setHallPageService(HallPageService hallPageService) {
        this.hallPageService = hallPageService;
    }

    public void setHallPartSource(HallPartSource hallPartSource) {
        this.hallPartSource = hallPartSource;
    }

	public void setRoomSubCatalogService(RoomSubCatalogService roomSubCatalogService) {
		this.roomSubCatalogService = roomSubCatalogService;
	}

	/**
     * 获得大厅显示板块(20010301)
     * @param jsonObject
     * @return
     */
    public JsonObject getHallPlateList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        int appId = 0;
        int channel = 0;
        try {
            //platform必填
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        List<HomePage> tempList = null;
        try {
            /*
             * 直播大厅暂时使用随机的方式获取
                FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);//暂时用本地temp接口
                tempList = firstPageHandler.getFistPagelist(appId, channel, platform);
             */
            tempList = hallPageService.getFistPagelist(appId, channel, platform);
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for(HomePage temp : tempList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", temp.getSeatId());
                json.addProperty("type", temp.getSeatType());
                json.addProperty("title", temp.getTitleName());
                json.addProperty("cdnState", temp.getCdnState());
                json.addProperty("icon", ConstantEnum.FUN_ICON_HOME + temp.getDetailId()+".png");
                if(temp.getSubTitle() != null ) {
                    json.addProperty("subTitle", temp.getSubTitle());
                }
                if (temp.getRoomTotal() != null) {
                    json.addProperty("roomTotal", temp.getRoomTotal());
                }
                if (temp.getLiveTotal() != null) {
                    json.addProperty("liveTotal", temp.getLiveTotal());
                }
                if(temp.getDetailId() != null) {
                    //专区编号
                    json.addProperty("id", temp.getDetailId());
                }
                if(temp.getCdnState() != null) {
                    if(temp.getCdnState() > 0 && temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<RoomInfo> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (RoomInfo roomInfo : roomList) {
                                roomArray.add(RoomTF.roomInfoToJson(roomInfo, platform));
                            }
                        }
                        json.add("result", roomArray);
                    } else {
                        json.add("result", new JsonArray());
                    }
                }
                plateList.add(json);
            }
            result.add("plateList", plateList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }
        
        return result;
    }
    
    /**
     * 获得kk游戏推荐栏目(20010304)
     * 即获取设置了封面海报的栏目
     * @param jsonObject
     * @return
     */
    public JsonObject getKKRecommended(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int appId = 0;
        int count = 0;
        int platform = 0;
        String parentIds = null;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.GAME, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 1, null, 1, Integer.MAX_VALUE);
            parentIds = CommonUtil.getJsonParamString(jsonObject, "parentIds", null, null, 0, 100);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<SysMenu> sysMenuList = null;
        try {
            FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
            if(platform == 2 || platform == 3){ //2015-6-26 移动端暂开放全部栏目
                count = Integer.MAX_VALUE;
            }
            sysMenuList = firstPageHandler.getRecommendSysMenus(parentIds, appId, count);
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getKKRecommended ", e);
        }
        if (sysMenuList != null) {
            JsonArray cataList = new JsonArray();
            for (SysMenu sysMenu : sysMenuList) {
                JsonObject json = new JsonObject();

                if(sysMenu.getTitleName() != null) {
                    json.addProperty("cataName", sysMenu.getTitleName());
                }
                
                if(sysMenu.getTitleId() != null) {
                    json.addProperty("cataId", sysMenu.getTitleId());
                }
                
                if(sysMenu.getPosterPic() != null) {
                    json.addProperty("cataPic", sysMenu.getPosterPic());
                }
                
                if(sysMenu.getCoverPic()!= null) {  //返回封面图片, 配置ipad横屏展示需要的图片资源, cataPic和coverPic和一个图片的横竖两种
                    json.addProperty("coverPic", sysMenu.getCoverPic());
                }
                
                if(sysMenu.getPeopleInRoom() != null) {
                    json.addProperty("cataPeople", sysMenu.getPeopleInRoom());
                }
                cataList.add(json);
            }
            result.add("cataList", cataList);
            try {
                GameTagService gameTagService = MelotBeanFactory.getBean("gameTagService", GameTagService.class);
                Integer iRet = gameTagService.getLiveTagCount(appId);
                if (iRet != null) {
                    result.addProperty("livingCataCount", iRet);
                }
            } catch (Exception e) {
                logger.error("Fail to call gameTagService.getLiveTagCount", e);
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }
        
        return result;
    }
    
    /**
     * 移动端精选频道 (20010308)
     * 移动端原本与PC端一致,统一调用的getSubCataRoomList[cataId = 46]接口
     * 现移动端规则又变,配合移动端版本发行,编写新接口getMobileHandpick为API改造留下变动空间
     * @param jsonObject
     * @return
     */
    public JsonObject getMobileHandpick(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        
        JsonObject result = new JsonObject();
        final int followActorSize = 2;
        final int size = 6; 
        int platform = 0;
        Integer userId;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        
        try {
            RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
            List<RoomInfo> hotActors = RoomService.getTopActors(roomSubCatalogDao);
            List<RoomInfo>goalRoomlist = null ;
            JsonArray roomArray = new JsonArray();
            if(checkTag){ //登录用户
                goalRoomlist = getUserFollowRooms(userId, 1, followActorSize);
            }else{ //游客
                goalRoomlist = getSuggestActorsForVisitor(hotActors, size);
            }
            for(RoomInfo roomInfo : goalRoomlist) {
                if(roomInfo.getLiveEndtime() != null){//离线主播不作为推荐
                    continue;
                }
                JsonObject json = RoomTF.roomInfoToJson(roomInfo, platform);
                roomArray.add(json);
            }
            if(roomArray.size() < size){  //应为推荐了未开播等原因, 需要补足
                for (RoomInfo roomInfo  : hotActors) {
                    if(!goalRoomlist.contains(roomInfo)){
                        JsonObject json = RoomTF.roomInfoToJson(roomInfo, platform);
                        roomArray.add(json);
                    }
                    if(roomArray.size() == size){
                        break;
                    }
                }
            }
            PartService  partService  = MelotBeanFactory.getBean("partService", DefaultPartServiceImpl.class);
            result.addProperty("liveTotal", partService.getPartLiveCount(ConstantEnum.KKGAME_ALL_ACTORS_CATAID, null, null));
            result.addProperty("roomTotal", partService.getPartRoomCount(ConstantEnum.KKGAME_ALL_ACTORS_CATAID,null,null));
            result.add("roomList", roomArray);
        } catch(Exception e) {
            logger.error("Fail to call getMobileHandpick ", e);
        }
        
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);    
        return result;
    }
    
    /**
     * FuncTag: 20010309
     * 根据栏目id获取栏目下配置的在播房间(随机分配)
     * @param jsonObject
     * @return
     */
    public JsonObject getLiveRoomListByCataId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        
        JsonObject result = new JsonObject();
        int cataId = 0;
        int count = 0;
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, TagCodeEnum.CATAID_MISSING, 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<RoomInfo> roomList = null;
        try {
            RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
            roomList = roomSubCatalogDao.getPartLiveRoomList(cataId, Integer.valueOf(0), MAX_COUNT_OF_LIVING_ROOM);  
            if (roomList == null || roomList.size() < 2) { 
                //当前栏目下个数达不到响应个数, 从全站获取
                roomList = roomSubCatalogDao.getPartLiveRoomList(ConstantEnum.KKGAME_ALL_ACTORS_CATAID, 0, MAX_COUNT_OF_LIVING_ROOM);  
            }
        } catch(Exception e) {
            logger.error("Fail to call getLiveRoomListByCataId, "+ "cataId " + cataId, e);
        }
        JsonArray roomArray = new JsonArray(); 
        try{
            if (roomList != null) {
                List<RoomInfo>list = getRoomListByRandom(roomList, count);
                for (RoomInfo room : list) {
                    JsonObject json =  RoomTF.roomInfoToJson(room, PlatformEnum.WEB);
                    roomArray.add(json);
                }
            }
        }catch (Exception e) {
            logger.error("format from javabean to json error, "+ "cataId " + cataId, e);
        }
        
        result.add("roomList", roomArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     *  从一个列表内筛选出一个子集
     *  该子集的位置从全集内随机获取, 即全集是个排序的集合,结果集是全集的无须子集
     *  @param list: 进行筛选过滤的全集
     *  @param count: 需要随机返回的个数
     *  @return list的一个子集
     * 
     */
    private static List<RoomInfo> getRoomListByRandom(List<RoomInfo>list, int count){
        if(list == null || list.size() == 0){  //全集为空集, 则返回空列表, 不返回null;
            return new ArrayList<RoomInfo>();
        }
        int size = list.size();
        if(size <= count){ //当全集需要全部返回, 则随机后返回新列表
            List<RoomInfo>result = new ArrayList<RoomInfo>(list);
            Collections.shuffle(result);
            return result;
        }
        List<RoomInfo>result = new ArrayList<RoomInfo>();
        while(result.size() < count){
            RoomInfo room = list.get(RandomUtils.getRandomValue(-1, size));
            if(!result.contains(room)){
                result.add(room);
            }
        }
        return result;
    }
    
    /**
     * 移动端体育精选频道 (20010310)
     * 移动端原本与PC端一致,统一调用的getSubCataRoomList[cataId = 181]接口
     * 现移动端规则又变,配合移动端版本发行,编写新接口getMobileHandpick为API改造留下变动空间
     * @param jsonObject
     * @return
     */
    public JsonObject getMobileHandpickForSport(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        final int FOLLOW_RECOMMAND_COUNT = 2;
        final int size = 6; //返回总个数 
        
        int platform = 0;
        Integer userId;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        
        RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
        List<RoomInfo> hotActors =new ArrayList<RoomInfo>(RoomService.getTopActors(roomSubCatalogDao));
        Collections.shuffle(hotActors); //随机排列
        List<RoomInfo>goalRoomlist = null ;
        JsonArray roomArray = new JsonArray();
        if(checkTag){ //登录用户
            goalRoomlist =  getUserFollowRooms(userId, 1, FOLLOW_RECOMMAND_COUNT);
        }else{ //游客
            goalRoomlist = getSuggestActorsForVisitor(hotActors, size);
        }
        for(RoomInfo roomInfo : goalRoomlist) {
            if(roomInfo.getLiveEndtime() != null){//离线主播不作为推荐
                continue;
            }
          
            JsonObject json =   RoomTF.roomInfoToJson(roomInfo, platform);
            roomArray.add(json);
        }
        if(roomArray.size() < size){  //应为推荐了未开播等原因, 需要补足
            for (RoomInfo roomInfo : RoomService.getTopActors(roomSubCatalogDao)) {
                if(!goalRoomlist.contains(roomInfo)){
                    JsonObject json = RoomTF.roomInfoToJson(roomInfo, platform);
                    roomArray.add(json);
                }
                if(roomArray.size() == size){
                    break;
                }
            }
        }
        
        result.add("roomList", roomArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取最新开播的主播列表 (20010400)
     * @param jsonObject
     * @return
     * 
     */
    public JsonObject getLastLiveRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int start, offset, platform, gender;
        try {
        	start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 4, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            gender = CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, -1, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try{
            JsonArray jsonarray = new JsonArray();
            JsonParser parse = new JsonParser();
            Map<Integer, Long> lastestRooms = hallPartSource.getLaststrLiveRoomsInOrder(start, offset, gender);
            if (lastestRooms == null || lastestRooms.size() == 0) {
                result.add("lastestRoomList", new JsonArray());
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            
            RoomInfoService roomInfoService = (RoomInfoService)MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            Iterator<Map.Entry<Integer, Long>> iterators = lastestRooms.entrySet().iterator();
            while (iterators.hasNext()) {
                Map.Entry<Integer, Long> entryRoom = iterators.next();
                Integer roomId = entryRoom.getKey();
                Long liveTime = entryRoom.getValue();
                String jsonRoomInfo = hallPartSource.getLiveResRoomInfo(roomId);
                if (jsonRoomInfo == null || jsonRoomInfo == "") {
                    RoomInfo roomInfo =  roomInfoService.getRoomInfoById(roomId);
                    JsonObject tempObj = RoomTF.roomInfoToJson(roomInfo, platform);
                    tempObj.addProperty("lastestLiveTime", liveTime);
                    tempObj.addProperty("livestarttime", liveTime);  //bugfix: 在node 发布开播事件 与更新pg中间去获取房间信息造成的错误
                    hallPartSource.setLiveRoomInfo(roomId, tempObj.toString());
                    jsonarray.add(tempObj);
                } else {
                    JsonObject jsonObj = parse.parse(jsonRoomInfo).getAsJsonObject();
                    jsonarray.add(jsonObj);
                }
            }
            result.addProperty("liveRoomTotal", hallPartSource.getLiveTotalCount(gender));
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.add("lastestRoomList", jsonarray);
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error",e);
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        result.addProperty("currentTime", new Date().getTime());
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 附近的主播接口[20010401]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorNearby(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        //gender -1：全部；0：女；1：男；
        int cityId, gender;
        //appId -1:全部；1：唱响；2：直播；
        @SuppressWarnings("unused")
        int start, offset, appId, platform;
        String ip, cityName = null;
        try {
        	cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", -1, null, 0, Integer.MAX_VALUE);
        	gender = CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 4, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            ip = CommonUtil.getJsonParamString(jsonObject, "ip", CommonUtil.getIpAddr(request), null, 0, 20);
            cityName = CommonUtil.getJsonParamString(jsonObject, "cityName", null, null, 0, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            if (cityId < 1 && !StringUtil.strIsNull(cityName) && !"null".equals(cityName)) {
                String[] str = cityName.split("nbsp");
                if (str.length == 3){
                    String district = str[0];
                    String city = str[1];
                    String province = str[2];
                    String provinceId = hallPartSource.getProvinceId(province);
                    if (null == provinceId) {
                        Integer pId = GeneralService.getProvinceIdByProvinceName(province);
                        if (null == pId) {
                            cityId = 0;
                        } else {
                            hallPartSource.setProvince(pId, province);
                            provinceId = pId + "";                                
                        }
                    }
                    if (null != provinceId) {
                        String dname = String.format("%s_%s", provinceId, district);
                        String cname = String.format("%s_%s", provinceId, city);
                        String districtId = hallPartSource.getDistrictId(dname);
                        districtId = districtId == null ? hallPartSource.getDistrictId(cname) : districtId;
                        if (null == districtId) {
                            Map<String, Object> districtMap = GeneralService.getDistrictIdByDistrictName(district, city, Integer.parseInt(provinceId));
                            if (districtMap != null && districtMap.get("districId") != null) {
                                Integer dId = (Integer) districtMap.get("districId");
                                dname = String.format("%s_%s", provinceId, districtMap.get("districName")); 
                                hallPartSource.setDistrict(dId, dname);
                                cityId = dId;
                            } else {
                                cityId = 0;
                            }
                        } else {
                            cityId = Integer.parseInt(districtId);
                        }
                    } 
                } else {
                    cityId = 0;
                }
            }
        } catch (Exception e) {
            logger.error("Fail to analysis cityName: " + cityName, e);
            cityId = 0;
        }
        
        if (cityId < 1) {
			cityId = CityUtil.getCityIdByIpAddr(ip);
		}
        
        String cacheKey = String.format("con20010401.kkgame.getActorNearby.%s.%s.%s.%s", cityId, gender, start, offset);
        Object cacheObject = cache.getObject(cacheKey);
        if (cacheObject != null) {
            result = (JsonObject) cacheObject;
        } else {
            int total = roomSubCatalogService.getActorNearbyCount(cityId, gender, -1);
    		if (total > start) {
    			BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
            	List<RoomInfo> roomList = roomSubCatalogService.getActorNearby(cityId, gender, -1, start, offset);
            	List<Integer> actorList = new ArrayList<Integer>();
            	if (roomList != null && roomList.size() > 0) {
            	    for (RoomInfo roomInfo : roomList) {
                        actorList.add(roomInfo.getActorId());
                    }
            	}
            	Map<Integer, Boolean> isBlack = blacklistService.isSameCityBlacklist(actorList);
    	        JsonArray jsonArray = new JsonArray();
    	        if (roomList != null && roomList.size() > 0) {
    	        	for(RoomInfo room : roomList){
    	        		if (isBlack.containsKey(room.getActorId()) && isBlack.get(room.getActorId()) == false) {
    	        			jsonArray.add(RoomTF.roomInfoToJson(room, platform));
    	        		}
    	        	}
    	        	result.add("actorNearby", jsonArray);
    	        }
            }
    
            result.addProperty("cityId", cityId);
            result.addProperty("total", total);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            
            cache.insertObject(cacheKey, result);
        }
        return result;
    }
    
    /**
     *  获取用户关注主播房间 
     */
    @SuppressWarnings("unchecked")
    private List<RoomInfo> getUserFollowRooms(int userId,int pageIndex, int followNum){
        int followsCount = UserRelationService.getFollowsCount(userId);
        if(followsCount == 0 ){
            return new ArrayList<RoomInfo>();
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("userId", userId);
        map.put("pageIndex", pageIndex);
        map.put("countPerPage", followNum);
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.getUserFollowedList", map);
        } catch (SQLException e) {
            logger.error("未能正常调用存储过程", e);
        }
        if (TagCodeEnum.SUCCESS.equals(map.get("TagCode"))) {
            List<Room> roomList = (ArrayList<Room>) map.get("roomList");
            if (roomList != null && roomList.size() > 0) {
                StringBuffer actorRoomIds = new StringBuffer();
                for (Room room : roomList) {
                    if (room.getActorTag() != null && room.getActorTag().intValue() == 1) {
                        actorRoomIds.append(room.getUserId()).append(",");
                    }
                }
                if (actorRoomIds.length() > 0) {
                  return  RoomService.getRoomListByRoomIds(actorRoomIds.toString());
                }
            }
        }
        return new ArrayList<RoomInfo>();
    } 
    
    /**
     * 获取游客推荐精选
     * @param offset
     * @param hotActors
     */
    private List<RoomInfo> getSuggestActorsForVisitor(List<RoomInfo> hotActors, int size) {
        List<RoomInfo>goalRoomlist = new ArrayList<RoomInfo>();
        Iterator<RoomInfo>it =hotActors.iterator();
        while(it.hasNext()){
            RoomInfo roomInfo = it.next();
            goalRoomlist.add(roomInfo);
            if(goalRoomlist.size() == size){
                break; //满足条数
            }
        }
        return goalRoomlist;
    }
    
    /**
     *  ipad获取栏目及子栏目,需要返回栏目下的房间数 
     * 
     */
    @SuppressWarnings("unused")
    private List<SysMenu> fetchSysMenuList(Integer appId){
        SysMenuDao sysMenuDao = MelotBeanFactory.getBean("sysMenuDao", SysMenuDao.class);
        List<SysMenu> sysMenus = sysMenuDao.getSysMenusByParentId(0, appId);
        for (SysMenu sysMenu : sysMenus) {
            List<SysMenu> subSysMenuList = sysMenuDao.getSubSysMenusByParentId(sysMenu.getTitleId(),1,null);
            Collections.sort(subSysMenuList, new Comparator<SysMenu>(){
                public int compare(SysMenu arg0, SysMenu arg1) {
                    return arg0.getSortIndex().compareTo(arg1.getSortIndex());
                }
            });
            sysMenu.setSysMenus(subSysMenuList);
        }
        return sysMenus;
    }
    
}
