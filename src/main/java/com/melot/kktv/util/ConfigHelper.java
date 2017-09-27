package com.melot.kktv.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 常量帮助类
 * 
 * @author liyue
 * 
 */
public class ConfigHelper {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ConfigHelper.class);

	private static String roomLiveRecordUrl;
	private static String recoderServerAddress;
	
	private static String kkApiAddress = "";
	private static int appId = 0;

    private static String httpdir = "";
	
	private static String zkServices = "";
	
	private static String mediahttpdir = "";
	private static String mfsURL = "";
	private static String mcmURL = "";

	private static int vip0 = 0;
	private static int vip0kickout = 0;
	private static int vip0shutup = 0;
	private static int vip1 = 0;
	private static int vip1kickout = 0;
	private static int vip1shutup = 0;

	private static String versionSavePath = "";

	private static String crashSavePath = "";

	private static String parkCarResURL = "";
	private static String parkLogoResURL = "";
	private static String vipResURL = "";
	private static String vipWebResURL = "";

	private static String parkCarAndroidResURL = "";
	private static String parkLogoAndroidResURL = "";
	private static String vipAndroidResURL = "";

	private static String giftIconAndroidResURL = "";
	private static String giftGifAndroidResURL = "";
	private static String giftIconIphoneResURL = "";
	private static String giftGifIphoneResURL = "";
	private static String giftSmallIconAndroidResURL = "";
	private static String giftSmallIconIphoneResURL = "";
	
	private static Set<String> allowedPayClientIp;
	private static Set<String> allowedRegisterClientIp;
	
	private static String upVersion;
	private static String upMerchantId;
	private static String upResponseCodeSuccess;
	private static String upSecurityKey;
	private static String upOrderDescription;
	private static String upBackendUrl;
	private static String upTradeUrl;
	private static String upQueryUrl;
	
	private static String activityUrl;
	private static String noticeUrl;

	private static int giftCompeteRefreshIntervel;
	
	private static int roomFansRankingIntervel;
	
	private static int redisUserDataExpireTime = 0;
	
	private static boolean recordCallInterface = false;

	private static String appleStoreVerifyUrl;
	private static Map<String, Integer> iphoneChargeProduct;
	private static Map<String, Integer> ipadChargeProduct;
	private static Map<String, Integer> checkinReward;
	
	private static String notifyWebUrl;

	private static int redisSecurityUUIDExpireTime = 0;
	private static int clientSecurityUUIDActiveTime = 0;
	
	private static String desSecurityKey;
	
	private static int periodGiftRankingExpireTime = 0;

	private static int periodFamilyRoomRefreshExpireTime = 0;
	private static int familyMatchCacheRefreshExpireTime = 0;
	
	private static char[] webSecKey0;
	private static char[] webSecKey1;

	//message system config
	private static long initLastReadTime;
	private static long cycleEffRedisActIds;
	private static long cycleGenAct;
	
	//rookie logo config
	private static String webRookieLogo;
	private static String androidRookieLogo;
	private static String iphoneRookieLogo;

	//weekly logo config
	private static String webWeeklyLogo;
	private static String androidWeeklyLogo;
	private static String iphoneWeeklyLogo;
	
	private static int newsRewardRankRefreshIntervel;
	
	private static List<String> chinaUnicomFreeFlowIp;
	private static String chinaUnicomCpid;
	private static String chinaUnicomPassword;
	private static String chinaUnicomSpid;
	private static String chinaUnicomPid;
	private static String chinaUnicomPortalid;
	
	// 七牛
	private static String videoURL;
	private static String domainURL;
	private static String bucket;
	private static String accessKey;
	private static String secretKey;
	
	// 推荐动态加入的条数
	private static int recNewsAddedCount;
	
	// 此程序是否服务多个 APP
	private static int moreAppFlag;
	
	// 年度盛典结果信息
	private static String annualActor;
	private static String annualFamily;
	
	// 房间跑道地址
	private static String runwayUrl;
	
	// 用户送阳光中奖类别
	private static String sendSunshinePrizeType;
	
	
	private static String kkgameActivity;
	
	//新增勋爵徽章id
	private static Integer firstChargeMedal;

	private static int giftRankPosition;
	
	private static int followStaticMax;
	
	private static  String accountType;
    private static String sdk_appid;
    private static String expire_after;
    
    /**
     * KK自己维护的资源服务器域名
     */
    private static String kkDomain = "";
    
	@SuppressWarnings("unchecked")
    public static void initConfig(String path) {

		FileInputStream fis = null;
		SAXBuilder saxBuiler = new SAXBuilder();
		// 加载配置文件
		try {
			fis = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			logger.error("未找到配置文件config.xml!", e);
		}
		try {
			Document doc = saxBuiler.build(fis);
			Element root = doc.getRootElement();
			giftRankPosition = Integer.parseInt(root.getChildText("giftRankPosition"));
			kkgameActivity = root.getChildText("kkgameActivity");
			roomLiveRecordUrl = root.getChildText("roomLiveRecordUrl");
			recoderServerAddress = root.getChildText("recoderServerAddress");
			kkApiAddress = root.getChildText("kkApiAddress");
			appId = Integer.parseInt(root.getChildText("appId"));
			httpdir = root.getChildText("httpdir");
			mediahttpdir = root.getChildText("mediahttpdir");
			
			zkServices = root.getChildText("zkServices");
			
			mfsURL = root.getChildText("mfsURL");
			mcmURL = root.getChildText("mcmURL");
			
			vip0 = Integer.parseInt(root.getChildText("vip0"));
			vip0kickout = Integer.parseInt(root.getChildText("vip0kickout"));
			vip0shutup = Integer.parseInt(root.getChildText("vip0shutup"));
			vip1 = Integer.parseInt(root.getChildText("vip1"));
			vip1kickout = Integer.parseInt(root.getChildText("vip1kickout"));
			vip1shutup = Integer.parseInt(root.getChildText("vip1shutup"));

			parkCarResURL = root.getChildText("parkCarResURL");
			parkLogoResURL = root.getChildText("parkLogoResURL");
			vipResURL = root.getChildText("vipResURL"); 
			vipWebResURL = root.getChildText("vipWebResURL"); 

			parkCarAndroidResURL = root.getChildText("parkCarAndroidResURL");
			parkLogoAndroidResURL = root.getChildText("parkLogoAndroidResURL");
			vipAndroidResURL = root.getChildText("vipAndroidResURL");

			giftIconAndroidResURL = root.getChildText("giftIconAndroidResURL");
			giftGifAndroidResURL = root.getChildText("giftGifAndroidResURL");
			giftIconIphoneResURL = root.getChildText("giftIconIphoneResURL");
			giftGifIphoneResURL = root.getChildText("giftGifIphoneResURL");
			giftSmallIconAndroidResURL = root.getChildText("giftSmallIconAndroidResURL");
			giftSmallIconIphoneResURL = root.getChildText("giftSmallIconIphoneResURL");
			
			versionSavePath = root.getChildText("versionSavePath");

			crashSavePath = root.getChildText("crashSavePath");
			
			allowedPayClientIp = new HashSet<String>();
			List<Element> children = root.getChildren("allowedPayClientIp");
			for (Element element : children) {
				allowedPayClientIp.add(element.getTextTrim());
			}
			
			allowedRegisterClientIp = new HashSet<String>();
			children = root.getChildren("allowedRegisterClientIp");
			for (Element element : children) {
				allowedRegisterClientIp.add(element.getTextTrim());
			}
			
			upVersion = root.getChildText("upVersion");
			upMerchantId = root.getChildText("upMerchantId");
			upSecurityKey = root.getChildText("upSecurityKey");
			upResponseCodeSuccess = root.getChildText("upResponseCodeSuccess");
			upBackendUrl = root.getChildText("upBackendUrl");
			upTradeUrl = root.getChildText("upTradeUrl");
			upQueryUrl = root.getChildText("upQueryUrl");
			upOrderDescription = root.getChildText("upOrderDescription");
			
			activityUrl = root.getChildText("activityUrl");
			noticeUrl = root.getChildText("noticeUrl");

			giftCompeteRefreshIntervel = Integer.parseInt(root.getChildText("giftCompeteRefreshIntervel"));
			
			roomFansRankingIntervel = Integer.parseInt(root.getChildText("roomFansRankingIntervel"));
			
			redisUserDataExpireTime = Integer.parseInt(root.getChildText("redisUserDataExpireTime"));
			
			recordCallInterface = Boolean.parseBoolean(root.getChildText("recordCallInterface"));
			
			appleStoreVerifyUrl = root.getChildText("appleStoreVerifyUrl");
			
			iphoneChargeProduct = new HashMap<String, Integer>();
			Element iphocProductElement = root.getChild("iphoneChargeProduct");
			List<Element> iphocProductChildren = iphocProductElement.getChildren("product");
			for (Element element : iphocProductChildren) {
				String key = element.getAttributeValue("id");
				Integer value = Integer.parseInt(element.getTextTrim());
				iphoneChargeProduct.put(key, value);
			}
			
			ipadChargeProduct = new HashMap<String, Integer>();
			Element ipadcProductElement = root.getChild("ipadChargeProduct");
			List<Element> ipadcProductChildren = ipadcProductElement.getChildren("product");
			for (Element element : ipadcProductChildren) {
				String key = element.getAttributeValue("id");
				Integer value = Integer.parseInt(element.getTextTrim());
				ipadChargeProduct.put(key, value);
			}
			
			checkinReward = new HashMap<String, Integer>();
			Element checkinRewardElement = root.getChild("checkinReward");
			List<Element> rewardChildren = checkinRewardElement.getChildren("reward");
			for (Element element : rewardChildren) {
				String key = element.getAttributeValue("id");
				Integer value = Integer.parseInt(element.getTextTrim());
				checkinReward.put(key, value);
			}
			
			notifyWebUrl = root.getChildText("notifyWebUrl");

			redisSecurityUUIDExpireTime = Integer.parseInt(root.getChildText("redisSecurityUUIDExpireTime"));
			clientSecurityUUIDActiveTime = Integer.parseInt(root.getChildText("clientSecurityUUIDActiveTime"));
			
			desSecurityKey = root.getChildText("desSecurityKey");
			
			periodGiftRankingExpireTime = Integer.parseInt(root.getChildText("periodGiftRankingExpireTime"));

			periodFamilyRoomRefreshExpireTime = Integer.parseInt(root.getChildText("periodFamilyRoomRefreshExpireTime"));

			webSecKey0 = root.getChildText("webseckey0").toCharArray();
			webSecKey1 = root.getChildText("webseckey1").toCharArray();

			familyMatchCacheRefreshExpireTime = Integer.parseInt(root.getChildText("familyMatchCacheRefreshExpireTime"));
			
			Element msgSysElement = root.getChild("MessageSystem");
			initLastReadTime = Long.parseLong(msgSysElement.getChildText("FirstLastReadTime"));
			cycleEffRedisActIds = Long.parseLong(msgSysElement.getChildText("CycleEffRedisActIds"));
			cycleGenAct = Long.parseLong(msgSysElement.getChildText("CycleGenAct"));

			newsRewardRankRefreshIntervel = Integer.parseInt(root.getChildText("newsRewardRankRefreshIntervel"));
			
			Element chinaUnicomFreeFlowIpEle = root.getChild("chinaUnicomFreeFlowIp");
			List<Element> chinaUnicomFreeFlowIpList = chinaUnicomFreeFlowIpEle.getChildren("ip");
			chinaUnicomFreeFlowIp = new ArrayList<String>();
			for (int i = 0; i < chinaUnicomFreeFlowIpList.size(); i++) {
				Element element = chinaUnicomFreeFlowIpList.get(i);
				chinaUnicomFreeFlowIp.add(element.getTextTrim());
			}
			
			chinaUnicomCpid = root.getChildText("chinaUnicomCpid");
			chinaUnicomPassword = root.getChildText("chinaUnicomPassword");
			chinaUnicomSpid = root.getChildText("chinaUnicomSpid");
			chinaUnicomCpid = root.getChildText("chinaUnicomPid");
			chinaUnicomPortalid = root.getChildText("chinaUnicomPortalid");
			
			videoURL = root.getChildText("videoURL");
			domainURL = root.getChildText("domainURL");
			bucket = root.getChildText("bucket");
			accessKey = root.getChildText("accessKey");
			secretKey = root.getChildText("secretKey");
			
			recNewsAddedCount = Integer.parseInt(root.getChildText("recNewsAddedCount"));
			firstChargeMedal = Integer.parseInt(root.getChildText("firstChargeMedal"));
			
			moreAppFlag = Integer.parseInt(root.getChildText("moreAppFlag"));
			
			annualActor = root.getChildText("annualActor");
            annualFamily = root.getChildText("annualFamily");
            
            runwayUrl = root.getChildText("runwayUrl");
            
            sendSunshinePrizeType = root.getChildText("sendSunshinePrizeType");
			
            setFollowStaticMax(Integer.parseInt(root.getChildText("followStaticMax")));
            
            accountType = root.getChildText("accountType");
			sdk_appid = root.getChildText("sdk_appid");
			expire_after = root.getChildText("expire_after");
			
			kkDomain = root.getChildText("kkDomain");
            
		} catch (Exception e) {
			logger.error("读取配置文件config.xml异常!", e);
		}

	}
	
    public static String getRoomLiveRecordUrl() {
		return roomLiveRecordUrl;
	}

	public static void setRoomLiveRecordUrl(String roomLiveRecordUrl) {
		ConfigHelper.roomLiveRecordUrl = roomLiveRecordUrl;
	}

	public static String getRecoderServerAddress() {
        return recoderServerAddress;
    }

    public static void setRecoderServerAddress(String recoderServerAddress) {
        ConfigHelper.recoderServerAddress = recoderServerAddress;
    }

    public static String getKkApiAddress() {
        return kkApiAddress;
    }

    public static void setKkApiAddress(String kkApiAddress) {
        ConfigHelper.kkApiAddress = kkApiAddress;
    }

    public static int getAppId() {
        return appId;
    }

    public static void setAppId(int appId) {
        ConfigHelper.appId = appId;
    }

	public static long getInitLastReadTime() {
		return initLastReadTime;
	}

	public static void setInitLastReadTime(long initLastReadTime) {
		ConfigHelper.initLastReadTime = initLastReadTime;
	}

	public static long getCycleEffRedisActIds() {
		return cycleEffRedisActIds;
	}

	public static void setCycleEffRedisActIds(long cycleEffRedisActIds) {
		ConfigHelper.cycleEffRedisActIds = cycleEffRedisActIds;
	}

	public static long getCycleGenAct() {
		return cycleGenAct;
	}

	public static void setCycleGenAct(long cycleGenAct) {
		ConfigHelper.cycleGenAct = cycleGenAct;
	}

	public static String getHttpdir() {
		return httpdir;
	}

	public static void setHttpdir(String httpdir) {
		ConfigHelper.httpdir = httpdir;
	}

	public static String getMfsURL() {
		return mfsURL;
	}

	public static void setMfsURL(String mfsURL) {
		ConfigHelper.mfsURL = mfsURL;
	}

	public static String getMcmURL() {
		return mcmURL;
	}

	public static void setMcmURL(String mcmURL) {
		ConfigHelper.mcmURL = mcmURL;
	}
	
	public static String getZkServices() {
		return zkServices;
	}

	public static void setZkServices(String zkServices) {
		ConfigHelper.zkServices = zkServices;
	}

	@Deprecated
	public static int getVip0() {
		return vip0;
	}

	public static void setVip0(int vip0) {
		ConfigHelper.vip0 = vip0;
	}

	@Deprecated
	public static int getVip0kickout() {
		return vip0kickout;
	}

	public static void setVip0kickout(int vip0kickout) {
		ConfigHelper.vip0kickout = vip0kickout;
	}
	
	@Deprecated
	public static int getVip0shutup() {
		return vip0shutup;
	}

	public static void setVip0shutup(int vip0shutup) {
		ConfigHelper.vip0shutup = vip0shutup;
	}

	@Deprecated
	public static int getVip1() {
		return vip1;
	}

	public static void setVip1(int vip1) {
		ConfigHelper.vip1 = vip1;
	}

	@Deprecated
	public static int getVip1kickout() {
		return vip1kickout;
	}

	public static void setVip1kickout(int vip1kickout) {
		ConfigHelper.vip1kickout = vip1kickout;
	}

	@Deprecated
	public static int getVip1shutup() {
		return vip1shutup;
	}

	public static void setVip1shutup(int vip1shutup) {
		ConfigHelper.vip1shutup = vip1shutup;
	}

	public static String getVersionSavePath() {
		return versionSavePath;
	}

	public static void setVersionSavePath(String versionSavePath) {
		ConfigHelper.versionSavePath = versionSavePath;
	}

	public static String getParkCarResURL() {
		return parkCarResURL;
	}

	public static void setParkCarResURL(String parkCarResURL) {
		ConfigHelper.parkCarResURL = parkCarResURL;
	}

	public static String getParkLogoResURL() {
		return parkLogoResURL;
	}

	public static void setParkLogoResURL(String parkLogoResURL) {
		ConfigHelper.parkLogoResURL = parkLogoResURL;
	}

	public static String getVipResURL() {
		return vipResURL;
	}

	public static void setVipResURL(String vipResURL) {
		ConfigHelper.vipResURL = vipResURL;
	}

	public static String getParkCarAndroidResURL() {
		return parkCarAndroidResURL;
	}

	public static void setParkCarAndroidResURL(String parkCarAndroidResURL) {
		ConfigHelper.parkCarAndroidResURL = parkCarAndroidResURL;
	}

	public static String getParkLogoAndroidResURL() {
		return parkLogoAndroidResURL;
	}

	public static void setParkLogoAndroidResURL(String parkLogoAndroidResURL) {
		ConfigHelper.parkLogoAndroidResURL = parkLogoAndroidResURL;
	}

	public static String getVipAndroidResURL() {
		return vipAndroidResURL;
	}

	public static void setVipAndroidResURL(String vipAndroidResURL) {
		ConfigHelper.vipAndroidResURL = vipAndroidResURL;
	}

	public static String getGiftIconAndroidResURL() {
		return giftIconAndroidResURL;
	}

	public static void setGiftIconAndroidResURL(String giftIconAndroidResURL) {
		ConfigHelper.giftIconAndroidResURL = giftIconAndroidResURL;
	}

	public static String getGiftGifAndroidResURL() {
		return giftGifAndroidResURL;
	}

	public static void setGiftGifAndroidResURL(String giftGifAndroidResURL) {
		ConfigHelper.giftGifAndroidResURL = giftGifAndroidResURL;
	}

	public static String getGiftIconIphoneResURL() {
		return giftIconIphoneResURL;
	}

	public static void setGiftIconIphoneResURL(String giftIconIphoneResURL) {
		ConfigHelper.giftIconIphoneResURL = giftIconIphoneResURL;
	}

	public static String getGiftGifIphoneResURL() {
		return giftGifIphoneResURL;
	}

	public static void setGiftGifIphoneResURL(String giftGifIphoneResURL) {
		ConfigHelper.giftGifIphoneResURL = giftGifIphoneResURL;
	}

	public static String getActivityUrl() {
		return activityUrl;
	}

	public static void setActivityUrl(String activityUrl) {
		ConfigHelper.activityUrl = activityUrl;
	}

	public static String getNoticeUrl() {
		return noticeUrl;
	}

	public static void setNoticeUrl(String noticeUrl) {
		ConfigHelper.noticeUrl = noticeUrl;
	}

	public static String getCrashSavePath() {
		return crashSavePath;
	}

	public static void setCrashSavePath(String crashSavePath) {
		ConfigHelper.crashSavePath = crashSavePath;
	}

	public static int getGiftCompeteRefreshIntervel() {
		return giftCompeteRefreshIntervel;
	}

	public static void setGiftCompeteRefreshIntervel(int giftCompeteRefreshIntervel) {
		ConfigHelper.giftCompeteRefreshIntervel = giftCompeteRefreshIntervel;
	}

	public static String getUpVersion() {
		return upVersion;
	}

	public static void setUpVersion(String upVersion) {
		ConfigHelper.upVersion = upVersion;
	}

	public static String getUpMerchantId() {
		return upMerchantId;
	}

	public static void setUpMerchantId(String upMerchantId) {
		ConfigHelper.upMerchantId = upMerchantId;
	}

	public static String getUpSecurityKey() {
		return upSecurityKey;
	}

	public static void setUpSecurityKey(String upSecurityKey) {
		ConfigHelper.upSecurityKey = upSecurityKey;
	}

	public static String getUpBackendUrl() {
		return upBackendUrl;
	}

	public static void setUpBackendUrl(String upBackendUrl) {
		ConfigHelper.upBackendUrl = upBackendUrl;
	}

	public static String getUpQueryUrl() {
		return upQueryUrl;
	}

	public static void setUpQueryUrl(String upQueryUrl) {
		ConfigHelper.upQueryUrl = upQueryUrl;
	}

	public static String getUpTradeUrl() {
		return upTradeUrl;
	}

	public static void setUpTradeUrl(String upTradeUrl) {
		ConfigHelper.upTradeUrl = upTradeUrl;
	}
	
	public static String getUpResponseCodeSuccess() {
		return upResponseCodeSuccess;
	}

	public static void setUpResponseCodeSuccess(String upResponseCodeSuccess) {
		ConfigHelper.upResponseCodeSuccess = upResponseCodeSuccess;
	}

	public static Set<String> getAllowedPayClientIp() {
		return allowedPayClientIp;
	}

	public static void setAllowedPayClientIp(Set<String> allowedPayClientIp) {
		ConfigHelper.allowedPayClientIp = allowedPayClientIp;
	}

	public static String getUpOrderDescription() {
		return upOrderDescription;
	}

	public static void setUpOrderDescription(String upOrderDescription) {
		ConfigHelper.upOrderDescription = upOrderDescription;
	}

	public static int getRoomFansRankingIntervel() {
		return roomFansRankingIntervel;
	}

	public static void setRoomFansRankingIntervel(int roomFansRankingIntervel) {
		ConfigHelper.roomFansRankingIntervel = roomFansRankingIntervel;
	}
	
	public static boolean isRecordCallInterface() {
		return recordCallInterface;
	}

	public static void setRecordCallInterface(boolean recordCallInterface) {
		ConfigHelper.recordCallInterface = recordCallInterface;
	}

	public static String getGiftSmallIconAndroidResURL() {
		return giftSmallIconAndroidResURL;
	}

	public static void setGiftSmallIconAndroidResURL(String giftSmallIconAndroidResURL) {
		ConfigHelper.giftSmallIconAndroidResURL = giftSmallIconAndroidResURL;
	}

	public static String getGiftSmallIconIphoneResURL() {
		return giftSmallIconIphoneResURL;
	}

	public static void setGiftSmallIconIphoneResURL(String giftSmallIconIphoneResURL) {
		ConfigHelper.giftSmallIconIphoneResURL = giftSmallIconIphoneResURL;
	}

	public static String getAppleStoreVerifyUrl() {
		return appleStoreVerifyUrl;
	}

	public static void setAppleStoreVerifyUrl(String appleStoreVerifyUrl) {
		ConfigHelper.appleStoreVerifyUrl = appleStoreVerifyUrl;
	}

	public static Map<String, Integer> getIphoneChargeProduct() {
		return iphoneChargeProduct;
	}

	public static void setIphoneChargeProduct(Map<String, Integer> iphoneChargeProduct) {
		ConfigHelper.iphoneChargeProduct = iphoneChargeProduct;
	}

	public static Map<String, Integer> getIpadChargeProduct() {
		return ipadChargeProduct;
	}

	public static void setIpadChargeProduct(Map<String, Integer> ipadChargeProduct) {
		ConfigHelper.ipadChargeProduct = ipadChargeProduct;
	}

	public static Map<String, Integer> getCheckinReward() {
		return checkinReward;
	}

	public static void setCheckinReward(Map<String, Integer> checkinReward) {
		ConfigHelper.checkinReward = checkinReward;
	}

	public static String getNotifyWebUrl() {
		return notifyWebUrl;
	}

	public static void setNotifyWebUrl(String notifyWebUrl) {
		ConfigHelper.notifyWebUrl = notifyWebUrl;
	}

	public static int getRedisUserDataExpireTime() {
		return redisUserDataExpireTime;
	}

	public static void setRedisUserDataExpireTime(int redisUserDataExpireTime) {
		ConfigHelper.redisUserDataExpireTime = redisUserDataExpireTime;
	}

	public static int getRedisSecurityUUIDExpireTime() {
		return redisSecurityUUIDExpireTime;
	}

	public static void setRedisSecurityUUIDExpireTime(int redisSecurityUUIDExpireTime) {
		ConfigHelper.redisSecurityUUIDExpireTime = redisSecurityUUIDExpireTime;
	}

	public static String getDesSecurityKey() {
		return desSecurityKey;
	}

	public static void setDesSecurityKey(String desSecurityKey) {
		ConfigHelper.desSecurityKey = desSecurityKey;
	}
	
	public static char[] getWebSecurityKey0() {
		return webSecKey0;
	}

	public static void setDesSecurityKey0(char[]  key) {
		ConfigHelper.webSecKey0 = key;
	}
	
	public static char[] getWebSecurityKey1() {
		return webSecKey1;
	}

	public static void setDesSecurityKey1(char[]  key) {
		ConfigHelper.webSecKey1 = key;
	}

	public static int getClientSecurityUUIDActiveTime() {
		return clientSecurityUUIDActiveTime;
	}

	public static void setClientSecurityUUIDActiveTime(int clientSecurityUUIDActiveTime) {
		ConfigHelper.clientSecurityUUIDActiveTime = clientSecurityUUIDActiveTime;
	}

	public static int getPeriodGiftRankingExpireTime() {
		return periodGiftRankingExpireTime;
	}

	public static void setPeriodGiftRankingExpireTime(int periodGiftRankingExpireTime) {
		ConfigHelper.periodGiftRankingExpireTime = periodGiftRankingExpireTime;
	}

	public static int getPeriodFamilyRoomRefreshExpireTime() {
		return periodFamilyRoomRefreshExpireTime;
	}

	public static void setPeriodFamilyRoomRefreshExpireTime(int periodFamilyRoomRefreshExpireTime) {
		ConfigHelper.periodFamilyRoomRefreshExpireTime = periodFamilyRoomRefreshExpireTime;
	}

	public static Set<String> getAllowedRegisterClientIp() {
		return allowedRegisterClientIp;
	}

	public static void setAllowedRegisterClientIp(Set<String> allowedRegisterClientIp) {
		ConfigHelper.allowedRegisterClientIp = allowedRegisterClientIp;
	}

	public static int getFamilyMatchCacheRefreshExpireTime() {
		return familyMatchCacheRefreshExpireTime;
	}

	public static void setFamilyMatchCacheRefreshExpireTime(int familyMatchCacheRefreshExpireTime) {
		ConfigHelper.familyMatchCacheRefreshExpireTime = familyMatchCacheRefreshExpireTime;
	}

	public static String getWebRookieLogo() {
		return webRookieLogo;
	}

	public static String getAndroidRookieLogo() {
		return androidRookieLogo;
	}

	public static String getIphoneRookieLogo() {
		return iphoneRookieLogo;
	}

	public static String getWebWeeklyLogo() {
		return webWeeklyLogo;
	}

	public static String getAndroidWeeklyLogo() {
		return androidWeeklyLogo;
	}

	public static String getIphoneWeeklyLogo() {
		return iphoneWeeklyLogo;
	}

	public static void setWebRookieLogo(String webRookieLogo) {
		ConfigHelper.webRookieLogo = webRookieLogo;
	}

	public static void setAndroidRookieLogo(String androidRookieLogo) {
		ConfigHelper.androidRookieLogo = androidRookieLogo;
	}

	public static void setIphoneRookieLogo(String iphoneRookieLogo) {
		ConfigHelper.iphoneRookieLogo = iphoneRookieLogo;
	}

	public static void setWebWeeklyLogo(String webWeeklyLogo) {
		ConfigHelper.webWeeklyLogo = webWeeklyLogo;
	}

	public static void setAndroidWeeklyLogo(String androidWeeklyLogo) {
		ConfigHelper.androidWeeklyLogo = androidWeeklyLogo;
	}

	public static void setIphoneWeeklyLogo(String iphoneWeeklyLogo) {
		ConfigHelper.iphoneWeeklyLogo = iphoneWeeklyLogo;
	}

	public static String getMediahttpdir() {
		return mediahttpdir;
	}

	public static void setMediahttpdir(String mediahttpdir) {
		ConfigHelper.mediahttpdir = mediahttpdir;
	}

	public static int getNewsRewardRankRefreshIntervel() {
		return newsRewardRankRefreshIntervel;
	}

	public static void setNewsRewardRankRefreshIntervel(int newsRewardRankRefreshIntervel) {
		ConfigHelper.newsRewardRankRefreshIntervel = newsRewardRankRefreshIntervel;
	}

	public static List<String> getChinaUnicomFreeFlowIp() {
		return chinaUnicomFreeFlowIp;
	}

	public static void setChinaUnicomFreeFlowIp(List<String> chinaUnicomFreeFlowIp) {
		ConfigHelper.chinaUnicomFreeFlowIp = chinaUnicomFreeFlowIp;
	}

	public static String getChinaUnicomCpid() {
		return chinaUnicomCpid;
	}

	public static void setChinaUnicomCpid(String chinaUnicomCpid) {
		ConfigHelper.chinaUnicomCpid = chinaUnicomCpid;
	}

	public static String getChinaUnicomPassword() {
		return chinaUnicomPassword;
	}

	public static void setChinaUnicomPassword(String chinaUnicomPassword) {
		ConfigHelper.chinaUnicomPassword = chinaUnicomPassword;
	}

	public static String getChinaUnicomSpid() {
		return chinaUnicomSpid;
	}

	public static void setChinaUnicomSpid(String chinaUnicomSpid) {
		ConfigHelper.chinaUnicomSpid = chinaUnicomSpid;
	}

	public static String getChinaUnicomPid() {
		return chinaUnicomPid;
	}

	public static void setChinaUnicomPid(String chinaUnicomPid) {
		ConfigHelper.chinaUnicomPid = chinaUnicomPid;
	}

	public static String getChinaUnicomPortalid() {
		return chinaUnicomPortalid;
	}

	public static void setChinaUnicomPortalid(String chinaUnicomPortalid) {
		ConfigHelper.chinaUnicomPortalid = chinaUnicomPortalid;
	}

	public static String getVideoURL() {
		return videoURL;
	}

	public static void setVideoURL(String videoURL) {
		ConfigHelper.videoURL = videoURL;
	}
	
    public static String getDomainURL() {
        return domainURL;
    }

    public static void setDomainURL(String domainURL) {
        ConfigHelper.domainURL = domainURL;
    }
    
    public static String getBucket() {
        return bucket;
    }

    public static void setBucket(String bucket) {
        ConfigHelper.bucket = bucket;
    }

	public static String getAccessKey() {
		return accessKey;
	}

	public static String getSecretKey() {
		return secretKey;
	}

	public static void setAccessKey(String accessKey) {
		ConfigHelper.accessKey = accessKey;
	}

	public static void setSecretKey(String secretKey) {
		ConfigHelper.secretKey = secretKey;
	}

	public static int getRecNewsAddedCount() {
		return recNewsAddedCount;
	}

	public static void setRecNewsAddedCount(int recNewsAddedCount) {
		ConfigHelper.recNewsAddedCount = recNewsAddedCount;
	}

	public static int getMoreAppFlag() {
		return moreAppFlag;
	}

	public static void setMoreAppFlag(int moreAppFlag) {
		ConfigHelper.moreAppFlag = moreAppFlag;
	}
    
    public static String getAnnualActor() {
        return annualActor;
    }

    public static String getAnnualFamily() {
        return annualFamily;
    }

    public static String getVipWebResURL() {
        return vipWebResURL;
    }

    public static void setVipWebResURL(String vipWebResURL) {
        ConfigHelper.vipWebResURL = vipWebResURL;
    }

    public static String getRunwayUrl() {
        return runwayUrl;
    }

    public static String getSendSunshinePrizeType() {
        return sendSunshinePrizeType;
    }

    public static String getKkgameActivity() {
        return kkgameActivity;
    }

	public static Integer getFirstChargeMedal() {
		return firstChargeMedal;
	}

	public static void setFirstChargeMedal(Integer firstChargeMedal) {
		ConfigHelper.firstChargeMedal = firstChargeMedal;
	}

	public static int getFollowStaticMax() {
		return followStaticMax;
	}

	public static void setFollowStaticMax(int followStaticMax) {
		ConfigHelper.followStaticMax = followStaticMax;
	}

	public static int getGiftRankPosition() {
		return giftRankPosition;
	}

	public static void setGiftRankPosition(int giftRankPosition) {
		ConfigHelper.giftRankPosition = giftRankPosition;
	}

	public static String getAccountType() {
		return accountType;
	}

	public static String getSdk_appid() {
		return sdk_appid;
	}

	public static String getExpire_after() {
		return expire_after;
	}
    
    public static String getKkDomain() {
        return kkDomain;
    }

    public static void setKkDomain(String kkDomain) {
        ConfigHelper.kkDomain = kkDomain;
    }
}
