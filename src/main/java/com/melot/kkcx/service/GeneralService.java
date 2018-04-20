package com.melot.kkcx.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.chat.domain.PrivateLetter;
import com.melot.chat.service.ChatAnalyzerService;
import com.melot.content.config.domain.ReportFlowRecord;
import com.melot.content.config.report.service.ReportFlowRecordService;
import com.melot.kktv.domain.SmsConfig;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.sms.api.domain.ConfigSms;
import com.melot.sms.api.service.SmsService;

/**
 * 通用功能服务
 * @author Administrator
 *
 */
public class GeneralService {
	
	private static Logger logger = Logger.getLogger(GeneralService.class);
	
	/**
	 * 获取短信内容
	 * @param userId
	 * @param token
	 * @return
	 */
	public static SmsConfig getSmsMsgFormat(int appId, int channel, int platform, int smsType) {
	    
	    SmsService smsService = MelotBeanFactory.getBean("smsService", SmsService.class);
        if (smsService != null) {
            ConfigSms configSms = smsService.getSmsConfig(appId,smsType,platform,null);
            if (configSms != null) {
                SmsConfig smsConfig = new SmsConfig();
                BeanUtils.copyProperties(configSms, smsConfig);
                return smsConfig;
            }
        }
        return null;
	}
	
	/**
	 * 房间举报
	 * @param userId
	 * @param nickname
	 * @param toUserId
	 * @param toNickname
	 * @param reportType
	 * @param userType
	 * @param reason
	 * @param evidenceUrls
	 * @return
	 */
	public static Integer roomReport(int userId, String nickname, int toUserId,
			String toNickname, int reportType, int userType, String reason,
			String evidenceUrls) {
		
		Integer reportId = null;
		
		ReportFlowRecord reportFlowRecord = new ReportFlowRecord();
		reportFlowRecord.setUserId(userId);
		reportFlowRecord.setUserName(nickname);
		reportFlowRecord.setBeUserId(toUserId);
		reportFlowRecord.setBeUserName(toNickname);
		reportFlowRecord.setReportType(reportType);
		reportFlowRecord.setRoomType(userType);
		reportFlowRecord.setReportReason(reason);
		reportFlowRecord.setEvidenceUrls(evidenceUrls);
		reportFlowRecord.setReportAmount(Constant.report_money);
		reportFlowRecord.setAwardAmount(Constant.award_money);
		
		try {
			ReportFlowRecordService reportFlowRecordService = MelotBeanFactory
					.getBean("reportFlowRecordService", ReportFlowRecordService.class);
			reportId = reportFlowRecordService.saveReportFlowRecord(reportFlowRecord);
	    } catch (Exception e) {
	    	logger.error("Fail to call reportFlowRecordService.saveReportFlowRecord ", e);
	    }
		 
		return reportId;
	}
	
	/**
	 * 是否合法appId
	 * @param appId 
	 * @return
	 */
	public static boolean isLegalAppId (int appId) {
		if (appId == AppIdEnum.AMUSEMENT || appId == AppIdEnum.GAME 
				|| appId == AppIdEnum.HZJHA || appId == AppIdEnum.KAIBO) {
			return true;
		} 
		return false;
	}
	
    /**
     * 发送跑道消息
     * @param type
     * @param msg
     * @return
     */
    public static boolean sendRunwayMsg (int type, JsonObject msg) {
        boolean result = false;
        
        HttpURLConnection url_con = null;
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("MsgTag", "50010102");
            
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(msg);
            jsonObject.add("MsgList", jsonArray);
            
            String queryParams = "?type=" + type + "&msg=" + URLEncoder.encode(jsonObject.toString(), "utf-8");
            logger.info("GeneralService sendRunwayMsg request: " + ConfigHelper.getRunwayUrl() + queryParams);
            URL url = new URL(ConfigHelper.getRunwayUrl() + queryParams);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            logger.info("GeneralService sendRunwayMsg response: " + tempStr);
            
            rd.close();
            in.close();
            
            result = true;
        } catch (Exception e) {
            logger.error("房间跑道消息推送请求异常", e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
        
        return result;
    }
    
    /**
     * 发送消息到房间
     * @param type
     * @param msg
     * @return
     */
    public static boolean sendMsgToRoom (int type, int roomId, int userId, int platform, JsonObject msg) {
        boolean result = false;
        
        HttpURLConnection url_con = null;
        try {
            StringBuffer queryParamsBuffer = new StringBuffer("?type=" + type + "&msg=" + URLEncoder.encode(msg.toString(), "utf-8"));
            if (roomId > 0) {
                queryParamsBuffer.append("&roomId=" + roomId);
            }
            if (userId > 0) {
                queryParamsBuffer.append("&userId=" + userId);
            }
            if (platform > 0) {
                queryParamsBuffer.append("&platform=" + platform);
            }
            String queryParams = queryParamsBuffer.toString();
            logger.info("GeneralService sendRunwayMsg request: " + ConfigHelper.getRunwayUrl() + queryParams);
            URL url = new URL(ConfigHelper.getRunwayUrl() + queryParams);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            logger.info("GeneralService sendRunwayMsg response: " + tempStr);
            
            rd.close();
            in.close();
            
            result = true;
        } catch (Exception e) {
            logger.error("房间跑道消息推送请求异常", e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
        
        return result;
    }

    /**
     * 发送消息到房间
     * @param type
     * @param msg
     * @param appId
     * @return
     */
    public static boolean sendMsgToRoom (int type, int roomId, int userId, int platform, int appId, String msg) {
        boolean result = false;
        
        HttpURLConnection url_con = null;
        try {
            StringBuffer queryParamsBuffer = new StringBuffer("?type=" + type + "&msg=" + URLEncoder.encode(msg, "utf-8"));
            if (roomId > 0) {
                queryParamsBuffer.append("&roomId=" + roomId);
            }
            if (userId > 0) {
                queryParamsBuffer.append("&userId=" + userId);
            }
            if (platform > 0) {
                queryParamsBuffer.append("&platform=" + platform);
            }
            if (appId > 0) {
                queryParamsBuffer.append("&appId=" + appId);
            }
            String queryParams = queryParamsBuffer.toString();
            logger.info("GeneralService sendRunwayMsg request: " + ConfigHelper.getRunwayUrl() + queryParams);
            URL url = new URL(ConfigHelper.getRunwayUrl() + queryParams);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            logger.info("GeneralService sendRunwayMsg response: " + tempStr);
            
            rd.close();
            in.close();
            
            result = true;
        } catch (Exception e) {
            logger.error("房间跑道消息推送请求异常", e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
        
        return result;
    }
 
    
    public static void main(String args[]) {
        JsonObject msg = new JsonObject();
        msg.addProperty("MsgTag", 30000001);
        msg.addProperty("content", "测试测试测试测试测试测试测试");
        
        String urlAddress = "http://10.0.3.8:8080/";
        
        HttpURLConnection url_con = null;
        try {
            StringBuffer queryParamsBuffer = new StringBuffer("?type=" + 2 + "&msg=" + URLEncoder.encode(msg.toString(), "utf-8"));
            if (1193456 > 0) {
                queryParamsBuffer.append("&roomId=" + 1193456);
            }
            if (1000080 > 0) {
                queryParamsBuffer.append("&userId=" + 1000080);
            }
            String queryParams = queryParamsBuffer.toString();
            logger.info("GeneralService sendRunwayMsg request: " + urlAddress + queryParams);
            URL url = new URL(urlAddress + queryParams);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            logger.info("GeneralService sendRunwayMsg response: " + tempStr);
            
            rd.close();
            in.close();
            
        } catch (Exception e) {
            logger.error("房间跑道消息推送请求异常", e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
    }
	
    /**
     * 根据ip地址获取所在城市及省份 默认北京
     * @param ipAddr
     * @return eg. {"city":43,"area":1}
     */
	public static Map<String, Integer> getIpCity(String ipAddr) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("city", 43);
		map.put("area", 1);
		Integer cityId = CityUtil.getCityIdByIpAddr(ipAddr);
		if (cityId != null) {
			Integer parentId = CityUtil.getParentCityId(cityId);
			if (parentId != null) {
				map.put("city", cityId);
				map.put("area", parentId);
				return map;
			}
		}
 		return map;
     }
    
	/**
	 * 根据城市名称获取城市id
	 * @param cityName
	 * @param continentName
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static Map<String, Integer> getCityIdByCityName(String districtName, String cityName, String continentName) {
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("districtName", districtName);
	    map.put("cityName", cityName);
	    map.put("continentName", continentName);
	    try {
	        return (Map<String, Integer>) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getCityIdByCityName", map);
        } catch (SQLException e) {
            logger.error("Fail to execute getCityIdByCityName, cityName: " + cityName + ", continentName: " + continentName, e);
        }
	    return null;
	}
	
	/**
     * 根据省市名称获取省市id
     * @param provinceName
     * @return
     */
    public static Integer getProvinceIdByProvinceName(String provinceName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("provinceName", provinceName);
        try {
            return (Integer) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getProvinceId", map);
        } catch (SQLException e) {
            logger.error("Fail to execute getProvinceIdByProvinceName, provinceName: " + provinceName, e);
        }
        return null;
    }
    
    /**
     * 根据地区名称及上级区域id获取地区id
     * @param districtName
     * @param parentId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDistrictIdByDistrictName(String districtName, String cityName, int parentId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("districtName", districtName);
        map.put("cityName", cityName);
        map.put("parentId", parentId);
        try {
            return (Map<String, Object>) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getDistricId", map);
        } catch (SQLException e) {
            logger.error("Fail to execute getProvinceIdByProvinceName, districtName: " + districtName, e);
        }
        return null;
    }
    
    /**
     * 替换字符串中的敏感词为* 
     */
    public static String replaceSensitiveWords(int userId, String word){
        if (!StringUtil.strIsNull(word)) {
            try {
                ChatAnalyzerService chatAnalyzerService = (ChatAnalyzerService) MelotBeanFactory.getBean("chatAnalyzerService");
                if (chatAnalyzerService != null) {
                    return chatAnalyzerService.checkPhraseAndReturnDetails(userId, 0, word).getNewWord();
                }
            } catch (Exception e) {
                logger.error("ChatAnalyzerService.checkPhrase(" + userId + ", " + word + ") execute exception.", e);
            }
        }
        
        return word;
    }
    
    /**
     * 判断字符串中是否包含敏感词 
     */
    public static boolean hasSensitiveWords(int userId, String word){
        if (!StringUtil.strIsNull(word)) {
            try {
                ChatAnalyzerService chatAnalyzerService = (ChatAnalyzerService) MelotBeanFactory.getBean("chatAnalyzerService");
                if (chatAnalyzerService != null) {
                    PrivateLetter privateLetter = chatAnalyzerService.checkPhraseAndReturnDetails(userId, 0, word);
                    return StringUtil.strIsNull(privateLetter.getSensitiveWord());
                }
            } catch (Exception e) {
                logger.error("ChatAnalyzerService.checkPhrase(" + userId + ", " + word + ") execute exception.", e);
            }
        }
        
        return false;
    }

	/**
	 * 根据cityID获取地址信息
	 * @param cityId
	 * @return e.g. {"city":43,"area":1}
	 */
	public static Map<String, Integer> getCityByCityId(Integer cityId) {
		if (cityId == null || cityId == 0) {
			return null;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		if (!isValidCity(cityId)) {
			cityId = 1; // 默认城市 北京：1
		}
		
		Integer parentId = CityUtil.getParentCityId(cityId);
		map.put("city", cityId);
		map.put("area", parentId);
		return map;
	}
	
	/**
	 * 判断cityID在数据库是否合法
	 * @param cityId
	 * @return
	 */
	public static boolean isValidCity(Integer cityId) {
		if (cityId == null) {
			return false;
		}
		return CityUtil.isValidCity(Math.abs(cityId));
	}
}
