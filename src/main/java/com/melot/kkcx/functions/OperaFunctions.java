package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.opera.api.dto.OperaUserInfoDTO;
import com.melot.kk.opera.api.service.OperaService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2019/4/19.
 */
public class OperaFunctions {

    private static Logger logger = Logger.getLogger(StoryFunctions.class);

    @Resource
    OperaService operaService;

    public JsonObject addOperaUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId,operaType;
        String userName,phoneNum,userDesc,video;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            operaType = CommonUtil.getJsonParamInt(jsonObject, "operaType", 0, null, 0, Integer.MAX_VALUE);
            userName = CommonUtil.getJsonParamString(jsonObject, "userName", null, null, 1, 30);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, null, 1, 20);
            userDesc = CommonUtil.getJsonParamString(jsonObject, "userDesc", null, null, 1, 500);
            video = CommonUtil.getJsonParamString(jsonObject, "video", null, null, 1, 500);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            OperaUserInfoDTO operaUserInfoDTO = new OperaUserInfoDTO();
            operaUserInfoDTO.setUserId(userId);
            operaUserInfoDTO.setOperaType(operaType);
            operaUserInfoDTO.setPhoneNum(phoneNum);
            operaUserInfoDTO.setUserName(userName);
            operaUserInfoDTO.setUserDesc(userDesc);
            if(!StringUtil.strIsNull(video)){
                operaUserInfoDTO.setVideoCover(Pattern.compile("mp4$").matcher(video).replaceAll("jpg"));
                operaUserInfoDTO.setVideo(video);
            }
            operaService.addOperaUserInfo(operaUserInfoDTO);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        catch (Exception e){
            logger.error("Error addOperaUserInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    public JsonObject updateOperaUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId,operaType;
        String userName,phoneNum,userDesc,video;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            operaType = CommonUtil.getJsonParamInt(jsonObject, "operaType", 0, null, 0, Integer.MAX_VALUE);
            userName = CommonUtil.getJsonParamString(jsonObject, "userName", null, null, 1, 30);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, null, 1, 20);
            userDesc = CommonUtil.getJsonParamString(jsonObject, "userDesc", null, null, 1, 500);
            video = CommonUtil.getJsonParamString(jsonObject, "video", null, null, 1, 500);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            OperaUserInfoDTO operaUserInfoDTO = new OperaUserInfoDTO();
            operaUserInfoDTO.setUserId(userId);
            operaUserInfoDTO.setOperaType(operaType);
            operaUserInfoDTO.setPhoneNum(phoneNum);
            operaUserInfoDTO.setUserName(userName);
            operaUserInfoDTO.setUserDesc(userDesc);
            if(!StringUtil.strIsNull(video)){
                operaUserInfoDTO.setVideoCover(Pattern.compile("mp4$").matcher(video).replaceAll("jpg"));
                operaUserInfoDTO.setVideo(video);
            }
            operaService.updateOperaUserInfo(operaUserInfoDTO);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        catch (Exception e){
            logger.error("Error updateOperaUserInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    public JsonObject getOperaUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
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
            OperaUserInfoDTO  operaUserInfoDTO = operaService.getOperaUserInfo(userId);
            if(operaUserInfoDTO!=null){
                result = new JsonParser().parse(new Gson().toJson(operaUserInfoDTO)).getAsJsonObject();
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        catch (Exception e){
            logger.error("Error getOperaUserInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }



}
