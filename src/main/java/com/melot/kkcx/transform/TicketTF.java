package com.melot.kkcx.transform;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.module.packagegift.driver.domain.EntranceTicket;

public class TicketTF {
	
	private static Logger logger = Logger.getLogger(TicketTF.class);
	
	/**
	 * Transform JavaBean TicketInfo To JsonObject
	 * @param ticketInfo
	 * @param platform
	 * @return ticketObject
	 */
	public static JsonObject ticketToJson(EntranceTicket ticketInfo, int platform) {
		
		String httpdir = ConfigHelper.getHttpdir();
		if (ticketInfo == null) return null;
		
		JsonObject ticketJson = new JsonObject();
		
		if (ticketInfo.getTicketId() != null) {
			ticketJson.addProperty("ticketId", ticketInfo.getTicketId());
		}
		if (ticketInfo.getTitle() != null && !ticketInfo.getTitle().trim().isEmpty()) {
			ticketJson.addProperty("title", ticketInfo.getTitle());
		}
		if (ticketInfo.getBanner() != null && !ticketInfo.getBanner().trim().isEmpty()) {
			ticketJson.addProperty("banner",  httpdir + ticketInfo.getBanner());
		}
		if (ticketInfo.getDescription() != null && !ticketInfo.getDescription().trim().isEmpty()) {
		    ticketJson.addProperty("description", ticketInfo.getDescription());
		}
		if (ticketInfo.getIcon() != null && !ticketInfo.getIcon().trim().isEmpty()) {
			ticketJson.addProperty("icon", httpdir + ticketInfo.getIcon());
		}
		if (ticketInfo.getPrice() != null) {
		    ticketJson.addProperty("price", ticketInfo.getPrice());
		}
		if (ticketInfo.getDefinition() != null && !ticketInfo.getDefinition().trim().isEmpty()) {
			ticketJson.addProperty("definition", ticketInfo.getDefinition());
		}
		if (ticketInfo.getSoldCount() != null) {
			ticketJson.addProperty("soldCount", ticketInfo.getSoldCount());
		}
		if (ticketInfo.getTicketCount() != null) {
			ticketJson.addProperty("ticketCount", ticketInfo.getTicketCount());
		}
		if (ticketInfo.getRoomIds() != null && !ticketInfo.getRoomIds().trim().isEmpty()) {
			ticketJson.addProperty("roomIds", ticketInfo.getRoomIds());
		}
		if (ticketInfo.getStartTime() != null) {
			ticketJson.addProperty("startTime", ticketInfo.getStartTime().getTime());
		}
		if (ticketInfo.getEndTime() != null) {
			ticketJson.addProperty("endTime", ticketInfo.getEndTime().getTime());
		}
		if (ticketInfo.getStartSaleTime() != null) {
			ticketJson.addProperty("startSaleTime", ticketInfo.getStartSaleTime().getTime());
		}
		if (ticketInfo.getEndSaleTime() != null) {
			ticketJson.addProperty("endSaleTime", ticketInfo.getEndSaleTime().getTime());
		}
        if (ticketInfo.getVideoUrl() != null && !ticketInfo.getVideoUrl().trim().isEmpty()) {
            ticketJson.addProperty("videoUrl", ConfigHelper.getMediahttpdir() + ticketInfo.getVideoUrl());
        }
        if (ticketInfo.getSubTitle() != null && !ticketInfo.getSubTitle().trim().isEmpty()) {
            ticketJson.addProperty("subTitle", ticketInfo.getSubTitle());
        }
        // 不同平台poster尺寸不同
        if (ticketInfo.getPosterUrl() != null && !ticketInfo.getPosterUrl().trim().isEmpty()) {
            try {
		        JsonObject posterJson = new JsonParser().parse(ticketInfo.getPosterUrl()).getAsJsonObject();
		        switch (platform) {
		        case 1:
		            if (posterJson.has("w") && posterJson.get("w") != null) {
		                ticketJson.addProperty("posterUrl", httpdir + posterJson.get("w").getAsString());
		            }
		            break;
		        case 2:
		            if (posterJson.has("i") && posterJson.get("i") != null) {
		                ticketJson.addProperty("posterUrl", httpdir + posterJson.get("i").getAsString());
		            }
		            break;
		        case 3:
		            if (posterJson.has("a") && posterJson.get("a") != null) {
		                ticketJson.addProperty("posterUrl", httpdir + posterJson.get("a").getAsString());
		            }
		            break;
		        default:
		            break;
		        }
		    } catch (Exception e) {
		        logger.error("TicketTF. fail to parse ticketInfo banner, banner : " + ticketInfo.getBanner());
		    }
        }
        if (ticketInfo.getPreviewTime() != null) {
            ticketJson.addProperty("previewTime", ticketInfo.getPreviewTime());
        }
        
        // 增加返回roomSource 默认返回唱响
        ticketJson.addProperty("roomSource", AppIdEnum.AMUSEMENT);
        ticketJson.addProperty("roomType", AppIdEnum.AMUSEMENT);
		
		return ticketJson;
	}
	
	/**
	 *  Transform JavaBean TicketInfo To JsonObject
	 * @param ticketInfo
	 * @return
	 */
	public static JsonObject ticketToJson(EntranceTicket ticketInfo) { 
		JsonObject ticketJson = new JsonObject();
		
		String httpdir = ConfigHelper.getHttpdir();
		ticketJson.addProperty("ticketId", ticketInfo.getTicketId());
		ticketJson.addProperty("banner", httpdir + ticketInfo.getBanner());
		ticketJson.addProperty("previewTime", ticketInfo.getPreviewTime());
		if (ticketInfo.getStartSaleTime() != null)
			ticketJson.addProperty("startSaleTime", ticketInfo.getStartSaleTime().getTime());
		if (ticketInfo.getEndSaleTime() != null)
			ticketJson.addProperty("endSaleTime", ticketInfo.getEndSaleTime().getTime());
		if (ticketInfo.getStartTime() != null)
			ticketJson.addProperty("startTime", ticketInfo.getStartTime().getTime());
		if (ticketInfo.getEndTime() != null)
			ticketJson.addProperty("endTime", ticketInfo.getEndTime().getTime());
		if (ticketInfo.getVideoUrl() != null) {
			ticketJson.addProperty("videoUrl", ConfigHelper.getMediahttpdir() + ticketInfo.getVideoUrl());
		}
        ticketJson.addProperty("subTitle", ticketInfo.getSubTitle());
        if (ticketInfo.getPosterUrl() != null && !ticketInfo.getPosterUrl().trim().isEmpty()) {
			try {
				JsonObject posterJson = new JsonParser().parse(ticketInfo.getPosterUrl()).getAsJsonObject();
				if (posterJson.has("w") && posterJson.get("w") != null) {
					posterJson.addProperty("w", httpdir + posterJson.get("w").getAsString());
				}
				if (posterJson.has("i") && posterJson.get("i") != null) {
					posterJson.addProperty("i", httpdir + posterJson.get("i").getAsString());
				}
				if (posterJson.has("a") && posterJson.get("a") != null) {
					posterJson.addProperty("a", httpdir + posterJson.get("a").getAsString());
				}
				ticketJson.add("posterUrl", posterJson);
			} catch (Exception e) {
				logger.error("fail to parse ticketInfo posterUrl, posterUrl : " + ticketInfo.getPosterUrl(), e);
			}
		}
        
        // 增加返回roomSource 默认门票信息为kk唱响
        ticketJson.addProperty("roomSource", AppIdEnum.AMUSEMENT);
        ticketJson.addProperty("roomType", AppIdEnum.AMUSEMENT);
        
		return ticketJson;
	}
	
}
