package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.SysMenuDao;
import com.melot.api.menu.sdk.dao.domain.HomePage;
import com.melot.api.menu.sdk.dao.domain.HotRoomInfo;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.api.menu.sdk.service.HomeService;
import com.melot.content.config.domain.GameTagConfig;
import com.melot.content.config.game.service.GameTagService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.SystemConfig;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 新版大厅
 * @author chenjian
 *
 */
public class HallFunctions {

    @Autowired
    private ConfigService configService;
	
	private static Logger logger = Logger.getLogger(HallFunctions.class);
	
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
		    FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
		    if (appId == 3) { // 金海岸首页不做房间过滤
		        tempList = firstPageHandler.getFistPagelist(appId, channel, platform, false);
		    } else {
		        tempList = firstPageHandler.getFistPagelist(appId, channel, platform);
		    }
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
		}
		
        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for (HomePage temp : tempList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", temp.getSeatId());
                json.addProperty("type", temp.getSeatType());
                if (temp.getTitleName() != null) {
                    json.addProperty("title", temp.getTitleName());
                }
                json.addProperty("cdnState", temp.getCdnState());
                json.addProperty("icon", ConstantEnum.FUN_ICON_HOME + temp.getDetailId()+".png");
                if (temp.getSubTitle() != null) {
                    json.addProperty("subTitle", temp.getSubTitle());
                }
                if (temp.getRoomTotal() != null) {
                    json.addProperty("roomTotal", temp.getRoomTotal());
                }
                if (temp.getLiveTotal() != null) {
                    json.addProperty("liveTotal", temp.getLiveTotal());
                }
                if (temp.getDetailId() != null) {
                    // 专区编号
                    json.addProperty("id", temp.getDetailId());
                }
                if (temp.getCdnState() != null) {
                    if (temp.getCdnState() > 0 && temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<RoomInfo> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (RoomInfo roomInfo : roomList) {
                                roomArray.add(RoomTF.roomInfoToJson(roomInfo,
                                        platform));
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
	 * 获取子目录直播间列表(20010302)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getSubCataRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		//二级目录编号
		int cataId = 0;
		int start = 0;
		int offset = 0;
		int platform = 0;
		Integer userId = null;
		int cityId = 0;
		int appId, channel;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
			cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, null, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_MISSING, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, TagCodeEnum.OFFSET_MISSING, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", 0, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		// 若cataId为0，默认取指定appId和channel下第一个栏目的内容
		int isdownload = 1;
		if (cataId == 0 && appId > 0 && channel > 0) {
			try {
			    FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
			    List<HomePage> tempList = firstPageHandler.getFistPagelist(appId, channel, platform, false);
			    if (tempList != null && tempList.size() > 0) {
			    	HomePage page = tempList.get(0);
			    	cataId = page.getDetailId();
			    	// 11: H5栏目大厅（无下载图标）
			    	if (page.getSeatType() == 11) {
			    		isdownload = 0;
			    	}
			    }
			} catch(Exception e) {
				logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
			}
		}
		result.addProperty("isdownload", isdownload);
		
        // 周边达人
        if (cataId == 42 && cityId == 0) {
        	String cityIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
    		if (cityIp != null && cityIp.indexOf(",") > 0) {
    			cityIp = cityIp.substring(cityIp.indexOf(",") + 1, cityIp.length());
    		}
        	Map<String, Integer> citMap = GeneralService.getIpCity(cityIp);
        	if (citMap != null && citMap.get("area") != null && citMap.get("city") != null) {
        		cityId = citMap.get("city").intValue();
        	}
        }
		
		SysMenu sysMenu = null;
        
		try {
		    FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
		    if (cataId == 42) {
		        sysMenu = firstPageHandler.getPartListForRim(appId, cataId, cityId, start, offset);
		    } else {
		        sysMenu = firstPageHandler.getPartList(cataId, userId, cityId, start, offset);
		    }
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getPartList, "
					+ "cataId " + cataId + " userId " + ((userId == null)?" ":userId) + " cityId " + cityId, e);
		}
		
		if (sysMenu != null) {
			if(sysMenu.getTitleName() != null) {
				result.addProperty("titleName", sysMenu.getTitleName());
			}
			String subTitle = sysMenu.getSubTitle();
			int roomTotal = 0;
			if (sysMenu.getLiveTotal() != null) {
				result.addProperty("liveTotal", sysMenu.getLiveTotal());
			}
			if (sysMenu.getRoomCount() != null) {
				roomTotal = sysMenu.getRoomCount().intValue();
			} else {
				roomTotal = 0;
			}
			
			//周边达人要造假
			if(cataId == 42 && roomTotal == 100) {
				int fakeCount = roomTotal + new Random().nextInt(20);
				if(subTitle != null) {
					subTitle = subTitle.replaceFirst(String.valueOf(roomTotal), String.valueOf(fakeCount));
				}
				roomTotal = fakeCount;
			}
			if(subTitle != null) {
				result.addProperty("subTitle", subTitle);
			}
			result.addProperty("roomTotal", roomTotal);
			
			JsonArray roomArray = new JsonArray();
			List<RoomInfo> roomList = sysMenu.getRooms();
			if (roomList != null) {
				for (RoomInfo roomInfo : roomList) {
					roomArray.add(RoomTF.roomInfoToJson(roomInfo, platform));
				}
			}
			
			result.add("roomList", roomArray);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		
		return result;
	}
	
	/**
	 * 获取同级子目录(20010303)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getSubCataList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int appId = AppIdEnum.AMUSEMENT;
		int limit = 0;
		int cataId = 0;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			limit = CommonUtil.getJsonParamInt(jsonObject, "limit", 0, null, 1, Integer.MAX_VALUE);
			cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, TagCodeEnum.CATAID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		SysMenu sysMenu = null;

		try {
			FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
			if (appId == AppIdEnum.AMUSEMENT) {
			    sysMenu = firstPageHandler.getSysMenuList(cataId);
			} else {
			    if (limit == 0) {
			        sysMenu = firstPageHandler.getSysMenuList(cataId, appId, null);
			    }else{
			        sysMenu = firstPageHandler.getSysMenuList(cataId, appId, limit);
			    }
			}
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getCataList ", e);
		}
		
		if (sysMenu != null) {
			result.addProperty("parentCataId", sysMenu.getTitleId());
			result.addProperty("parentCataName", sysMenu.getTitleName());
			
			JsonArray cataList = new JsonArray();
			if (sysMenu.getSysMenus() != null) {
				for(SysMenu temp : sysMenu.getSysMenus()) {
					JsonObject json = new JsonObject();
					json.addProperty("cataId", temp.getTitleId());
					json.addProperty("cataName", temp.getTitleName());
					json.addProperty("cdnState", temp.getCdnState());
					cataList.add(json);
				}
			}
			
			result.add("cataList", cataList);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		
		return result;
	}
	
	/**
	 * 获得kk游戏推荐栏目(20010304)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getKKRecommended(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int appId, count, platform;
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
            if(platform == 2 || platform == 3){ //2015-6-26 移动端暂开放全部栏目
                count = Integer.MAX_VALUE;
            }
		    
			FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
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
	 * 获得所有栏目及其子栏目列表(20010305)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getAllCataList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int appId, platform;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject,"platform", 1, null, 1, Integer.MAX_VALUE);
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
			if(platform != PlatformEnum.IPAD){
                sysMenuList = firstPageHandler.getAllSysMenuList(appId);
            }else{ //pad版本需要返回栏目下房间人数
                sysMenuList = fetchSysMenuList(appId);
            }
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getAllCataList ", e);
		}
		
		if (sysMenuList != null) {
			JsonArray totalList = new JsonArray();
			for(SysMenu sysMenu : sysMenuList) {
				JsonObject tempJson = new JsonObject();
				tempJson.addProperty("parentCataId", sysMenu.getTitleId());
				tempJson.addProperty("parentCataName", sysMenu.getTitleName());
                tempJson.addProperty("parentIconW", ConstantEnum.FUN_ICON_WHITE+sysMenu.getTitleId()+".png");
                tempJson.addProperty("parentIconB", ConstantEnum.FUN_ICON_BLACK+sysMenu.getTitleId()+".png");
                if(sysMenu.getSubTitle() != null){
                    tempJson.addProperty("alianceCataName", sysMenu.getSubTitle());
                }
				JsonArray sonList = new JsonArray();
				for (SysMenu temp : sysMenu.getSysMenus()) {
					JsonObject json = new JsonObject();
					json.addProperty("cataId", temp.getTitleId());
					json.addProperty("cataName", temp.getTitleName());
					json.addProperty("cdnState", temp.getCdnState());
                    if(temp.getPosterPic() != null) {
                        json.addProperty("cataPic", temp.getPosterPic());
                    }
                    if(temp.getCoverPic()!= null) {  //返回封面图片, 配置ipad横屏展示需要的图片资源, cataPic和coverPic和一个图片的横竖两种
                        json.addProperty("coverPic", temp.getCoverPic());
                    }
                    if(temp.getIcon() != null){
                        json.addProperty("icon", temp.getIcon());
                    }
                    if(temp.getWebIcon() != null){
                        json.addProperty("webIcon", temp.getWebIcon());
                    }
                    if(temp.getPeopleInRoom() != null) {
                        json.addProperty("cataPeople", temp.getPeopleInRoom());
                    }else {
                        json.addProperty("cataPeople", 0);
                    }
					sonList.add(json);
				}
				tempJson.add("cataList", sonList);
				totalList.add(tempJson);
			}
			
			result.add("totalList", totalList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		
		return result;
	}
    
    
    /**
     *  ipad获取栏目及子栏目,需要返回栏目下的房间数 
     * 
     */
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
	
	/**
	 * 查询配置的直播游戏类别(20010306)
	 * @return
	 */
	public JsonObject getAllTagConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
	    
		List<GameTagConfig> list = null;
		
		try {
			GameTagService gameTagService = MelotBeanFactory.getBean("gameTagService", GameTagService.class);
			list = gameTagService.getAllGameTagConfigsForApi();
		} catch (Exception e) {
			logger.error("Fail to call gameTagService.getAllGameTagConfigsForApi ", e);
		}
		
		if (list != null) {
			JsonArray roomTagList = new JsonArray();
			for (GameTagConfig gameTagConfig : list) {
				JsonObject json = new JsonObject();
				json.addProperty("tagName", gameTagConfig.getTagName());
				if (gameTagConfig.getTagAliasName() != null) {
					json.addProperty("tagAliasName ", gameTagConfig.getTagAliasName());
				}
				roomTagList.add(json);
			}
			
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("roomTagList", roomTagList);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		
		return result;
	}
	
	/**
	 * 更新主播游戏标签(20010307)
	 * @return
	 */
	public JsonObject setRoomGameTag(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int actorId = 0;
		String roomTag = null;
		try {
			actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTORID_MISSING, 1, Integer.MAX_VALUE);
			roomTag = CommonUtil.getJsonParamString(jsonObject, "roomTag", null, TagCodeEnum.GAME_TAG_MISSING, 0, 20);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		RoomInfo roomInfo = new RoomInfo();
		roomInfo.setActorId(actorId);
		roomInfo.setRoomTag(roomTag);
		if (RoomService.updateRoomInfo(roomInfo)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_UPDATE_GAME_TAG);
		}
		
		return result;
	}
	
	/**
	 * 获取看过的房间列表(10002033)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject getViewedRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int platform, appId;
		String userIds = null;
		List<Integer> userIdList = new ArrayList<Integer>(); // 用户编号列表
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			userIds = CommonUtil.getJsonParamString(jsonObject, "userIds", null, "02330001", 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		if (userIds != null) {
			try {
				String[] strArr = userIds.split(",");
				// 最大查询数限制
				int count = 20;
				String value = SystemConfig.getValue(SystemConfig.maxViewedQueryCount, appId);
				if (value != null) count = Integer.parseInt(value);
				if (strArr.length > count) {
					result.addProperty("TagCode", "02330003");
					return result;
				}
				for (String str : strArr)
					userIdList.add(Integer.parseInt(str));
			} catch (Exception e) {
				result.addProperty("TagCode", "02330002");
				return result;
			}
		}
		
		JsonArray roomArray = new JsonArray();
		List<RoomInfo> roomList = RoomService.getRoomListByRoomIds(userIds);
		if (roomList != null && roomList.size() > 0) {
			for (RoomInfo roomInfo : roomList) {
				roomArray.add(RoomTF.roomInfoToJsonTemp(roomInfo, platform));
			}
		}
		result.add("roomList", roomArray);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
    /**
     * 新版大厅显示板块(50001003)
     * @param jsonObject
     * @return
     */
    public JsonObject getV2HallPlateList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        int appId;
        int channel;
        try {
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
            FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
            tempList = firstPageHandler.getFistPagelist(appId, channel, platform, false);
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for (HomePage temp : tempList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", temp.getSeatId());
                json.addProperty("type", temp.getSeatType());
                if (temp.getTitleName() != null) {
                    json.addProperty("title", temp.getTitleName());
                }
                json.addProperty("cdnState", temp.getCdnState());
                json.addProperty("icon", ConstantEnum.FUN_ICON_HOME + temp.getDetailId()+".png");
                json.addProperty("cataIcon", temp.getIcon());
                json.addProperty("webIcon", temp.getWebIcon());
                if (temp.getSubTitle() != null) {
                    json.addProperty("subTitle", temp.getSubTitle());
                }
                if (temp.getRoomTotal() != null) {
                    json.addProperty("roomTotal", temp.getRoomTotal());
                }
                if (temp.getLiveTotal() != null) {
                    json.addProperty("liveTotal", temp.getLiveTotal());
                }
                if (temp.getDetailId() != null) {
                    // 专区编号
                    json.addProperty("id", temp.getDetailId());
                }
                if (temp.getCdnState() != null) {
                    if (temp.getCdnState() > 0 && temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<RoomInfo> roomList = temp.getRooms();
//                        // 3金海岸首页不做房间过滤
//                        if (appId != 3) {
//                            roomList = filterUnLiveRoom(roomList, temp.getLiveTotal(), temp.getHomeShowRooms());
//                        }
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
     * 首页栏目下未直播房间过滤
     * @param rooms: 过滤筛选房间列表
     * @param seatCount: 类型栏目显示个数
     * @param liveTotal:该栏目下统计在播房间数目
     * @return:
     *     1.1个直播房间,返回空list,前端需要保留栏目显示
     *     2.0个直播房间,返回null
     */
    public static List<RoomInfo> filterUnLiveRoom(List<RoomInfo> rooms, int liveTotal, int seatCount) {
        if (liveTotal == 0 || rooms == null || rooms.size() == 0 ) {
            return null;
        }
        int liveCount = 0;
        Iterator<RoomInfo> it = rooms.iterator();
        while (it.hasNext()) { //队列本身已经根据开播状态做好了排序 
            if (it.next().isOnLive()) {  
                liveCount++;
            } else {
                break; 
            }
        }  
        
        if (liveCount == 0) { //栏目下房间都是未开播状态
            return null;
        }
        
        try {
            //获取栏目下在播用户
            return rooms.subList(0, liveCount);
        } catch (Exception e) {
            logger.error("filterUnLiveRoom error happend-------------", e);
            return rooms;
        }
    }
    
    /**
     * 获取广场栏目(50001004)
     * @param jsonObject
     * @return
     */
    public JsonObject getSquareTitleList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId;
        int channel;
        int city = 0;
        int platform = 0;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
            city = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<HomePage> titleList = null;
        
        try {
            if (configService.getIsAbroad()) {
                // 根据city获取默认渠道ID
                if (city == 0) {
                    String clientIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
                    city = CityUtil.getCityIdByIpAddr(clientIp);
                 }
                 int defaultChannel = CityUtil.getCityDefaultChannel(city);
                 HomeService homeService = MelotBeanFactory.getBean("homeService", HomeService.class);
                 titleList = homeService.getPartListInHome(appId, channel, defaultChannel);
            } else {
                FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
                titleList = firstPageHandler.getSquareTitleList(appId, channel);
            }
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        if (titleList != null) {
            JsonArray plateList = new JsonArray();
            for (HomePage hp : titleList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", hp.getSeatId());
                if (hp.getTitleName() != null) {
                    json.addProperty("titleName", hp.getTitleName());
                }
                if (hp.getDetailId() != null) {
                    // 栏目编号
                    json.addProperty("titleId", hp.getDetailId());
                }
                if (hp.getIcon() != null) {
                    // 栏目图标
                    json.addProperty("icon", hp.getIcon());
                }
                if (hp.getWebIcon() != null) {
                    // 栏目图标
                    json.addProperty("webIcon", hp.getWebIcon());
                }
                if (hp.getCdnState() != null) {
                    // 栏目是否做CND缓存
                    json.addProperty("cdnState", hp.getCdnState());
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
     * 获取用户热度值(50001018)
     * @param jsonObject
     * @return
     */
    public JsonObject getHotRoomInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        HotRoomInfo hotRoomInfo = null;
        
        try {
            FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
            hotRoomInfo = firstPageHandler.getHotRoomInfo(roomId);
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        if (hotRoomInfo != null && hotRoomInfo.getScore() > 0) {
            result.addProperty("roomId", hotRoomInfo.getRoomId());
            result.addProperty("score", (long) Math.ceil(hotRoomInfo.getScore()));
            result.addProperty("position", hotRoomInfo.getPosition() + 1);
            if (hotRoomInfo.getPreRoomId() != 0) {
                result.addProperty("preRoomId", hotRoomInfo.getPreRoomId());
                result.addProperty("preScore", (long) Math.ceil(hotRoomInfo.getPreScore()));
                result.addProperty("prePosition", hotRoomInfo.getPrePosition() + 1);
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }
        
        return result;
    }
    
}
