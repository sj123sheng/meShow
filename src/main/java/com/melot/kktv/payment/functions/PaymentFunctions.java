package com.melot.kktv.payment.functions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.payment.conf.PaymentActivityConf;
import com.melot.kktv.payment.conf.PaymentGradeConf;
import com.melot.kktv.payment.conf.PaymentInfoConf;
import com.melot.kktv.payment.conf.PaymentPackageConf;
import com.melot.kktv.payment.domain.ConfPaymentActivity;
import com.melot.kktv.payment.domain.ConfPaymentGrade;
import com.melot.kktv.payment.domain.ConfPaymentInfo;
import com.melot.kktv.payment.domain.ConfPaymentPackage;
import com.melot.kktv.payment.domain.PaymentPackageGift;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class PaymentFunctions {
	
	/**
	 * 获取充值配置信息
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getPaymentConfigInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	
    	JsonObject result = new JsonObject();
        
        int appId, platform, version;
        
        try {
        	appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, 5);
        	platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.ANDROID, null, 1, 5);
        	version = CommonUtil.getJsonParamInt(jsonObject, "version", 0, null, 0, Integer.MAX_VALUE);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 返回充值活动配置
        List<ConfPaymentActivity> activityList = PaymentActivityConf.getActivityList(appId);
        if (activityList != null && activityList.size() > 0) {
        	JsonArray activityConfigs = new JsonArray();
        	for (ConfPaymentActivity activity : activityList) {
        		JsonObject activityJson = new JsonObject();
        		activityJson.addProperty("activityId", activity.getActivityId());
        		if (activity.getActivityType() != null) {
        			activityJson.addProperty("activityType", activity.getActivityType());
        		}
        		String activityURL = null;
        		if (activity.getActivityURL() == null || activity.getActivityURL().trim().equals("")
        				|| activity.getActivityURL().trim().equals("#")) {
        			activityURL = ConfigHelper.getActivityUrl() + activity.getActivityURL();
        		} else {
        			activityURL = activity.getActivityURL();
        		}
        		String activityMobileURL = null;
                if (activity.getActivityMobileURL() == null || activity.getActivityMobileURL().trim().equals("")
                        || activity.getActivityMobileURL().trim().equals("#")) {
                    activityMobileURL = ConfigHelper.getActivityUrl() + activity.getActivityMobileURL();
                } else {
                    activityMobileURL = activity.getActivityMobileURL();
                }
                if (platform != PlatformEnum.WEB) {
                    if (activityMobileURL != null) {
                        activityJson.addProperty("activityURL", activityMobileURL);
                    }
                } else if (activityURL != null) {
                    activityJson.addProperty("activityURL", activityURL);
                }
        		if (platform == PlatformEnum.WEB) {
        			if (activity.getTopURL() != null && !activity.getTopURL().trim().equals(""))
        				activityJson.addProperty("topURL", activity.getTopURL());
        		} else if (platform == PlatformEnum.ANDROID) {
        			if (activity.getTopMobileURL() != null && !activity.getTopMobileURL().trim().equals(""))
        				activityJson.addProperty("topURL", activity.getTopMobileURL());
        		} else if (platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
        			if (activity.getTopMobileURLIOS() != null && !activity.getTopMobileURLIOS().trim().equals(""))
        				activityJson.addProperty("topURL", activity.getTopMobileURLIOS());
        		}
        		activityConfigs.add(activityJson);
    		}
        	result.add("activityConfig", activityConfigs);
        	Integer acType;
        	JsonArray activityConfig = new JsonArray();
        	for (int i = 0; i < activityConfigs.size(); i++) {
        		acType = activityConfigs.get(i).getAsJsonObject().get("activityType").getAsInt();
        		if (acType != null && (acType == 0 || acType == 1)) {
        			activityConfigs.get(i).getAsJsonObject().addProperty("isFirstCharge", acType);
        			activityConfig.add(activityConfigs.get(i).getAsJsonObject());
        		}
			}
        	result.add("activityConfigs", activityConfig);
        }
		
        // 返回充值用户等级配置
        List<ConfPaymentGrade> gradeList = PaymentGradeConf.getGradeList(appId);
        if (gradeList != null) {
        	JsonArray gradeConfigs = new JsonArray();
        	try {
        		gradeConfigs = new JsonParser().parse(new Gson().toJson(gradeList)).getAsJsonArray(); 
			} catch (Exception e) {
			}
        	result.add("gradeConfigs", gradeConfigs);
        }
        
        // 返回充值礼包配置
        List<ConfPaymentPackage> packageList = PaymentPackageConf.getPackageList(appId);
        if (packageList != null) {
        	JsonArray packageConfigs = new JsonArray();
        	try {
        		JsonObject paymentPackageObject, giftObject;
        		JsonArray packageGiftsArray;
        		for (ConfPaymentPackage paymentPackage : packageList) {
        		    paymentPackageObject = new JsonObject();
        		    paymentPackageObject.addProperty("minAmount", paymentPackage.getMinAmount());
        		    paymentPackageObject.addProperty("maxAmount", paymentPackage.getMaxAmount());
        		    paymentPackageObject.addProperty("packageId", paymentPackage.getPackageId());
        		    paymentPackageObject.addProperty("packageNotice", paymentPackage.getPackageNotice());
        		    paymentPackageObject.addProperty("packageWorth", paymentPackage.getPackageWorth());
        		    
        		    packageGiftsArray = new JsonArray();
        		    if (paymentPackage.getPackageGiftList() != null && paymentPackage.getPackageGiftList().size() > 0) {
                        for (PaymentPackageGift gift : paymentPackage.getPackageGiftList()) {
                            giftObject = new JsonObject();
                            giftObject.addProperty("giftId", gift.getGiftId());
                            giftObject.addProperty("giftType", gift.getGiftType());
                            giftObject.addProperty("giftName", gift.getGiftName());
                            giftObject.addProperty("giftNotice", gift.getGiftNotice());
                            giftObject.addProperty("giftIcon", gift.getGiftIcon());
                            giftObject.addProperty("giftCount", gift.getGiftCount());
                            
                            // 礼品类型(1)秀币(2)礼物(3)vip(4)座驾(5)券(6)勋章
                            switch (gift.getGiftType().intValue()) {
                            case 1:
                                giftObject.addProperty("giftUnit", "秀币");
                                break;

                            case 2:
                                giftObject.addProperty("giftUnit", "个");
                                break;

                            case 3:
                            case 4:
                            case 6:
                                giftObject.addProperty("giftUnit", "天");
                                break;

                            case 5:
                                giftObject.addProperty("giftUnit", "张");
                                break;

                            default:
                                break;
                            }
                            packageGiftsArray.add(giftObject);
                        }
                    }
        		    paymentPackageObject.add("packageGiftList", packageGiftsArray);
        		    packageConfigs.add(paymentPackageObject);
                }
			} catch (Exception e) {
			}
        	result.add("packageConfigs", packageConfigs);
        }
        
        // 返回充值勋章配置
        UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
        List<ConfMedal> medalList = userMedalService.getAllRechargeMedaList();
        if (medalList != null) {
        	Collections.sort(medalList, new Comparator<ConfMedal>() {
				@Override
				public int compare(ConfMedal o1, ConfMedal o2) {
					return o1.getMedalLevel() - o2.getMedalLevel();
				}
			});
        	JsonArray medalConfigs = new JsonArray();
        	for (ConfMedal medal : medalList) {
        		JsonObject medalJson = new JsonObject();
            	medalJson.addProperty("medalTitle", medal.getMedalTitle());
            	medalJson.addProperty("medalLevel", medal.getMedalLevel() - 1);
            	try {
            		JsonObject medalIconJson = new JsonParser().parse(medal.getMedalMedalUrl()).getAsJsonObject();
            		if (platform == PlatformEnum.WEB) {
            			if (medalIconJson.get("web") != null
            					&& !medalIconJson.get("web").getAsString().trim().equals(""))
            				medalJson.addProperty("medalIcon", medalIconJson.get("web").getAsString());
            		} else if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE) {
            			if (medalIconJson.get("phone_small") != null
            					&& !medalIconJson.get("phone_small").getAsString().trim().equals(""))
            				medalJson.addProperty("medalIcon", medalIconJson.get("phone_small").getAsString());
            		} else if (platform == PlatformEnum.IPAD) {
            			if (medalIconJson.get("phone_large") != null
            					&& !medalIconJson.get("phone_large").getAsString().trim().equals(""))
            				medalJson.addProperty("medalIcon", medalIconJson.get("phone_large").getAsString());
            		}
				} catch (Exception e) {
				}
            	medalJson.addProperty("minAmount", medal.getMinAmount());
            	medalJson.addProperty("maxAmount", medal.getMaxAmount());
            	medalConfigs.add(medalJson);
            }
        	result.add("medalConfigs", medalConfigs);
        }
        
        // 返回充值类型配置
        List<ConfPaymentInfo> paymentList = PaymentInfoConf.getPaymentList(appId, version);
        if (paymentList != null) {
        	JsonArray paymentConfigs = new JsonArray();
        	try {
        		paymentConfigs = new JsonParser().parse(new Gson().toJson(paymentList)).getAsJsonArray(); 
			} catch (Exception e) {
			}
        	result.add("paymentConfigs", paymentConfigs);
        }
        
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
    }
	
}
