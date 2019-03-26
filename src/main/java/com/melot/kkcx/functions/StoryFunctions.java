package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kk.competition.api.constant.ReturnCode;
import com.melot.kk.competition.api.dto.ReportDetailDTO;
import com.melot.kk.story.api.dto.StoryDTO;
import com.melot.kk.story.api.service.StoryService;
import com.melot.kkcore.actor.api.ActorInfo;
import com.melot.kkcore.actor.api.RoomInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2019/3/21.
 */
public class StoryFunctions {

    private static Logger logger = Logger.getLogger(StoryFunctions.class);

    @Resource
    StoryService storyService;

    @Resource
    ActorService actorService;

    /**
     * 51011601
     * 随机获取一个故事
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getAStory(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try{
            StoryDTO story = storyService.getAStory();
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            if(story!= null){
                result.addProperty("videoCover", story.getVideoCover());
                result.addProperty("video", story.getVideo());
                result.addProperty("actorId", story.getActorId());
            }
        }
        catch (Exception e){
            logger.error("Error getAStory()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 51011602
     * 随机获取一个故事
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject bindUserToRoom(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, actorId, channel;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 0, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            if(userId>0 && actorId>0 && channel>0){
                storyService.bindUserStory(userId,actorId,channel);
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        catch (Exception e){
            logger.error("Error bindUserToRoom()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 51011603
     * 检查是否有绑定
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkBind(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            StoryDTO storyDTO = storyService.getBindStory(userId);
            if(storyDTO != null){
                result.addProperty("actorId",storyDTO.getActorId());
                result.addProperty("poster",storyDTO.getPoster());
                RoomInfo roomInfo = actorService.getRoomInfoByIdWithoutCache(storyDTO.getActorId());
                if(roomInfo != null){
                    if(roomInfo.getLiveEndTime() == null){
                        result.addProperty("isLive",1);
                    }
                    else {
                        result.addProperty("isLive",0);
                    }
                    result.addProperty("roomSource",roomInfo.getRoomSource());
                    result.addProperty("screenType",roomInfo.getScreenType());
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        catch (Exception e){
            logger.error("Error checkBind()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }


}
