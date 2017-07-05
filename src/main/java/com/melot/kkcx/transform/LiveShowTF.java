package com.melot.kkcx.transform;

import com.google.gson.JsonObject;
import com.melot.content.config.domain.LiveAlbum;
import com.melot.content.config.domain.LiveVideo;

/**
 * 
 * 类LiveShowTF.java的实现描述：kk现场类实现描述 
 * @author Administrator 2014年12月10日 下午2:44:33
 */
public class LiveShowTF {

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public static JsonObject toJsonObject(LiveAlbum liveAlbum, String domain) {
		
		JsonObject jObject = new JsonObject();
		jObject.addProperty("title", liveAlbum.getTitle());
		jObject.addProperty("subTitle", liveAlbum.getSubTitle());
		jObject.addProperty("albumId", liveAlbum.getAlbumId());
		jObject.addProperty("picture", domain + liveAlbum.getPicture());
		
		return jObject;
	}
	
	public static JsonObject toInnerJsonObject(LiveAlbum liveAlbum, String domain) {

		JsonObject jObject = new JsonObject();
		jObject.addProperty("title", liveAlbum.getTitle());
		jObject.addProperty("subTitle", liveAlbum.getSubTitle());
		jObject.addProperty("albumId", liveAlbum.getAlbumId());
		jObject.addProperty("describe", liveAlbum.getDescribe());
		domain = domain != null ? domain : "";
		jObject.addProperty("poster", domain + liveAlbum.getPoster());

		return jObject;
	}
	
	public static JsonObject toInnerVideoobject(LiveVideo lv, String domain) {
		
		JsonObject jObject = new JsonObject();
		jObject.addProperty("title", lv.getTitle());
		jObject.addProperty("videoId", lv.getVideoId());
		jObject.addProperty("describe", lv.getDescribe());
		jObject.addProperty("path", domain + lv.getPath());
		jObject.addProperty("picture", domain + lv.getPicture());
		if (lv.getCreateTime() != null) 
			jObject.addProperty("createTime", lv.getCreateTime().getTime());
		
		return jObject;
	}

}
