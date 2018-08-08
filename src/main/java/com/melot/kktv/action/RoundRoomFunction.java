package com.melot.kktv.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kkcx.service.RoomService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.round.driver.domain.RoundRoomActInfo;
import com.melot.round.driver.domain.RoundRoomActList;
import com.melot.round.driver.service.RoundRoomService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class RoundRoomFunction {
	
	private static final int cache_time = 300;
	
	/**
	 * 获取轮播房节目列表(50001013)
	 * 
	 * @param jsonObject 请求对象
	 * @return 登录结果
	 */
	public JsonObject getRoundRoomActList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		JsonArray roomActList = new JsonArray();
		long systemTime = 0l;
		int roomId;
		try {
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		String dateString =new SimpleDateFormat("yyyy-MM-dd 04:00:00").format(new Date());
		Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Transaction t = Cat.getProducer().newTransaction("MCall", "RoundRoomService.getRoundRoomActList");
		try {
			RoundRoomService roundRoomService = MelotBeanFactory.getBean("roundRoomService", RoundRoomService.class);
			RoundRoomActList roundRoomActList = roundRoomService.getRoundRoomActListNew(roomId, 0, startTime,DateUtil.addOnField(startTime, Calendar.DATE, 1));
			if (roundRoomActList !=null) {
				if (roundRoomActList.getRoomActList() != null && roundRoomActList.getRoomActList().size() > 0) {
					String cacheKey = null;
					Gson gson = new Gson();
					Date endTime = null;
					Date beginTime = null;
					for (RoundRoomActInfo actInfo : roundRoomActList.getRoomActList()) {
						beginTime = dateFormat.parse(actInfo.getStartTime());
						endTime = dateFormat.parse(actInfo.getEndTime());
//						if (endTime.getTime() < beginTime.getTime()) {
//							endTime = DateUtil.addOnField(endTime, Calendar.DATE, 1);
//						}
						systemTime = roundRoomActList.getSystemTime();
						// 已完成也展示
//						if (systemTime >= endTime.getTime()) {
//							continue;
//						}
						RoomInfo roomInfo = RoomService.getRoomInfo(actInfo.getActorId());
						if (roomInfo != null) {
							// 去除因为缓存造成的数据替换
							if (actInfo.getStatus().equals("1") && roomInfo.getActorId().intValue() != roomInfo.getRoomId().intValue()) {
								roomInfo = RoomService.getRoomInfoByIdInDb(actInfo.getActorId());
							}
							JsonObject actObject = null;
							//此处缓存10分钟减少 昵称验证，星级获取耗时
							cacheKey = String.format("room_act_info_%s_%s",roomInfo.getRoomId(),roomInfo.getActorId());
							String tempData = HotDataSource.getTempDataString(cacheKey);
							if (StringUtils.isNotBlank(tempData)) {
								actObject = gson.fromJson(tempData, JsonObject.class);
							}
							if (actObject == null || !actObject.isJsonObject()) {
								actObject = RoomTF.roomInfoToJson(roomInfo, 1, true);
								HotDataSource.setTempDataString(cacheKey, gson.toJson(actObject), cache_time);
							}
							actObject.addProperty("startTime", beginTime.getTime());
							actObject.addProperty("endTime", endTime.getTime());
							actObject.addProperty("status", actInfo.getStatus());
							roomActList.add(actObject);
						}
					}
				}
			}
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
			t.setStatus(e);
		} finally {
			t.complete();
		}
	    result.add("roomActList", roomActList);
	    result.addProperty("roomId", roomId);
	    result.addProperty("systemTime", systemTime==0l?new Date().getTime():systemTime);
	    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}



}
