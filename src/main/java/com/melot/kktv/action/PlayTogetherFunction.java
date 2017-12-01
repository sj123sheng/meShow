package com.melot.kktv.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.kk.doll.api.constant.DollMachineStatusEnum;
import com.melot.kk.doll.api.domain.DO.RedisDollMachineDO;
import com.melot.kk.doll.api.service.DollMachineService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlayTogetherCataTypeEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.commons.io.Charsets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: PlayTogetherFunction
 * <p>
 * Description:一起玩大厅相关接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年10月12日
 */
public class PlayTogetherFunction {

    private static Logger logger = Logger.getLogger(PlayTogetherFunction.class);

    @Autowired
    private ConfigService configService;

    private static String encode = Charsets.ISO_8859_1.name();

    private static String decode = Charsets.UTF_8.name();

    public static void main(String[] args) {

        JsonArray roomArray = new JsonArray();

        JsonObject roomObject = new JsonObject();
        roomObject.addProperty("cataId", 0);
        roomObject.addProperty("title", "1V1视频");
        roomObject.addProperty("subTitle", "与心仪的TA视频畅聊");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.SINGLE_CHAT);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/single_chat.png");
        roomObject.addProperty("title_poster", "/poster/offical/cata/single_chat.png");
        roomObject.addProperty("initialWeightValue", 7);
        roomObject.addProperty("showActorPoster", true);
        roomObject.addProperty("isTop", true);
        roomArray.add(roomObject);


        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3791);
        roomObject.addProperty("title", "欢乐跳舞机");
        roomObject.addProperty("subTitle", "用你的表情来跳舞");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.HAPPY_DANCE_MACHINE);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/dance_machine.png");
        roomObject.addProperty("title_poster", "/poster/offical/dance.jpg");
        roomObject.addProperty("initialWeightValue", 8);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3739);
        roomObject.addProperty("title", "开心抓娃娃");
        roomObject.addProperty("subTitle", "直播抓娃娃 包邮送到家");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.HAPPY_CATCH_DOLL);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/catch_doll.png");
        roomObject.addProperty("title_poster", "/poster/offical/doll.jpg");
        roomObject.addProperty("initialWeightValue", 6);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3787);
        roomObject.addProperty("title", "互动游戏");
        roomObject.addProperty("subTitle", "多人联网玩游戏");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.INTERACTIVE_GAME);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/interactive_game.png");
        roomObject.addProperty("title_poster", "/poster/offical/cata/interactive_game.jpg");
        roomObject.addProperty("initialWeightValue", 5);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3790);
        roomObject.addProperty("title", "欢乐PK");
        roomObject.addProperty("subTitle", "刺激匹配 下一个还有谁");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.NATIONAL_PK);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/national_pk.png");
        roomObject.addProperty("title_poster", "/poster/offical/cata/national_pk.png");
        roomObject.addProperty("initialWeightValue", 4);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3771);
        roomObject.addProperty("title", "情感电台");
        roomObject.addProperty("subTitle", "声控声优的聚集地");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.EMOTIONAL_RADIO);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/emotional_radio.png");
        roomObject.addProperty("initialWeightValue", 3);
        roomObject.addProperty("showActorPoster", true);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3781);
        roomObject.addProperty("title", "相亲交友");
        roomObject.addProperty("subTitle", "虚拟相亲 真实交友");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.FRIENDSHIP_DATING);
        roomObject.addProperty("hall_poster", "/poster/offical/cata/friendship_dating.png");
        roomObject.addProperty("initialWeightValue", 1);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        System.out.println("roomArray: " + roomArray);

        String playTogetherConfig = new Gson().toJson(roomArray);
        JsonArray roomArray1 = new JsonParser().parse(playTogetherConfig).getAsJsonArray();
        System.out.println("roomArray1: " + roomArray1);
    }

    /**
     * 获取一起玩大厅栏目列表【51070201】
     */
    public JsonObject getTitleList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            String playTogetherConfig = new String(configService.getPlayTogetherConfig().getBytes(encode), decode);
            JsonArray roomArray = new JsonParser().parse(playTogetherConfig).getAsJsonArray();

            result.add("cataList", roomArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getTitleList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取抓娃娃栏目下的房间列表【51070202】
     */
    public JsonObject getCatchDollRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int cataId, platform, pageIndex, countPerPage;
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            int start = (pageIndex <= 1 ? 0 : pageIndex - 1) * countPerPage;
            FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
            SysMenu sysMenu = firstPageHandler.getPartList(cataId, null, null, start, countPerPage);
            JsonArray roomArray = new JsonArray();
            List<RoomInfo> roomList = sysMenu.getRooms();
            if (roomList != null) {
                for (RoomInfo roomInfo : roomList) {

                    JsonObject roomObject = RoomTF.roomInfoToJson(roomInfo, platform);

                    // 获取直播间娃娃机的状态和直播间抓中娃娃的总数
                    int roomId = roomInfo.getActorId();
                    Result<RedisDollMachineDO> redisDollMachineDOResult = dollMachineService.getRedisDollMachineDO(roomId);
                    RedisDollMachineDO redisDollMachineDO;
                    if(redisDollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                        redisDollMachineDO = redisDollMachineDOResult.getData();
                    }else {
                        result.addProperty("TagCode", "5110903");
                        return result;
                    }
                    Integer dollMachineStatus = redisDollMachineDO.getStatus();
                    if(dollMachineStatus == null) {
                        dollMachineStatus = DollMachineStatusEnum.Ready;
                    }else if(dollMachineStatus == DollMachineStatusEnum.Wait_Coin) {
                        dollMachineStatus = DollMachineStatusEnum.Play;
                    }
                    roomObject.addProperty("dollMachineStatus", dollMachineStatus);
                    roomObject.addProperty("graspCount", redisDollMachineDO.getGraspDollCount());

                    roomArray.add(roomObject);
                }
            }

            int roomTotal = sysMenu.getRoomCount().intValue();

            result.addProperty("roomTotal", roomTotal);
            result.add("roomList", roomArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCatchDollRoomList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

}
