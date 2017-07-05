/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action.external;


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.album.service.AlbumContentService;
import com.melot.game.config.sdk.album.service.PictureAlbumService;
import com.melot.game.config.sdk.domain.AlbumContent;
import com.melot.game.config.sdk.domain.PictureAlbum;
import com.melot.kkgame.action.BaseAction;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: PictureAlbumFunction
 * <p>
 * Description: 图片墙相关接口
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-10-26 下午3:45:39 
 */
public class PictureAlbumFunction extends BaseAction{

    private static Logger logger = Logger.getLogger(PictureAlbumFunction.class);
    
    /***
     * 获取相册列表
     * [fucTag=20100001] 
     */
	public JsonObject getAlbumList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		Integer channel, ownerId, start, offset;
		try {
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 1, Integer.MAX_VALUE);
			ownerId = CommonUtil.getJsonParamInt(jsonObject, "ownerId", 0, null, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		if (ownerId == 0) {
			ownerId = null;
		}
		PictureAlbumService pictureAlbumService = MelotBeanFactory.getBean("pictureAlbumService", PictureAlbumService.class);
		JsonArray roomArray = new JsonArray();
		List<PictureAlbum> albumList = pictureAlbumService.getPageList(ownerId, channel, start, offset);
		for (int i = 0; i < albumList.size(); i++) {// 海报封面地址http://ures.kktv8.com/kktv/portrait/
			roomArray.add(albumToJson(albumList.get(i)));
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("albumList", roomArray);
		
		return result;
	}
    
    private JsonObject albumToJson(PictureAlbum album){
        JsonObject json = new JsonObject();
        json.addProperty("albumId", album.getAlbumId()); //相册id
        json.addProperty("title", album.getAlbumTitle()); //相册标题
        json.addProperty("poster", album.getAlbumPoster()); //海报地址
        json.addProperty("cover", album.getAlbumCoverPic()); //封面地址
        json.addProperty("desciption", album.getAlbumDescribe()); //相册描述详情
        return json;
    }
    /***
     * 获取相册详情, 包含相册内全部图片列表, 不考虑一个相册有几百张的情形
     * [fucTag=20100002] 
     */
	public JsonObject getAlbumDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int albumId;
		try {
			albumId = CommonUtil.getJsonParamInt(jsonObject, "albumId", 0, TagCodeEnum.ALBUMID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		PictureAlbumService pictureAlbumService = MelotBeanFactory.getBean("pictureAlbumService", PictureAlbumService.class);
		PictureAlbum pAlbum = pictureAlbumService.getPictureAlbumByAlbumId(albumId);
		if (pAlbum == null) {
			logger.info("albumId = " + albumId + ", 此相册不存在");
			result.addProperty("albumId", albumId);
			result.add("picList", new JsonArray());
		} else {
			AlbumContentService albumContentService = MelotBeanFactory.getBean("albumContentService", AlbumContentService.class);
			result.addProperty("albumId", albumId);
			List<AlbumContent> contentList = albumContentService.getAlbumContentsByAlbumId(albumId);
			JsonArray picArray = new JsonArray();
			for (int i = 0; i < contentList.size(); i++) {
				picArray.add(picContentToJson(contentList.get(i)));
			}
			result.add("picList", picArray);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
    
    private JsonObject picContentToJson(AlbumContent albumContent){
        JsonObject json = new JsonObject();
        json.addProperty("contentId", albumContent.getContentId());
        json.addProperty("picTitle", albumContent.getTitle());
        json.addProperty("describe", albumContent.getDescribe());
        json.addProperty("picUrl", albumContent.getContentPath());
        json.addProperty("type", albumContent.getType());
        json.addProperty("viewTimes", albumContent.getViewTimes());
        json.addProperty("likeTimes", albumContent.getLikeTimes());
        json.addProperty("size", albumContent.getSize());
        json.addProperty("isCover", albumContent.getIsCover());
        return json;
    }
    
}
