package com.melot.kktv.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.content.config.domain.GalleryInfo;
import com.melot.content.config.domain.GalleryOrderRecord;
import com.melot.content.config.facepack.service.GalleryInfoService;
import com.melot.content.config.facepack.service.GalleryOrderRecordService;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kktv.service.ConsumeService;
import com.melot.kktv.service.StatisticsServices;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 用户消费相关接口
 * @author RC
 *
 */
public class ConsumAction {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ConsumAction.class);
    
    private static Logger showMoneyLogger = Logger.getLogger("showMoneyLogger");

}
