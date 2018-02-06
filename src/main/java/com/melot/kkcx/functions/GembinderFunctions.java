package com.melot.kkcx.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.melot.kkactivity.driver.service.GembinderService;
import com.melot.kkcore.account.service.AccountService;
import com.melot.kkcore.user.api.GameMoneyHistory;
import com.melot.kkcore.user.api.UserGameAssets;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.SecretKeyUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.opensymphony.xwork2.ActionContext;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2018/2/2.
 */
public class GembinderFunctions {

    /** 日志记录对象 */
    private Logger logger = Logger.getLogger(GembinderFunctions.class);

    private static final String key = "kkxiaoxiaole";

    private static final String openkey = "kktv5";

    @Resource
    private KkUserService kkUserService;

    @Resource
    private GembinderService gembinderService;

    @Resource
    private AccountService kkAccountService;

    public JsonObject encryptAccount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, null, 1, 500);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile.getIsActor() == 1){
            result.addProperty("TagCode", "40300000");
            return result;
        }

        try {
            String userIdString = SecretKeyUtil.encodeDES(Integer.toString(userId),key);
            String tokenString = encryptToken(token);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("userId", userIdString);
            result.addProperty("token", tokenString);
            return result;
        } catch (Exception e) {
            logger.error("【账户加密失败】userId="+userId+",token="+token,e);
            result.addProperty("TagCode", "5105030101");
            return result;
        }
    }

    public String getDiamonds(){

        ActionContext ctx = ActionContext.getContext();
        HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            logger.error("在action中从response获取writer时发生异常.", e);
            return null;
        }

        JsonObject result = new JsonObject();


        String userIdString = request.getParameter("userId");
        Integer userId = null;
        try {
            userId = getRealUserId(userIdString);
        }
        catch (Exception e){
            logger.error("【获取真实userId失败】userId"+userIdString, e);
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效，解密失败");
            out.println(result.toString());
            return null;
        }

        if(kkUserService == null){
            kkUserService = (KkUserService)MelotBeanFactory.getBean("kkUserService");
        }
        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile == null || (userProfile!= null&&userProfile.getIsActor()== 1)){
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效或者userId为主播");
            out.println(result.toString());
            return null;
        }

        String token = request.getParameter("token");
        if(!checkToken(userId,token)){
            result.addProperty("tagCode",2);
            result.addProperty("msg","token无效");
            out.println(result.toString());
            return null;
        }

        String time = request.getParameter("time");
        String sign = request.getParameter("sign");
        if(!checkSign(null,userIdString,token,Long.parseLong(time),sign)){
            result.addProperty("tagCode",3);
            result.addProperty("msg","签名无效");
            out.println(result.toString());
            return null;
        }
        UserGameAssets userGameAssets = kkUserService.getUserGameAssets(userId);
        if(userGameAssets != null){
            result.addProperty("tagCode",0);
            result.addProperty("diamonds",userGameAssets.getGameMoney());
            result.addProperty("msg","获取用户钻石数成功");
        }
        else {
            result.addProperty("tagCode",4);
            result.addProperty("msg","获取用户钻石数失败");
        }

        out.println(result.toString());
        return null;
    }

    public String startGame() {

        ActionContext ctx = ActionContext.getContext();
        HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            logger.error("在action中从response获取writer时发生异常.", e);
            return null;
        }

        JsonObject result = new JsonObject();

        String userIdString = request.getParameter("userId");
        Integer userId = null;
        try {
            userId = getRealUserId(userIdString);
        }
        catch (Exception e){
            logger.error("【获取真实userId失败】userId"+userIdString, e);
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效，解密失败");
            out.println(result.toString());
            return null;
        }
        if(kkUserService == null){
            kkUserService = (KkUserService)MelotBeanFactory.getBean("kkUserService");
        }
        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile == null || (userProfile!= null&&userProfile.getIsActor()== 1)){
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效或者userId为主播");
            out.println(result.toString());
            return null;
        }

        String token = request.getParameter("token");
        if(!checkToken(userId,token)){
            result.addProperty("tagCode",2);
            result.addProperty("msg","token无效");
            out.println(result.toString());
            return null;
        }

        String time = request.getParameter("time");
        String sign = request.getParameter("sign");
        long usedDiamonds = Long.parseLong(request.getParameter("usedDiamonds"));
        Map<String,Object> map = Maps.newHashMap();
        map.put("usedDiamonds",usedDiamonds);
        if(!checkSign(map,userIdString,token,Long.parseLong(time),sign)){
            result.addProperty("tagCode",3);
            result.addProperty("msg","签名无效");
            out.println(result.toString());
            return null;
        }

        GameMoneyHistory gameMoneyHistory = new GameMoneyHistory();
        gameMoneyHistory.setUserId(userId);
        gameMoneyHistory.setType(62);
        gameMoneyHistory.setDtime(new Date());
        gameMoneyHistory.setConsumeAmount((int)usedDiamonds);
        gameMoneyHistory.setProductDesc("消消乐游戏消费");
        UserGameAssets userGameAssets = kkUserService.decUserGameAssets(userId,usedDiamonds,gameMoneyHistory);
        if(userGameAssets !=null){
            if(gembinderService == null){
                gembinderService = (GembinderService)MelotBeanFactory.getBean("gembinderService");
            }
            Result<Long> histId = gembinderService.addGembinderHis(userId,(int)usedDiamonds);
            if(histId != null && histId.getCode() != null && histId.getCode().equals(CommonStateCode.SUCCESS)){
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("histId", histId.getData());
                result.addProperty("diamonds", userGameAssets.getGameMoney());
            }
            else {
                result.addProperty("tagCode",4);
                result.addProperty("msg","开始游戏失败");
                logger.error("【添加消消乐游戏记录失败，但钻石已扣，下面为返还】");
                GameMoneyHistory gameMoneyHistoryReturn = new GameMoneyHistory();
                gameMoneyHistoryReturn.setUserId(0);
                gameMoneyHistoryReturn.setToUserId(userId);
                gameMoneyHistoryReturn.setType(62);
                gameMoneyHistoryReturn.setDtime(new Date());
                gameMoneyHistoryReturn.setProductDesc("消消乐游戏开始失败返还扣除钻石");
                gameMoneyHistoryReturn.setIncomeAmount((int)usedDiamonds);
                UserGameAssets userGameAssetsReturn = kkUserService.incUserGameAssets(userId,usedDiamonds,gameMoneyHistoryReturn);
                if(userGameAssetsReturn !=null){
                    logger.error("【返还成功】");
                }
                else {
                    logger.error("【返还失败，请人工返还】userId = "+userId+",usedDiamonds="+usedDiamonds);
                }
            }
        }
        else {
            result.addProperty("tagCode",21);
            result.addProperty("msg","钻石余额不足");
        }
        out.println(result.toString());
        return null;
    }

    public String endGame() {

        ActionContext ctx = ActionContext.getContext();
        HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            logger.error("在action中从response获取writer时发生异常.", e);
            return null;
        }

        JsonObject result = new JsonObject();

        String userIdString = request.getParameter("userId");
        Integer userId = null;
        try {
            userId = getRealUserId(userIdString);
        }
        catch (Exception e){
            logger.error("【获取真实userId失败】userId"+userIdString, e);
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效，解密失败");
            out.println(result.toString());
            return null;
        }
        if(kkUserService == null){
            kkUserService = (KkUserService)MelotBeanFactory.getBean("kkUserService");
        }
        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile == null || (userProfile!= null&&userProfile.getIsActor()== 1)){
            result.addProperty("tagCode",1);
            result.addProperty("msg","userId无效或者userId为主播");
            out.println(result.toString());
            return null;
        }

        String token = request.getParameter("token");
        if(!checkToken(userId,token)){
            result.addProperty("tagCode",2);
            result.addProperty("msg","token无效");
            out.println(result.toString());
            return null;
        }

        String time = request.getParameter("time");
        String sign = request.getParameter("sign");
        long histId = Long.parseLong(request.getParameter("histId"));
        long getDiamonds = Long.parseLong(request.getParameter("getDiamonds"));
        Map<String,Object> map = Maps.newHashMap();
        map.put("histId",histId);
        map.put("getDiamonds",getDiamonds);
        if(!checkSign(map,userIdString,token,Long.parseLong(time),sign)){
            result.addProperty("tagCode",3);
            result.addProperty("msg","签名无效");
            out.println(result.toString());
            return null;
        }

        if(getDiamonds <= 0){
            UserGameAssets userGameAssets = kkUserService.getUserGameAssets(userId);
            if(userGameAssets != null){
                result.addProperty("tagCode",0);
                result.addProperty("diamonds",userGameAssets.getGameMoney());
                result.addProperty("msg","结束游戏成功");
            }
            else {
                logger.error("【结束游戏失败】histId="+histId+",getDiamonds="+getDiamonds);
                result.addProperty("tagCode",4);
                result.addProperty("msg","结束游戏失败");
            }
        }
        else{
            GameMoneyHistory gameMoneyHistory = new GameMoneyHistory();
            gameMoneyHistory.setUserId(0);
            gameMoneyHistory.setToUserId(userId);
            gameMoneyHistory.setType(62);
            gameMoneyHistory.setDtime(new Date());
            gameMoneyHistory.setProductDesc("消消乐游戏获得钻石");
            gameMoneyHistory.setIncomeAmount((int)getDiamonds);
            UserGameAssets userGameAssets = kkUserService.incUserGameAssets(userId,getDiamonds,gameMoneyHistory);
            if(userGameAssets != null){
                result.addProperty("diamonds", userGameAssets.getGameMoney());
                result.addProperty("msg","结束游戏成功");
                result.addProperty("tagCode",0);
                if(gembinderService == null){
                    gembinderService = (GembinderService)MelotBeanFactory.getBean("gembinderService");
                }
                Result<Boolean> updateResult = gembinderService.updateIncomeDiamond(histId,(int)getDiamonds);
                if(updateResult != null && updateResult.getCode() != null && updateResult.getCode().equals(CommonStateCode.SUCCESS)){
                    if(updateResult.getData()){

                    }
                    else {
                        logger.error("【更新消息乐游戏结果失败】histId="+histId+",getDiamonds="+getDiamonds);
                    }
                }
                else {
                    logger.error("【更新消息乐游戏结果失败】histId="+histId+",getDiamonds="+getDiamonds);
                }
            }
            else{
                logger.error("【结束游戏失败】histId="+histId+",getDiamonds="+getDiamonds);
                result.addProperty("tagCode",4);
                result.addProperty("msg","结束游戏失败");
            }
        }
        out.println(result.toString());
        return null;
    }


    private Integer getRealUserId(String userIdString) throws Exception {
        return Integer.parseInt(SecretKeyUtil.decodeDES(userIdString,key));
    }

    private String encryptToken(String token){
        if(StringUtil.strIsNull(token)){
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(token.substring(0,2)).append(CommonUtil.md5(token.substring(2)+key));
        return stringBuilder.toString();
    }

    private boolean checkToken(Integer userId,String checkTokenString){
        Integer otherApp = Integer.valueOf(checkTokenString.substring(1,2));
        if(kkAccountService == null){
            kkAccountService = (AccountService)MelotBeanFactory.getBean("kkAccountService");
        }
        String getToken = kkAccountService.getUserToken(userId, otherApp);
        return checkTokenString.equals(encryptToken(getToken));
    }

    private boolean checkSign(Map<String,Object> parms,String userId,String token,Long time,String ckeckSignString){
        StringBuilder stringBuilder = new StringBuilder();
        if(parms!= null && parms.size() != 0){
            Set<String> ks = parms.keySet();
            String[] kss = new String[ks.size()];
            ks.toArray(kss);
            Arrays.sort(kss, String.CASE_INSENSITIVE_ORDER);
            for(int i = 0; i < kss.length; ++i) {
                Object o = parms.get(kss[i]);
                if(o != null) {
                    stringBuilder.append(o.toString());
                }
            }
        }
        stringBuilder.append(userId).append(token).append(time).append(openkey);
        return ckeckSignString.equals(CommonUtil.md5(stringBuilder.toString()).toUpperCase());
    }



}
