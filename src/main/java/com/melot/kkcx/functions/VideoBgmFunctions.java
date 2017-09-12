package com.melot.kkcx.functions;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.common.driver.base.Result;
import com.melot.common.driver.base.ResultCode;
import com.melot.common.driver.domain.VideoBgmPage;
import com.melot.common.driver.queryOption.VideoBgmQueryOption;
import com.melot.common.driver.service.VideoBgmService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Title:
 * <p>
 * Description:动态视频音乐相关接口
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2017/9/8.
 */
public class VideoBgmFunctions {
    /** 日志记录对象 */
    private static Logger logger = Logger.getLogger(VideoBgmFunctions.class);

    /**
     * 51100101
     */
    public JsonObject getVideoBgmList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        String tagCode_prefix = "51100101";
        JsonObject result = new JsonObject();
        int pageNum = 0;
        int pageSize = 0;
        try {
            pageNum = CommonUtil.getJsonParamInt(jsonObject, "pageNum", 1,null, 1, Integer.MAX_VALUE);
            pageSize = CommonUtil.getJsonParamInt(jsonObject, "pageSize", 0,null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        VideoBgmQueryOption videoBgmQueryOption = new VideoBgmQueryOption();
        videoBgmQueryOption.setStatus(1);
        if(pageNum != 0 && pageSize != 0){
            videoBgmQueryOption.setStart((pageNum - 1) * pageSize);
            videoBgmQueryOption.setNum(pageSize);
        }
        try{
            VideoBgmService videoBgmService = (VideoBgmService) MelotBeanFactory.getBean("videoBgmService");
            Result<VideoBgmPage> page =  videoBgmService.getVideoBgmsByOption(videoBgmQueryOption);
            if (page.getCode().equals(ResultCode.SUCCESS)) {
                result.add("videoBgms", new Gson().toJsonTree(page.getData().getVideoBgms()));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("totalCount",page.getData().getTotalCount());
                result.addProperty("videoURL", ConfigHelper.getVideoURL());
            }
            else {
                result.addProperty("TagCode", tagCode_prefix + "01");
            }
            return result;
        }
        catch (Exception e){
            logger.error("【VideoBgmFunctions.getVideoBgmList fail】",e);
            result.addProperty("TagCode", tagCode_prefix + "01");
            return result;
        }
    }

}
