package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.content.config.domain.GameTagConfig;
import com.melot.content.config.game.service.GameTagService;
import com.melot.kk.hall.api.constant.QueryHallRoomInfoParam;
import com.melot.kk.hall.api.domain.FirstPageConfDTO;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.domain.HotRoomInfoDTO;
import com.melot.kk.hall.api.service.HallRoomService;
import com.melot.kk.hall.api.service.HomeService;
import com.melot.kk.hall.api.service.SysMenuService;
import com.melot.kkcx.functions.KKHallFunctions;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
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

    @Resource
	private HallRoomService hallRoomService;

    @Resource
	private SysMenuService hallPartService;

	@Resource
	private HomeService hallHomeService;

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
		
		List<FirstPageConfDTO> tempList = null;
		
		try {
		    if (appId == 3) { // 金海岸首页不做房间过滤
		        Result<List<FirstPageConfDTO>> tempListResult = hallHomeService.getFistPagelist(appId, channel, platform, 0, 0, 0, false, 0, false);
		        if (tempListResult != null && CommonStateCode.SUCCESS.equals(tempListResult.getCode())) {
		            tempList = tempListResult.getData();
		                    
                }
		    } else {
		        Result<List<FirstPageConfDTO>> tempListResult = hallHomeService.getFistPagelist(appId, channel, platform, 0, 0, 0, true, 0, false);
		        if (tempListResult != null && CommonStateCode.SUCCESS.equals(tempListResult.getCode())) {
                    tempList = tempListResult.getData();
                            
                }
		    }
		} catch(Exception e) {
			logger.error("Fail to call hallHomeService.getFistPagelist ", e);
		}
		
        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for (FirstPageConfDTO temp : tempList) {
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
                        List<HallRoomInfoDTO> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (HallRoomInfoDTO roomInfo : roomList) {
                                roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
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
		int area = 1;
        try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
			cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, null, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_MISSING, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, TagCodeEnum.OFFSET_MISSING, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", 0, null, 1, Integer.MAX_VALUE);
			area = CommonUtil.getJsonParamInt(jsonObject, "area", 0, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
        
        String cityIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
        Integer cityIdByIpAddr;
        if (cityId < 1 || area < 1) {
            cityIdByIpAddr = CityUtil.getProvincialCapital(CityUtil.getCityIdByIpAddr(cityIp));
            if (cityId < 1) {
                cityId = cityIdByIpAddr;
            }
            if (area < 1) {
                cityId = cityIdByIpAddr;
                area = CityUtil.getParentCityId(cityId);
            }
        }
		// 若cataId为0，默认取指定appId和channel下第一个栏目的内容
		int isdownload = 1;
		if (cataId == 0 && appId > 0 && channel > 0) {
			try {
			    Result<List<FirstPageConfDTO>> tempListResult = hallHomeService.getFistPagelist(appId, channel, platform, 0, 0, 0, false, 0, false);
			    if (tempListResult != null && CommonStateCode.SUCCESS.equals(tempListResult.getCode())
			            && CollectionUtils.isNotEmpty(tempListResult.getData())) {
			        List<FirstPageConfDTO> tempList = tempListResult.getData();
			        FirstPageConfDTO page = tempList.get(0);
                    cataId = page.getDetailId();
                    // 11: H5栏目大厅（无下载图标）
                    if (page.getSeatType().equals(11)) {
                        isdownload = 0;
                    }
                }
			} catch(Exception e) {
				logger.error("Fail to call hallHomeService.getFistPagelist ", e);
			}
		}
		result.addProperty("isdownload", isdownload);
		
        // 周边达人
        if (cataId == 42 && cityId == 0) {
    		if (cityIp != null && cityIp.indexOf(',') > 0) {
    			cityIp = cityIp.substring(cityIp.indexOf(',') + 1, cityIp.length());
    		}
        	Map<String, Integer> citMap = GeneralService.getIpCity(cityIp);
        	if (citMap != null && citMap.get("area") != null && citMap.get("city") != null) {
        		cityId = citMap.get("city").intValue();
        	}
        }
		
        HallPartConfDTO sysMenu = null;
        int filterCount = 0;
        Set<Integer> filterIdList = new HashSet<>();
        
		try {
		    if (cataId == 42) {
		        Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartListForRim(appId, cataId, cityId, start, offset);
		        if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                    sysMenu = sysMenuResult.getData();
                }
		    } else {
		        //官方推荐栏目需过滤个性推荐相关数据
		        if (cataId == 1551 || cataId == 1556) {
		            KKHallFunctions kkHallFunctions = (KKHallFunctions) MelotBeanFactory.getBean("kkHallFunction");
		            Page<HallRoomInfoDTO> resp = kkHallFunctions.getRecommendedList(appId, userId, cityIp, 0, 10);
		            if (!Collectionutils.isEmpty(resp.getList())) {
		                List<HallRoomInfoDTO> hallRoomInfoDTOList = resp.getList();
		                filterCount = hallRoomInfoDTOList.size();
		                for (HallRoomInfoDTO hallRoomInfoDTO : hallRoomInfoDTOList) {
		                    filterIdList.add(hallRoomInfoDTO.getRoomId());
		                }
		            }
		        }
		        
		        Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(cataId, userId, cityId, area, appId, start, offset + filterCount);
                if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                    sysMenu = sysMenuResult.getData();
                }
		    }
		} catch(Exception e) {
			logger.error("Fail to call hallPartService.getPartList, "
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
			JsonObject json;
			List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
			HallRoomInfoDTO roomInfo;
			if (roomList != null) {
				int i = 0;
				if (sysMenu.getDataSourceType() != null 
				        && (sysMenu.getDataSourceType() == 16
				        || sysMenu.getDataSourceType() == 17)) {
					// 如果是聚合栏目，需要判断同城，添加距离
					int dist;
					while (i < roomList.size()) {
						roomInfo = roomList.get(i);
						json = HallRoomTF.roomInfoWithPlaybackToJson(roomInfo, platform,appId);
						if (!Objects.equals(cityId, Math.abs(roomList.get(i).getRegisterCity()))) {
							break;
						}
						dist = configService.getNearbyStartDistance() + (start + i) * configService.getNearbyDistanceBeforeInterval();
						if (dist < configService.getNearbyMiddleDistance()) {
							dist += RandomUtils.nextInt(configService.getNearbyDistanceBeforeInterval());
						} else {
							dist += RandomUtils.nextInt(configService.getNearbyDistanceAfterInterval());
						}
						// 随机添加距离
						json.addProperty("distance", dist);
						i++;
						roomArray.add(json);
					}
				}
				while (i < roomList.size()) {
					roomInfo = roomList.get(i++);
					
					//官方推荐栏目需过滤个性推荐相关数据
                    if (cataId == 1551 || cataId == 1556) {
                        if (roomArray.size() >= offset) {
                            break;
                        } else if (filterIdList.contains(roomInfo.getRoomId())){
                            continue;
                        }
                    }
                    
                    if (cataId == 1598) {
                        roomInfo.setRecommendAttribute(12);
                    }
					json = HallRoomTF.roomInfoWithPlaybackToJson(roomInfo, platform,appId);
					if (sysMenu.getDataSourceType() != null 
					        && (sysMenu.getDataSourceType() == 16
					        || sysMenu.getDataSourceType() == 17)) {
						json.addProperty("distance", 0);
					}
					roomArray.add(json);
				}
			}
			
			result.add("roomList", roomArray);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			
			result.addProperty("cityId", cityId);
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
		
		int appId;
		int limit;
		int cataId;
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

		Result<HallPartConfDTO> hallPartConfDTOResult = null;
		HallPartConfDTO hallPartConfDTO = null;

		try {
			if (appId == AppIdEnum.AMUSEMENT) {
				hallPartConfDTOResult = hallPartService.getSubSysMenuList(cataId);
			} else {
			    if (limit <= 0) {
					hallPartConfDTOResult = hallPartService.getSysMenuList(cataId, appId, Integer.MAX_VALUE);
			    } else {
					hallPartConfDTOResult = hallPartService.getSysMenuList(cataId, appId, limit);
			    }
			}
		} catch(Exception e) {
			logger.error("Fail to call hallPartService.getCataList ", e);
		}
		if (hallPartConfDTOResult != null) {
			hallPartConfDTO = hallPartConfDTOResult.getData();
		}
		if (hallPartConfDTO != null) {
			result.addProperty("parentCataId", hallPartConfDTO.getTitleId());
			result.addProperty("parentCataName", hallPartConfDTO.getTitleName());
			
			JsonArray cataList = new JsonArray();
			if (CollectionUtils.isNotEmpty(hallPartConfDTO.getSysMenus())) {
				for(HallPartConfDTO temp : hallPartConfDTO.getSysMenus()) {
					JsonObject json = new JsonObject();
					json.addProperty("cataId", temp.getTitleId());
					json.addProperty("cataName", temp.getTitleName());
					json.addProperty("cdnState", temp.getCdnState());
					if (!StringUtil.strIsNull(temp.getIcon())) {
					    json.addProperty("icon", temp.getIcon());
                    }
					if (!StringUtil.strIsNull(temp.getWebIcon())) {
                        json.addProperty("webIcon", temp.getWebIcon());
                    }
					cataList.add(json);
				}
			}
			result.add("cataList", cataList);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
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
		String parentIds;
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
		

		try {
			//2015-6-26 移动端暂开放全部栏目
			if(platform == 2 || platform == 3){
				count = Integer.MAX_VALUE;
			}
			Result<List<HallPartConfDTO>> hallPartResult = hallPartService.getRecommendSysMenus(parentIds, appId, count);
			if (hallPartResult == null || !CommonStateCode.SUCCESS.equals(hallPartResult.getCode())) {
				result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
				return result;
			}
			List<HallPartConfDTO> hallPartConfDTOList = hallPartResult.getData();
			JsonArray cataList = new JsonArray();
			for (HallPartConfDTO hallPartConfDTO : hallPartConfDTOList) {
				JsonObject json = new JsonObject();
				if(hallPartConfDTO.getTitleName() != null) {
					json.addProperty("cataName", hallPartConfDTO.getTitleName());
				}
				if(hallPartConfDTO.getTitleId() != null) {
					json.addProperty("cataId", hallPartConfDTO.getTitleId());
				}
				if(hallPartConfDTO.getPosterPic() != null) {
					json.addProperty("cataPic", hallPartConfDTO.getPosterPic());
				}
				//返回封面图片, 配置ipad横屏展示需要的图片资源, cataPic和coverPic和一个图片的横竖两种
				if(hallPartConfDTO.getCoverPic()!= null) {
					json.addProperty("coverPic", hallPartConfDTO.getCoverPic());
				}
				if(hallPartConfDTO.getPeopleInRoom() != null) {
					json.addProperty("cataPeople", hallPartConfDTO.getPeopleInRoom());
				}
				cataList.add(json);
			}
			result.add("cataList", cataList);
		} catch (Exception e) {
			logger.error(String.format("Module Error：sysMenuService.getRecommendSysMenus(parentIds=%s, appId=%s, count=%s);",
					parentIds, appId, count), e);
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
			return result;
		}

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
		
		List<HallPartConfDTO> hallPartConfDTOList = null;
		
		try {
			Result<List<HallPartConfDTO>> hallPartResult;
			if(platform != PlatformEnum.IPAD) {
				hallPartResult = hallPartService.getAllSysMenuList(appId);
            } else { //pad版本需要返回栏目下房间人数
				hallPartResult = hallPartService.fetchSysMenuList(appId);
            }
			if (hallPartResult == null || !CommonStateCode.SUCCESS.equals(hallPartResult.getCode())) {
				result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
				return result;
			}
			hallPartConfDTOList = hallPartResult.getData();
		} catch(Exception e) {
			logger.error("Fail to call sysMenuService.getAllCataList ", e);
		}
		
		if (hallPartConfDTOList != null) {
			JsonArray totalList = new JsonArray();
			for(HallPartConfDTO hallPartConfDTO : hallPartConfDTOList) {
				JsonObject tempJson = new JsonObject();
				tempJson.addProperty("parentCataId", hallPartConfDTO.getTitleId());
				tempJson.addProperty("parentCataName", hallPartConfDTO.getTitleName());
                tempJson.addProperty("parentIconW", ConstantEnum.FUN_ICON_WHITE+hallPartConfDTO.getTitleId()+".png");
                tempJson.addProperty("parentIconB", ConstantEnum.FUN_ICON_BLACK+hallPartConfDTO.getTitleId()+".png");
                if(hallPartConfDTO.getSubTitle() != null){
                    tempJson.addProperty("alianceCataName", hallPartConfDTO.getSubTitle());
                }
				JsonArray sonList = new JsonArray();
                if (hallPartConfDTO.getSysMenus() != null) {
					for (HallPartConfDTO temp : hallPartConfDTO.getSysMenus()) {
						JsonObject json = new JsonObject();
						json.addProperty("cataId", temp.getTitleId());
						json.addProperty("cataName", temp.getTitleName());
						json.addProperty("cdnState", temp.getCdnState());
						if(temp.getPosterPic() != null) {
							json.addProperty("cataPic", temp.getPosterPic());
						}
						//返回封面图片, 配置ipad横屏展示需要的图片资源, cataPic和coverPic和一个图片的横竖两种
						if(temp.getCoverPic()!= null) {
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
				if (value != null) {
					count = Integer.parseInt(value);
				}
				if (strArr.length > count) {
					result.addProperty("TagCode", "02330003");
					return result;
				}
				for (String str : strArr) {
					userIdList.add(Integer.parseInt(str));
				}
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
        int area;
        int cityId = 0;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
            area = CommonUtil.getJsonParamInt(jsonObject, "area", 1, null, 0, Integer.MAX_VALUE);
            cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", 0, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<FirstPageConfDTO> tempList = null;
        
        Integer cityIdByIpAddr;
        if (cityId < 1 || area < 1) {
            cityIdByIpAddr = CityUtil.getProvincialCapital(CityUtil.getCityIdByIpAddr(com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null)));
            if (cityId < 1) {
                cityId = cityIdByIpAddr;
            }
            if (area < 1) {
                cityId = cityIdByIpAddr;
                area = CityUtil.getParentCityId(cityId);
            }
        }
        
        try {
            
            Result<List<FirstPageConfDTO>> tempListResult = hallHomeService.getFistPagelist(appId, channel, platform, 0, cityId, area, false, 0, false);
            if (tempListResult != null && CommonStateCode.SUCCESS.equals(tempListResult.getCode())) {
                tempList = tempListResult.getData();
            }
            
        } catch(Exception e) {
            logger.error("Fail to call hallHomeService.getFistPagelist ", e);
        }
        
        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for (FirstPageConfDTO temp : tempList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", temp.getSeatId());
                json.addProperty("type", temp.getSeatType());
                if (temp.getTitleName() != null) {
                    json.addProperty("title", temp.getTitleName());
                }
                json.addProperty("cdnState", temp.getCdnState());
                json.addProperty("icon", ConstantEnum.FUN_ICON_HOME + temp.getDetailId()+".png");
                if (!StringUtil.strIsNull(temp.getIcon())) {
                    json.addProperty("cataIcon", temp.getIcon());
                }
                
                if (!StringUtil.strIsNull(temp.getWebIcon())) {
                    json.addProperty("webIcon", temp.getWebIcon());
                }
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
                    if (temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<HallRoomInfoDTO> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (HallRoomInfoDTO roomInfo : roomList) {
                                roomArray.add(HallRoomTF.roomInfoWithPlaybackToJson(roomInfo, platform,appId));
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
		// 队列本身已经根据开播状态做好了排序
        while (it.hasNext()) {
            if (it.next().isOnLive()) {  
                liveCount++;
            } else {
                break; 
            }
        }
		// 栏目下房间都是未开播状态
        if (liveCount == 0) {
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
        int city;
        int platform;
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
        
        Result<List<FirstPageConfDTO>> firstPageListResult;
		List<FirstPageConfDTO> titleList = null;
        try {
            if (configService.getIsAbroad()) {
                // 根据city获取默认渠道ID
                if (city == 0) {
                    String clientIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
                    city = CityUtil.getCityIdByIpAddr(clientIp);
                }
                int defaultChannel = CityUtil.getCityDefaultChannel(city);
				firstPageListResult = hallHomeService.getPartListInHome(appId, channel, defaultChannel);
            } else {
				firstPageListResult = hallHomeService.getSquareTitleList(appId, channel);
            }
            if (firstPageListResult != null) {
				titleList = firstPageListResult.getData();
			}
            
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        if (titleList != null) {
            JsonArray plateList = new JsonArray();
            for (FirstPageConfDTO hp : titleList) {
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
                
                //  栏目类型
                if (hp.getSeatType() != null) {
                    json.addProperty("seatType", hp.getSeatType());
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

		HotRoomInfoDTO hotRoomInfo = null;
        try {
        	Result<HotRoomInfoDTO> hotRoomResult = hallRoomService.getHotRoomInfo(roomId);
        	if (hotRoomResult != null) {
				hotRoomInfo = hotRoomResult.getData();
			}
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

	/**
	 * 根据栏目id获取房间列表，按照weight权重排序
	 */
	public JsonObject getRoomListByCataIdOrderWeight(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		int cataId, start, num, platform;
		try {
			cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 0, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			num = CommonUtil.getJsonParamInt(jsonObject, "num", 10, null, 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
		} catch (Exception e) {
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		result.addProperty("isdownload", 1);

		HallPartConfDTO sysMenu = null;

		//查询栏目
		QueryHallRoomInfoParam params = new QueryHallRoomInfoParam();
		params.setCataId(cataId);
		params.setStart(start);
		params.setNum(num);
		try {
			Result<HallPartConfDTO> hallPartConfDTOResult = hallPartService.queryRoomList(params);
			if (Objects.equals(CommonStateCode.SUCCESS, hallPartConfDTOResult.getCode())) {
				sysMenu = hallPartConfDTOResult.getData();
			}
		} catch(Exception e) {
			logger.error(String.format("Error: hallPartService.queryRoomList(params=%s)", params), e);
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
				roomTotal = sysMenu.getRoomCount();
			}
			if(subTitle != null) {
				result.addProperty("subTitle", subTitle);
			}
			result.addProperty("roomTotal", roomTotal);

			JsonArray roomArray = new JsonArray();
			JsonObject json;
			List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
			HallRoomInfoDTO roomInfo;
			if (roomList != null) {
				int i = 0;
				while (i < roomList.size()) {
					roomInfo = roomList.get(i++);
					json = HallRoomTF.roomInfoToJson(roomInfo, platform);
					json.addProperty("weight", roomInfo.getWeight());
					roomArray.add(json);
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
}
