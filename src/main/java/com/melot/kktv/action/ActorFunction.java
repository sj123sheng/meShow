package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.share.driver.domain.RankData;
import com.melot.share.driver.service.ShareActivityService;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: ActorFunction
 * <p>
 * Description: 主播相关API接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年8月04日
 */
public class ActorFunction {

    private static Logger logger = Logger.getLogger(ActorFunction.class);

    /**
     * 获取主播代言团列表【51020101】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject getRepresentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        // 定义返回结果
        JsonObject result = new JsonObject();
        
        int actorId, userId;
        
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取主播列表
        try {
            ShareActivityService shareActivityService = MelotBeanFactory.getBean("shareActivityService", ShareActivityService.class);
            List<RankData> rankDataList = shareActivityService.getRankList(userId > 0 ? userId : null, actorId);
            
            if (CollectionUtils.isEmpty(rankDataList)) {
                /*result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;*/
                RankData rankData1 = new RankData();
                rankData1.setUserId(69257104);
                rankData1.setNickName("小西西 ღ 头疼啊啊");
                rankData1.setGender(1);
                rankData1.setPortrait("/portrait/20160917/11/69257104_4030735.jpg");
                rankData1.setRank(1);
                rankData1.setUserCount(100);
                rankData1.setShareTimes(205);
                rankData1.setUserUv(360);

                RankData rankData2 = new RankData();
                rankData2.setUserId(104357526);
                rankData2.setNickName("゛NA 娜拉");
                rankData2.setGender(1);
                rankData2.setPortrait("/portrait/20170730/11/104357526_4120897.jpg");
                rankData2.setRank(2);
                rankData2.setUserCount(80);
                rankData2.setShareTimes(105);
                rankData2.setUserUv(160);

                RankData rankData3 = new RankData();
                rankData3.setUserId(100803678);
                rankData3.setNickName("KK最丑需要时你在哪");
                rankData3.setGender(1);
                rankData3.setPortrait("/portrait/20170807/0/100803678_5217117.jpg");
                rankData3.setRank(3);
                rankData3.setUserCount(30);
                rankData3.setShareTimes(45);
                rankData3.setUserUv(56);

                rankDataList.add(0,rankData1);
                rankDataList.add(1,rankData2);
                rankDataList.add(2,rankData3);

                for (int i = 4; i < 9; i++) {
                    RankData rankData = new RankData();
                    rankData.setUserId(89705961);
                    rankData.setNickName("大 韵 韵 ♡");
                    rankData.setGender(0);
                    rankData.setPortrait("/portrait/20170802/10/89705961_215146.png");
                    rankData.setRank(i);
                    rankData.setUserCount(10-i);
                    rankData.setShareTimes(10-i);
                    rankData.setUserUv(10-i);
                    rankDataList.add(i-1,rankData);
                }

                RankData rankData15 = new RankData();
                rankData15.setUserId(90631785);
                rankData15.setNickName("甜橙\uD83C\uDF3B冉冉冉冉冉冉");
                rankData15.setGender(1);
                rankData15.setPortrait("/portrait/20170730/12/90631785_158709.jpg");
                rankData15.setRank(15);
                rankData15.setUserCount(1);
                rankData15.setShareTimes(1);
                rankData15.setUserUv(0);
                rankDataList.add(8,rankData15);
            }
            
            JsonArray representList = new JsonArray();

            int rankDataSize = rankDataList.size();
            for (int i = 0; i < rankDataSize; i++) {

                RankData rankData = rankDataList.get(i);
                JsonObject representJson = new JsonObject();
                representJson.addProperty("userId", rankData.getUserId());
                representJson.addProperty("nickname", rankData.getNickName());
                representJson.addProperty("gender", rankData.getGender());
                representJson.addProperty("ranking", rankData.getRank());
                String identity = "";
                if(rankData.getRank() != null) {
                    switch (rankData.getRank()) {
                        case 1:
                            identity = "团长";
                            break;
                        case 2:
                            identity = "副团长";
                            break;
                        case 3:
                            identity = "副团长";
                            break;
                        default:
                            break;
                    }
                }

                representJson.addProperty("identity", identity);

                representJson.addProperty("absorbFansCount", rankData.getUserCount());
                representJson.addProperty("shareCount", rankData.getShareTimes());
                representJson.addProperty("onlookersCount", rankData.getUserUv());

                if (rankData.getPortrait() != null) {
                    String portraitAddress = rankData.getPortrait();
                    representJson.addProperty("portrait_path_original", portraitAddress);
                    representJson.addProperty("portrait_path_48", portraitAddress + "!48");
                    representJson.addProperty("portrait_path_128", portraitAddress + "!128");
                    representJson.addProperty("portrait_path_256", portraitAddress + "!256");
                    representJson.addProperty("portrait_path_272", portraitAddress + "!272");
                    representJson.addProperty("portrait_path_1280", portraitAddress + "!1280");
                    representJson.addProperty("portrait_path_400", portraitAddress + "!400");
                    representJson.addProperty("portrait_path_756", portraitAddress + "!756x567");
                }

                if(userId > 0 && i == rankDataSize - 1) {
                    result.add("mySelfRepresent", representJson);
                }else {
                    representList.add(representJson);
                }

            }


            result.add("representList", representList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Module Error ShareActivityService.getRankList(" + actorId + ", " + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }
    
    /**
     * 更新H5页面上的 UV 数据 (UV：独立访问用户数)【51020102】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject updateUV(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();

        int appId, actorId, userId;
        String deviceUId;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", "", null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        // 更新UV
        try {
            ShareActivityService shareActivityService = MelotBeanFactory.getBean("shareActivityService", ShareActivityService.class);
            String clientIP = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null);
            boolean updateUVResult = shareActivityService.recordUv(userId, actorId, clientIP, deviceUId);

            result.addProperty("updateUVResult", updateUVResult);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Module Error ShareActivityService.getRankList(" + actorId + ", " + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}
