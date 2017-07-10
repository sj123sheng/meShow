package com.melot.kkcx.transform;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.RoomSideLabel;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.confdynamic.SystemConfig;

public class RoomTF {
	
	/**
	 * Transform RoomInfo Object to JsonObject 
	 * @param roomInfo
	 * @param platform
	 * @return
	 */
	public static JsonObject roomInfoToJson(RoomInfo roomInfo, int platform) {
	    return roomInfoToJson(roomInfo, platform, false);
	}

	
	/**
	 * @param roomInfo
	 * @param platform
	 * @param ifHttpPack       是否拼接前缀，false-不 true-拼接
	 * @return
	 */
	public static JsonObject roomInfoToJson(RoomInfo roomInfo, int platform, boolean ifHttpPack) {
	    return roomInfoToJson(roomInfo, platform, ifHttpPack, false);
	}
	
    
    /**
     * @param roomInfo
     * @param platform
     * @param ifHttpPack
     * @param onlyCore    false 有拓展字段
     *                    true 仅仅核心字段
     * @return
     */
    public static JsonObject roomInfoToJson(RoomInfo roomInfo, int platform, boolean ifHttpPack, boolean onlyCore) {
        JsonObject roomObject = new JsonObject();
        
        if (roomInfo.getActorId() != null) {
            if (onlyCore) {
                extractCommonRoomInfoCore(roomInfo, roomObject);
            }else {
                extractCommonRoomInfo(roomInfo, roomObject);
            }
            
            String basePictureDir = ifHttpPack ? ConfigHelper.getHttpdir() : "";
            
            String portraitAddress = roomInfo.getPortrait();
            if (StringUtil.strIsNull(portraitAddress)){
                portraitAddress = ConstantEnum.DEFAULT_PORTRAIT_USER;
            }
            portraitAddress = basePictureDir + portraitAddress;
            if (!StringUtil.strIsNull(portraitAddress)) {
                roomObject.addProperty("portrait_path_original", portraitAddress);
                roomObject.addProperty("portrait_path_48", portraitAddress + "!48");
                roomObject.addProperty("portrait_path_128", portraitAddress + "!128");
                roomObject.addProperty("portrait_path_208", portraitAddress + "!208");
                roomObject.addProperty("portrait_path_256", portraitAddress + "!256");
                roomObject.addProperty("portrait_path_272", portraitAddress + "!272");
                roomObject.addProperty("portrait_path_1280", portraitAddress + "!1280");
                roomObject.addProperty("portrait_path_400", portraitAddress + "!400");
                roomObject.addProperty("portrait_path_756", portraitAddress + "!756x567");
            }
            if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource().equals(10) && !StringUtil.strIsNull(portraitAddress)) {
                roomObject.addProperty("poster_path_original", portraitAddress);
                roomObject.addProperty("poster_path_48", portraitAddress + "!48");
                roomObject.addProperty("poster_path_128", portraitAddress + "!128x96");
                roomObject.addProperty("poster_path_131", portraitAddress + "!131");
                roomObject.addProperty("poster_path_208", portraitAddress + "!208");
                roomObject.addProperty("poster_path_256", portraitAddress + "!256");
                roomObject.addProperty("poster_path_272", portraitAddress + "!272");
                roomObject.addProperty("poster_path_290", portraitAddress + "!290x164");
                roomObject.addProperty("poster_path_1280", portraitAddress + "!1280");
                roomObject.addProperty("poster_path_400", portraitAddress + "!400");
                roomObject.addProperty("poster_path_756", portraitAddress + "!756x567");
                roomObject.addProperty("poster_path_300", portraitAddress + "!300");
            } else {
                String livePoster = roomInfo.getLivePoster();
                String poster = roomInfo.getPoster();
                if (roomInfo.getRoomSource() != null && roomInfo.getLiveEndtime() == null && (roomInfo.getRoomSource().equals(2) || roomInfo.getRoomSource().equals(7) || roomInfo.getRoomSource().equals(8))) {
                    // 直播主播有动态海报,采用动态海报
                    livePoster = livePoster == null ? poster : livePoster;
                    poster = livePoster;
                }
                if (!StringUtil.strIsNull(livePoster)) {
                    livePoster = basePictureDir + livePoster;
                    roomObject.addProperty("live_poster_original", livePoster);
                    roomObject.addProperty("live_poster_1280", livePoster + "!1280");
                    roomObject.addProperty("live_poster_290", livePoster + "!290x164");
                    roomObject.addProperty("live_poster_272", livePoster + "!272");
                    roomObject.addProperty("live_poster_256", livePoster + "!256");
                    roomObject.addProperty("live_poster_208", livePoster + "!208");
                    roomObject.addProperty("live_poster_131", livePoster + "!131");
                    roomObject.addProperty("live_poster_128", livePoster + "!128x96");
                    roomObject.addProperty("live_poster_400", livePoster + "!400");
                    roomObject.addProperty("live_poster_756", livePoster + "!756x567");
                    roomObject.addProperty("live_poster_300", livePoster + "!300");
                }
                if (!StringUtil.strIsNull(poster)) {
                    poster = basePictureDir + poster;
                    roomObject.addProperty("poster_path_original", poster);
                    roomObject.addProperty("poster_path_1280", poster + "!1280");
                    roomObject.addProperty("poster_path_290", poster + "!290x164");
                    roomObject.addProperty("poster_path_272", poster + "!272");
                    roomObject.addProperty("poster_path_256", poster + "!256");
                    roomObject.addProperty("poster_path_208", poster + "!208");
                    roomObject.addProperty("poster_path_131", poster + "!131");
                    roomObject.addProperty("poster_path_128", poster + "!128x96");
                    roomObject.addProperty("poster_path_400", poster + "!400");
                    roomObject.addProperty("poster_path_756", poster + "!756x567");
                    roomObject.addProperty("poster_path_300", poster + "!300");
                }
            }
        }
        
        return roomObject;
    }
	
	/**
     * 提取RoomInfo转化为json的公共代码
     * @param roomInfo
     * @param roomObject
     */
	private static void extractCommonRoomInfo(RoomInfo roomInfo, JsonObject roomObject) {
		
	    extractCommonRoomInfoCore(roomInfo, roomObject);
		
	    // 轮播房添加roomId，非轮播房正在直播的主播等于actorId
	    roomObject.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
        
        if (roomInfo.getScreenType() != null) {
            roomObject.addProperty("screenType", roomInfo.getScreenType());
        } else {
            roomObject.addProperty("screenType", 1);
        }
        
        if (roomInfo.getPeopleInRoom() != null) {
            roomObject.addProperty("onlineCount", roomInfo.getPeopleInRoom());
        } else {
            roomObject.addProperty("onlineCount", 0);
        }
        if (roomInfo.getMaxCount() != null) {
            roomObject.addProperty("maxCount", roomInfo.getMaxCount());
        }
        if (roomInfo.getLiveType() != null) {
            roomObject.addProperty("liveType", roomInfo.getLiveType());
        } else {
            roomObject.addProperty("liveType", 0);
        }
        if (roomInfo.getLiveStarttime() != null) {
            roomObject.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
        }
        if (roomInfo.getLiveEndtime() != null) {
            roomObject.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
        } else {
            // 房间直播流地址
            // http://pull.kktv8.com/livekktv/70837498.flv
            roomObject.addProperty("liveStream", String.format(Constant.room_lives_tream_format, roomInfo.getActorId()));
        }
        if (roomInfo.getNextStarttime() != null) {
            roomObject.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
        }
        
        // 主播被封号后不显示海报
        if(roomInfo.getLevels() != null && roomInfo.getLevels() < 0){ //封号主播,海报显示
            roomInfo.setPoster(ConstantEnum.ILLEGAL_ROOM_POSTER);
            roomInfo.setLivePoster(ConstantEnum.ILLEGAL_ROOM_POSTER);
        }
        
        if (roomInfo.getIcon() != null) {
            roomObject.addProperty("icon", roomInfo.getIcon());
            if (roomInfo.getIcon().intValue() == 3) {
                roomObject.addProperty("isRookie", 1);
            }
            if (roomInfo.getIcon().intValue() == 6) {
                roomObject.addProperty("isWeekly", 1);
            }
        }
//        if (!StringUtil.strIsNull(roomInfo.getRoomTheme())) {
//            roomObject.addProperty("roomTheme", GeneralService.replaceSensitiveWords(roomInfo.getActorId(), roomInfo.getRoomTheme()));
//        }
        if (roomInfo.getRoomMode() != null) {
            roomObject.addProperty("roomMode", roomInfo.getRoomMode());
        }
        if (roomInfo.getGender() != null) {
            roomObject.addProperty("roomGender", roomInfo.getGender());
        }
        if (roomInfo.getRoomTag() != null) {
            roomObject.addProperty("roomTag", roomInfo.getRoomTag());
        }
        if (roomInfo.getType() != null) {
            roomObject.addProperty("roomType", roomInfo.getType());
        } else {
            roomObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
        }
        if (roomInfo.getRoomSource() != null) {
            roomObject.addProperty("roomSource", roomInfo.getRoomSource());
        } else {
            roomObject.addProperty("roomSource", roomObject.get("roomType").getAsString());
        }
        
        // 置顶位置
        if (roomInfo.getPartPosition() != null) {
            roomObject.addProperty("partPosition", roomInfo.getPartPosition());
        }
        
        if(roomInfo.getMappingScore() != null ){
            if(roomInfo.getMappingScore() > 100){
                roomObject.addProperty("searchTag", 1);
            }else{
                roomObject.addProperty("searchTag", 0);
            }
        }
        
        if (roomInfo.getRegisterCity() != null) {
            roomObject.addProperty("city", CityUtil.getCityName(roomInfo.getRegisterCity()));
        }
        
        //直播间角标
        if (roomInfo.getSideLabelValue() != null) {
            Gson gson = new Gson();
            RoomSideLabel roomSideLabel = (RoomSideLabel) gson.fromJson(roomInfo.getSideLabelValue(), new TypeToken<RoomSideLabel>(){}.getType());
            if (roomSideLabel != null && roomSideLabel.getContent() != null) {
                //有效时间内显示
                if (roomSideLabel.getEffectiveStartTime() == null || roomSideLabel.getEffectiveStartTime().getTime() < System.currentTimeMillis()) {
                    if (roomSideLabel.getEffectiveEndTime() == null || roomSideLabel.getEffectiveEndTime().getTime() > System.currentTimeMillis()) {
                        roomObject.addProperty("sideLabelContent", roomSideLabel.getContent());
                        if (roomSideLabel.getColor() != null) {
                            roomObject.addProperty("sideLabelColor", roomSideLabel.getColor());
                        }
                        if (roomSideLabel.getEffectiveStartTime() != null) {
                            roomObject.addProperty("sideLabelStartTime", roomSideLabel.getEffectiveStartTime().getTime());
                        }
                        if (roomSideLabel.getEffectiveEndTime() != null) {
                            roomObject.addProperty("sideLabelEndTime", roomSideLabel.getEffectiveEndTime().getTime());
                        }
                    }
                }
            }
        }
        
        if (roomInfo.getRoomMode() != null && roomInfo.getRoomMode() > 10) {
            String modeLabelPath = SystemConfig.getValue(String.format("modeLabelPath_%d", roomInfo.getRoomMode()), AppIdEnum.AMUSEMENT);
            if (modeLabelPath != null) {
                roomObject.addProperty("modeLabelPath", modeLabelPath);
            }
        }
        
        // 读取星级
        roomObject.addProperty("starLevel", UserService.getStarLevel(roomInfo.getActorId()));
        
    	// 读取靓号
 		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(roomInfo.getActorId()); //获取用户虚拟账号
 		if(validVirtualId != null) {
 			if (validVirtualId.get("idType").getAsInt() == 1) {
 				// 支持老版靓号
 				roomObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
 			}
 			roomObject.add("validId", validVirtualId);
 		}
	}
	
	private static void extractCommonRoomInfoCore(RoomInfo roomInfo, JsonObject roomObject) {
	    roomObject.addProperty("userId", roomInfo.getActorId());
	    
	    if (!StringUtil.strIsNull(roomInfo.getNickname())) {
            roomObject.addProperty("nickname", GeneralService.replaceSensitiveWords(roomInfo.getActorId(), roomInfo.getNickname()));
        }
	    
	    if (roomInfo.getActorLevel() != null) {
            roomObject.addProperty("actorLevel", roomInfo.getActorLevel());
        } else {
            roomObject.addProperty("actorLevel", 0);
        }
	    
        if (roomInfo.getRichLevel() != null) {
            roomObject.addProperty("richLevel", roomInfo.getRichLevel());
        } else {
            roomObject.addProperty("richLevel", 0);
        }
        
        if (roomInfo.getGender() != null) {
            roomObject.addProperty("gender", roomInfo.getGender());
        } else {
            roomObject.addProperty("gender", 0);
        }
        
        if (roomInfo.getRegisterCity() != null) {
            roomObject.addProperty("cityId", Math.abs(roomInfo.getRegisterCity()));
        }
	}
	
	/**
	 * Transform RoomInfo Object to JsonObject 临时用于兼容老版本
	 * @param roomInfo
	 * @return
	 */
	public static JsonObject roomInfoToJsonTemp(RoomInfo roomInfo, int platform) {
	    
		JsonObject roomObject = new JsonObject();
		if (roomInfo.getActorId() != null) {
			
			// 轮播房添加roomId，非轮播房正在直播的主播等于actorId
		    roomObject.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
			roomObject.addProperty("userId", roomInfo.getActorId());
            
            if (roomInfo.getScreenType() != null) {
                roomObject.addProperty("screenType", roomInfo.getScreenType());
            } else {
                roomObject.addProperty("screenType", 1);
            }
			
			if (!StringUtil.strIsNull(roomInfo.getNickname())) {
				roomObject.addProperty("nickname", roomInfo.getNickname());
			}
			if (roomInfo.getGender() != null) {
				roomObject.addProperty("gender", roomInfo.getGender());
			}
			if (roomInfo.getPeopleInRoom() != null) {
				roomObject.addProperty("onlineCount", roomInfo.getPeopleInRoom());
			}
			if (roomInfo.getMaxCount() != null) {
				roomObject.addProperty("max", roomInfo.getMaxCount());
			}
			if (roomInfo.getLiveType() != null) {
				roomObject.addProperty("liveType", roomInfo.getLiveType());
			}
			if (roomInfo.getLiveStarttime() != null) {
				roomObject.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
			}
			if (roomInfo.getLiveEndtime() != null) {
				roomObject.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
			}
			if (roomInfo.getNextStarttime() != null) {
				roomObject.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
			}
			if (roomInfo.getActorLevel() != null) {
				roomObject.addProperty("actorLevel", roomInfo.getActorLevel());
			}
			if (roomInfo.getRichLevel() != null) {
				roomObject.addProperty("richLevel", roomInfo.getRichLevel());
			}
			if (roomInfo.getIcon() != null) {
				roomObject.addProperty("icon", roomInfo.getIcon());
				if (roomInfo.getIcon().intValue() == 3) {
					roomObject.addProperty("isRookie", 1);
				}
				if (roomInfo.getIcon().intValue() == 6) {
					roomObject.addProperty("isWeekly", 1);
				}
			}
			if (roomInfo.getRoomMode() != null) {
				// 房间模式 0:普通房 1:演艺房 2:游戏房
				roomObject.addProperty("roomMode", roomInfo.getRoomMode());
				if (roomInfo.getRoomMode().intValue() == 1) {
					roomObject.addProperty("videoLevel", 1);
				}
				if (roomInfo.getRoomMode().intValue() == 2) {
					roomObject.addProperty("userPart", "[107]");
					roomObject.addProperty("videoLevel", 1);
				}
			}
//			if (!StringUtil.strIsNull(roomInfo.getRoomTheme())) {
//				roomObject.addProperty("roomTheme", roomInfo.getRoomTheme());
//			}
            if (roomInfo.getType() != null) {
                roomObject.addProperty("roomType", roomInfo.getType());
            } else {
                roomObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
            }
            if (roomInfo.getRoomSource() != null) { // 房间主播来源
                roomObject.addProperty("roomSource", roomInfo.getRoomSource());
            } else {
                roomObject.addProperty("roomSource", roomObject.get("roomType").getAsString());
            }
            String portrait = roomInfo.getPortrait();
			if (!StringUtil.strIsNull(portrait)) {
				if (platform == PlatformEnum.WEB) {
					roomObject.addProperty("portrait_path_48", portrait + "!48");
					roomObject.addProperty("portrait_path_256", portrait + "!256");
					roomObject.addProperty("portrait_path_300", portrait + "!300");
					roomObject.addProperty("portrait_path_400", portrait + "!400");
	                roomObject.addProperty("portrait_path_756", portrait + "!756x567");
				}
				if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
					roomObject.addProperty("portrait_path_128", portrait + "!128");
					roomObject.addProperty("portrait_path_256", portrait + "!256");
					roomObject.addProperty("portrait_path_400", portrait + "!400");
	                roomObject.addProperty("portrait_path_756", portrait + "!756x567");
				}
			}
			if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource().equals(10) && !StringUtil.strIsNull(portrait)) {
			    if (platform == PlatformEnum.WEB) {
                    roomObject.addProperty("poster_path_290", portrait + "!290x164");
                    roomObject.addProperty("poster_path_272", portrait + "!272");
                    roomObject.addProperty("poster_path_128", portrait + "!128x96");
                    roomObject.addProperty("poster_path_400", portrait + "!400");
                    roomObject.addProperty("poster_path_756", portrait + "!756x567");
                    roomObject.addProperty("poster_path_300", portrait + "!300");
                }
                if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
                    roomObject.addProperty("poster_path_1280", portrait + "!1280");
                    roomObject.addProperty("poster_path_272", portrait + "!272");
                    roomObject.addProperty("poster_path_128", portrait + "!128x96");
                    roomObject.addProperty("poster_path_400", portrait + "!400");
                    roomObject.addProperty("poster_path_756", portrait + "!756x567");
                    roomObject.addProperty("poster_path_300", portrait + "!300");
                }
            } else {
                if (!StringUtil.strIsNull(roomInfo.getLivePoster()) 
                        && roomInfo.getRoomSource() != null 
                        && (roomInfo.getRoomSource().equals(2) || roomInfo.getRoomSource().equals(7) || roomInfo.getRoomSource().equals(8))) {
                    if(platform == PlatformEnum.WEB) {
                        roomObject.addProperty("poster_path_290", roomInfo.getLivePoster() + "!290x164");
                        roomObject.addProperty("poster_path_272", roomInfo.getLivePoster() + "!272");
                        roomObject.addProperty("poster_path_128", roomInfo.getLivePoster() + "!128x96");
                        roomObject.addProperty("live_poster_290", roomInfo.getLivePoster() + "!290x164");
                        roomObject.addProperty("live_poster_272", roomInfo.getLivePoster() + "!272");
                        roomObject.addProperty("live_poster_128", roomInfo.getLivePoster() + "!128x96");
                        roomObject.addProperty("poster_path_400", roomInfo.getLivePoster() + "!400");
                        roomObject.addProperty("poster_path_756", roomInfo.getLivePoster() + "!756x567");
                        roomObject.addProperty("poster_path_300", roomInfo.getLivePoster() + "!300");
                        roomObject.addProperty("live_poster_400", roomInfo.getLivePoster() + "!400");
                        roomObject.addProperty("live_poster_756", roomInfo.getLivePoster() + "!756x567");
                        roomObject.addProperty("live_poster_300", roomInfo.getLivePoster() + "!300");
                    }
                    if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
                        roomObject.addProperty("poster_path_1280", roomInfo.getLivePoster() + "!1280");
                        roomObject.addProperty("poster_path_272", roomInfo.getLivePoster() + "!272");
                        roomObject.addProperty("poster_path_128", roomInfo.getLivePoster() + "!128x96");
                        roomObject.addProperty("live_poster_272", roomInfo.getLivePoster() + "!272");
                        roomObject.addProperty("live_poster_128", roomInfo.getLivePoster() + "!128x96");
                        roomObject.addProperty("poster_path_400", roomInfo.getLivePoster() + "!400");
                        roomObject.addProperty("poster_path_756", roomInfo.getLivePoster() + "!756x567");
                        roomObject.addProperty("poster_path_300", roomInfo.getLivePoster() + "!300");
                        roomObject.addProperty("live_poster_400", roomInfo.getLivePoster() + "!400");
                        roomObject.addProperty("live_poster_756", roomInfo.getLivePoster() + "!756x567");
                        roomObject.addProperty("live_poster_300", roomInfo.getLivePoster() + "!300");
                    }
                } else {
                    if (!StringUtil.strIsNull(roomInfo.getPoster())) {
                        if (platform == PlatformEnum.WEB) {
                            roomObject.addProperty("poster_path_290", roomInfo.getPoster() + "!290x164");
                            roomObject.addProperty("poster_path_272", roomInfo.getPoster() + "!272");
                            roomObject.addProperty("poster_path_128", roomInfo.getPoster() + "!128x96");
                            roomObject.addProperty("poster_path_400", roomInfo.getPoster() + "!400");
                            roomObject.addProperty("poster_path_756", roomInfo.getPoster() + "!756x567");
                            roomObject.addProperty("poster_path_300", roomInfo.getPoster() + "!300");
                        }
                        if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
                            roomObject.addProperty("poster_path_1280", roomInfo.getPoster() + "!1280");
                            roomObject.addProperty("poster_path_272", roomInfo.getPoster() + "!272");
                            roomObject.addProperty("poster_path_128", roomInfo.getPoster() + "!128x96");
                            roomObject.addProperty("poster_path_400", roomInfo.getPoster() + "!400");
                            roomObject.addProperty("poster_path_756", roomInfo.getPoster() + "!756x567");
                            roomObject.addProperty("poster_path_300", roomInfo.getPoster() + "!300");
                        }
                    }
                }
            }
			
			//获取用户虚拟账号
			JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(roomInfo.getActorId());
			if(validVirtualId != null) {
				if (validVirtualId.get("idType").getAsInt() == 1) {
					// 支持老版靓号
					roomObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
				}
				roomObject.add("validId", validVirtualId);
			}
            
			if (roomInfo.getRegisterCity() != null) {
			    roomObject.addProperty("city", CityUtil.getCityName(roomInfo.getRegisterCity()));
            }
			
	        //直播间角标
	        if (roomInfo.getSideLabelValue() != null) {
	            Gson gson = new Gson();
	            RoomSideLabel roomSideLabel = (RoomSideLabel) gson.fromJson(roomInfo.getSideLabelValue(), new TypeToken<RoomSideLabel>(){}.getType());
	            if (roomSideLabel != null && roomSideLabel.getContent() != null) {
	                //有效时间内显示
	                if (roomSideLabel.getEffectiveStartTime() == null || roomSideLabel.getEffectiveStartTime().getTime() < System.currentTimeMillis()) {
	                    if (roomSideLabel.getEffectiveEndTime() == null || roomSideLabel.getEffectiveEndTime().getTime() > System.currentTimeMillis()) {
	                        roomObject.addProperty("sideLabelContent", roomSideLabel.getContent());
	                        if (roomSideLabel.getColor() != null) {
	                            roomObject.addProperty("sideLabelColor", roomSideLabel.getColor());
	                        }
	                        if (roomSideLabel.getEffectiveStartTime() != null) {
	                            roomObject.addProperty("sideLabelStartTime", roomSideLabel.getEffectiveStartTime().getTime());
	                        }
	                        if (roomSideLabel.getEffectiveEndTime() != null) {
	                            roomObject.addProperty("sideLabelEndTime", roomSideLabel.getEffectiveEndTime().getTime());
	                        }
	                    }
	                }
	            }
	        }
	        
	        if (roomInfo.getRoomMode() != null && roomInfo.getRoomMode() > 10) {
	            String modeLabelPath = SystemConfig.getValue(String.format("modeLabelPath_%d", roomInfo.getRoomMode()), AppIdEnum.AMUSEMENT);
	            if (modeLabelPath != null) {
	                roomObject.addProperty("modeLabelPath", modeLabelPath);
	            }
	        }
		}
		
		return roomObject;
	}
	
}
