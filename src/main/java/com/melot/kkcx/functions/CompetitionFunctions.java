package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kk.competition.api.constant.ReturnCode;
import com.melot.kk.competition.api.dto.*;
import com.melot.kk.competition.api.param.CreateCompetitionParam;
import com.melot.kk.competition.api.param.PageParam;
import com.melot.kk.competition.api.service.CompetitionService;
import com.melot.kk.competition.api.service.FingerGuessingService;
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
 * @since 2018/12/17.
 */
public class CompetitionFunctions {

    private static Logger logger = Logger.getLogger(CompetitionFunctions.class);

    @Resource
    FingerGuessingService fingerGuessingService;

    @Resource
    CompetitionService competitionService;

    /**
     * 51070409
     * 报名参加猜拳
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applyFingerGuessing(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId,histCompetitionId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            histCompetitionId = CommonUtil.getJsonParamInt(jsonObject, "histCompetitionId", 0,"5107040903", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            Result<Boolean> booleanResult = fingerGuessingService.applyFingerGuessing(userId,histCompetitionId);
            if(ReturnCode.SUCCESS.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("result", booleanResult.getData());
            }
            else if(ReturnCode.ERROR_TICKET_NOT_ENOUGH.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040901");
                return result;
            }
            else if(ReturnCode.ERROR_COMPETITION_GAMING.getCode().equals(booleanResult.getCode())||ReturnCode.ERROR_COMPETITION_ALREADY_APPLY.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040904");
                return result;
            }
            else if(ReturnCode.ERROR_ON_OTHER_GAME.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040905");
                return result;
            }
            else if(ReturnCode.ERROR_COMPETITION_APPLY.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_COMPETITION_NOT_EXIST.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_COMPETITION_ROOM_GAMING.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_COMPETITION_ROOM_FULL.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_COMPETITION_TIME.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040902");
                return result;
            }
            else{
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
        catch (Exception e){
            logger.error("Error applyFingerGuessing()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

    /**
     * 51070403
     * 获取消息播报列表
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listReport(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try{
            Result<List<ReportDetailDTO>> reports = competitionService.listReportByUserId();
            if(ReturnCode.SUCCESS.getCode().equals(reports.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("reportDetailDTOList",new Gson().toJsonTree(reports.getData()));
            }
        }
        catch (Exception e){
            logger.error("Error listReportByUserId()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }


        return result;
    }

    /**
     * 51070404
     * 大奖赛官方赛事开赛列表
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listOfficalCompetition(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try{
            Result<List<CompetitionDTO>> competitions = competitionService.listOfficialCompetition();
            if(ReturnCode.SUCCESS.getCode().equals(competitions.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("competitionDTOList",new Gson().toJsonTree(competitions.getData()));
            }
        }
        catch (Exception e){
            logger.error("Error listOfficalCompetition()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

    /**
     * 51070405
     * 大奖赛开赛列表
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listComeptition(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int pageIndex,pageSize;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            pageSize = CommonUtil.getJsonParamInt(jsonObject, "pageSize", 10,null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            PageParam pageParam = new PageParam();
            pageParam.setPageIndex(pageIndex);
            pageParam.setPageSize(pageSize);
            Result<List<CompetitionDTO>> competitions = competitionService.listCompetition(pageParam);
            if(ReturnCode.SUCCESS.getCode().equals(competitions.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("competitionDTOList",new Gson().toJsonTree(competitions.getData()));
            }
        }
        catch (Exception e){
            logger.error("Error listComeptition()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

    /**
     * 51070406
     * 用户大奖赛详情
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getCompetitionUserDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            Result<CompetitionUserDetailDTO> competitionUserDetailDTOResult = competitionService.getCompetitionUserDetail(userId);
            if(ReturnCode.SUCCESS.getCode().equals(competitionUserDetailDTOResult.getCode())){
                CompetitionUserDetailDTO competitionUserDetailDTO = competitionUserDetailDTOResult.getData();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("userId",userId);
                result.addProperty("total",competitionUserDetailDTO.getTotal());
                result.addProperty("winTotal",competitionUserDetailDTO.getWinTotal());
                result.addProperty("ticketTotal",competitionUserDetailDTO.getTicketTotal());
            }
        }
        catch (Exception e){
            logger.error("Error getCompetitionUserDetail()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

    /**
     * 51070407
     * 用户获奖记录
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject pageUserHistory(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId,pageIndex,pageSize;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            pageSize = CommonUtil.getJsonParamInt(jsonObject, "pageSize", 10,null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try{
            PageParam pageParam = new PageParam();
            pageParam.setPageIndex(pageIndex);
            pageParam.setPageSize(pageSize);
            Result<List<UserHistoryDTO>> UserHistoryResult = competitionService.pageUserHistory(userId,pageParam);
            if(ReturnCode.SUCCESS.getCode().equals(UserHistoryResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("userHistoryDTO",new Gson().toJsonTree(UserHistoryResult.getData()));
            }
        }
        catch (Exception e){
            logger.error("Error pageUserHistory()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }


        return result;
    }

    /**
     * 51070408
     * 创建大奖赛
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject createCompetition(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int gameType,userId,roomId,gameConfigId;
        String competitionName;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameType = CommonUtil.getJsonParamInt(jsonObject, "gameType", 0, "5107040803", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, "5107040804", 1, Integer.MAX_VALUE);
            gameConfigId = CommonUtil.getJsonParamInt(jsonObject, "gameConfigId", 0, "5107040805", 1, Integer.MAX_VALUE);
            competitionName = CommonUtil.getJsonParamString(jsonObject, "competitionName", null, "5107040806", 1, 30);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try{
            CreateCompetitionParam createCompetitionParam = new CreateCompetitionParam();
            createCompetitionParam.setCreateUserId(userId);
            createCompetitionParam.setGameType(gameType);
            createCompetitionParam.setRoomId(roomId);
            createCompetitionParam.setGameConfigId(gameConfigId);
            createCompetitionParam.setCompetitionName(competitionName);
            Result<Boolean> booleanResult = competitionService.createCompetition(createCompetitionParam);
            if(ReturnCode.SUCCESS.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("result", booleanResult.getData());
            }
            else if(ReturnCode.ERROR_COMPETITION_ROOM_GAMING.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040802");
                return result;
            }
            else if(ReturnCode.ERROR_SHOW_MONEY.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040801");
                return result;
            }
            else if(ReturnCode.ERROR_ON_LIVE_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_GAME_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_PK_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_MATCH_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_INVITING_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_INVITED_STATUS.getCode().equals(booleanResult.getCode())||
                    ReturnCode.ERROR_ON_WAITING_LIVE_STATUS.getCode().equals(booleanResult.getCode())){
                result.addProperty("TagCode", "5107040803");
                return result;
            }
            else{
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
        catch (Exception e){
            logger.error("Error createCompetition()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

    /**
     * 51070410
     * 获取大奖赛配置信息
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listCompetitionConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try{
            Result<List<CompetitionConfigDTO>> competitionConfigs = competitionService.listCompetitionConfig();
            if(ReturnCode.SUCCESS.getCode().equals(competitionConfigs.getCode())){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("CompetitionConfigDTO",new Gson().toJsonTree(competitionConfigs.getData()));
            }
        }
        catch (Exception e){
            logger.error("Error listOfficalCompetition()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

}
