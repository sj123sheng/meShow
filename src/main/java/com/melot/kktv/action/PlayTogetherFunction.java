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
        roomObject.addProperty("hall_poster", "/poster/20170822/17/8668597_758655.png!256");
        roomObject.addProperty("title_poster", "/poster/20170822/17/8668597_758655.png!256");
        roomObject.addProperty("initialWeightValue", 10);
        roomObject.addProperty("showActorPoster", true);
        roomObject.addProperty("isTop", true);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3739);
        roomObject.addProperty("title", "开心抓娃娃");
        roomObject.addProperty("subTitle", "天天开心，天天抓");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.HAPPY_CATCH_DOLL);
        roomObject.addProperty("hall_poster", "/poster/20170907/22/104401023_4631990.jpg!256");
        roomObject.addProperty("title_poster", "/poster/20170907/22/104401023_4631990.jpg!256");
        roomObject.addProperty("initialWeightValue", 20);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3791);
        roomObject.addProperty("title", "欢乐跳舞机");
        roomObject.addProperty("subTitle", "谈恋爱不如跳舞");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.HAPPY_DANCE_MACHINE);
        roomObject.addProperty("hall_poster", "/poster/20171124/16/124044785_5457368.png!256");
        roomObject.addProperty("title_poster", "/poster/20171124/16/124044785_5457368.png!256");
        roomObject.addProperty("initialWeightValue", 30);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3787);
        roomObject.addProperty("title", "互动游戏");
        roomObject.addProperty("subTitle", "好玩到根本停不下来");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.INTERACTIVE_GAME);
        roomObject.addProperty("hall_poster", "/poster/20171013/16/127564984_1937127.jpg!256");
        roomObject.addProperty("title_poster", "/poster/20171013/16/127564984_1937127.jpg!256");
        roomObject.addProperty("initialWeightValue", 40);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3771);
        roomObject.addProperty("title", "情感电台");
        roomObject.addProperty("subTitle", "磁性酥音，多人语玩");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.EMOTIONAL_RADIO);
        roomObject.addProperty("hall_poster", "/poster/20171023/6/118282148_5928218.jpg!256");
        roomObject.addProperty("title_poster", "/poster/20171023/6/118282148_5928218.jpg!256");
        roomObject.addProperty("initialWeightValue", 50);
        roomObject.addProperty("showActorPoster", true);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3790);
        roomObject.addProperty("title", "全民PK");
        roomObject.addProperty("subTitle", "趣味视频对战，就是干");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.NATIONAL_PK);
        roomObject.addProperty("hall_poster", "/poster/20171119/21/132099080_955504.jpg!256");
        roomObject.addProperty("title_poster", "/poster/20171119/21/132099080_955504.jpg!256");
        roomObject.addProperty("initialWeightValue", 60);
        roomObject.addProperty("showActorPoster", false);
        roomObject.addProperty("isTop", false);
        roomArray.add(roomObject);

        roomObject = new JsonObject();
        roomObject.addProperty("cataId", 3781);
        roomObject.addProperty("title", "相亲交友");
        roomObject.addProperty("subTitle", "现场约会，小姐姐等撩");
        roomObject.addProperty("type", PlayTogetherCataTypeEnum.FRIENDSHIP_DATING);
        roomObject.addProperty("hall_poster", "/poster/20170728/12/115754956_2110452.png!256");
        roomObject.addProperty("title_poster", "/poster/20170728/12/115754956_2110452.png!256");
        roomObject.addProperty("initialWeightValue", 70);
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
