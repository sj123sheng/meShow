package com.melot.kkcx.functions;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.room.pendant.domain.ReturnResult;
import com.melot.room.pendant.dto.UserPendantDTO;
import com.melot.room.pendant.service.PendantService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shoujian
 * Date: 2018-11-20
 * Time: 2:37 PM
 */
public class PendantFunctions {

    private Logger logger = Logger.getLogger(PendantFunctions.class);

    /**
     * 获取用户挂件列表
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    request
     * @return 返回结果
     */
    public JsonObject getUserPendantList(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        Integer userId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            PendantService pendantService = (PendantService) MelotBeanFactory.getBean("pendantService");
            ReturnResult<List<UserPendantDTO>> pendantDTOResult = pendantService.listByUserId(userId);
            if ("0".equals(pendantDTOResult.getCode())) {
                final String pathPrefix = ConfigHelper.getHttpdir();
                List<UserPendantDTO> userPendantDTOList = pendantDTOResult.getData();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("pathPrefix", pathPrefix);
                result.addProperty("pendantDTOList", JSON.toJSONString(userPendantDTOList));

            }
        } catch (Exception e) {
            logger.error("pendantService.listByUserId(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }

    /**
     * 获取用户已佩戴的挂件详情
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    请求
     * @return 挂件详情
     */
    public JsonObject getUserPendant(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        Integer userId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            PendantService pendantService = (PendantService) MelotBeanFactory.getBean("pendantService");
            ReturnResult<UserPendantDTO> pendantDTOResult = pendantService.getUserPendant(userId);
            if ("0".equals(pendantDTOResult.getCode())) {
                final String pathPrefix = ConfigHelper.getHttpdir();
                UserPendantDTO userPendantDTO = pendantDTOResult.getData();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("pathPrefix", pathPrefix);
                result.addProperty("pendantId", userPendantDTO.getPendantId());
                result.addProperty("pendantName", userPendantDTO.getPendantName());
                result.addProperty("pendantBigUrl", userPendantDTO.getPendantBigUrl());
                result.addProperty("pendantSmallUrl", userPendantDTO.getPendantSmallUrl());
                result.addProperty("pendantDescribe", userPendantDTO.getPendantDescribe());
            }
        } catch (Exception e) {
            logger.error("pendantService.getUserPendant(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }

}
