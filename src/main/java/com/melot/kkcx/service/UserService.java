package com.melot.kkcx.service;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.melot.cms.admin.api.bean.OfficialIdInfo;
import com.melot.cms.admin.api.constant.AdminApiTagCodes;
import com.melot.cms.admin.api.service.AdminDataService;
import com.melot.cms.api.base.Result;
import com.melot.kk.otherlogin.api.service.OtherLoginService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.common.driver.service.StarService;
import com.melot.goldcoin.domain.UserGoldAssets;
import com.melot.goldcoin.service.GoldcoinService;
import com.melot.kkcore.account.service.AccountSecurityService;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.api.UserGameAssets;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserLevel;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.ActorProfit;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.model.StarInfo;
import com.melot.kktv.domain.UserInfo;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.LoginTypeEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class UserService {
	
	private static Map<Integer, Integer> liveVideoMap = new HashMap<Integer, Integer>(310);
	static {
		liveVideoMap.put(6560218, 2);
		liveVideoMap.put(113910033, 2);
		liveVideoMap.put(114638376, 2);
		liveVideoMap.put(10000, 3);
		liveVideoMap.put(10034, 3);
		liveVideoMap.put(100012, 3);
		liveVideoMap.put(2754066, 3);
		liveVideoMap.put(3093747, 3);
		liveVideoMap.put(7769247, 3);
		liveVideoMap.put(12341948, 3);
		liveVideoMap.put(12370447, 3);
		liveVideoMap.put(20327381, 3);
		liveVideoMap.put(23396146, 3);
		liveVideoMap.put(25944016, 3);
		liveVideoMap.put(26107912, 3);
		liveVideoMap.put(31132069, 3);
		liveVideoMap.put(36992837, 3);
		liveVideoMap.put(37395221, 3);
		liveVideoMap.put(37715288, 3);
		liveVideoMap.put(39323364, 3);
		liveVideoMap.put(41405921, 3);
		liveVideoMap.put(43678517, 3);
		liveVideoMap.put(57762269, 3);
		liveVideoMap.put(58347793, 3);
		liveVideoMap.put(60489540, 3);
		liveVideoMap.put(63002177, 3);
		liveVideoMap.put(63213566, 3);
		liveVideoMap.put(64824698, 3);
		liveVideoMap.put(66263658, 3);
		liveVideoMap.put(68268168, 3);
		liveVideoMap.put(69108988, 3);
		liveVideoMap.put(70006222, 3);
		liveVideoMap.put(70445137, 3);
		liveVideoMap.put(71568535, 3);
		liveVideoMap.put(71582364, 3);
		liveVideoMap.put(72134035, 3);
		liveVideoMap.put(72898341, 3);
		liveVideoMap.put(72980805, 3);
		liveVideoMap.put(75374533, 3);
		liveVideoMap.put(76030795, 3);
		liveVideoMap.put(77899697, 3);
		liveVideoMap.put(79207254, 3);
		liveVideoMap.put(79859247, 3);
		liveVideoMap.put(79934765, 3);
		liveVideoMap.put(80053742, 3);
		liveVideoMap.put(80371403, 3);
		liveVideoMap.put(80374704, 3);
		liveVideoMap.put(80565984, 3);
		liveVideoMap.put(80845596, 3);
		liveVideoMap.put(80961267, 3);
		liveVideoMap.put(81141213, 3);
		liveVideoMap.put(81500440, 3);
		liveVideoMap.put(81501703, 3);
		liveVideoMap.put(82213210, 3);
		liveVideoMap.put(82618958, 3);
		liveVideoMap.put(83360082, 3);
		liveVideoMap.put(83804345, 3);
		liveVideoMap.put(84525969, 3);
		liveVideoMap.put(86612858, 3);
		liveVideoMap.put(86855516, 3);
		liveVideoMap.put(86952675, 3);
		liveVideoMap.put(86980642, 3);
		liveVideoMap.put(88889908, 3);
		liveVideoMap.put(89271717, 3);
		liveVideoMap.put(91143024, 3);
		liveVideoMap.put(92187426, 3);
		liveVideoMap.put(92274142, 3);
		liveVideoMap.put(93977273, 3);
		liveVideoMap.put(94398295, 3);
		liveVideoMap.put(94933406, 3);
		liveVideoMap.put(95089981, 3);
		liveVideoMap.put(95781907, 3);
		liveVideoMap.put(97065625, 3);
		liveVideoMap.put(97449179, 3);
		liveVideoMap.put(98160079, 3);
		liveVideoMap.put(98379823, 3);
		liveVideoMap.put(99017365, 3);
		liveVideoMap.put(99911886, 3);
		liveVideoMap.put(99919728, 3);
		liveVideoMap.put(101720824, 3);
		liveVideoMap.put(102579744, 3);
		liveVideoMap.put(102934494, 3);
		liveVideoMap.put(102980270, 3);
		liveVideoMap.put(104498976, 3);
		liveVideoMap.put(104564858, 3);
		liveVideoMap.put(104928820, 3);
		liveVideoMap.put(105192208, 3);
		liveVideoMap.put(105440282, 3);
		liveVideoMap.put(105531019, 3);
		liveVideoMap.put(105558131, 3);
		liveVideoMap.put(105595102, 3);
		liveVideoMap.put(105603584, 3);
		liveVideoMap.put(105654879, 3);
		liveVideoMap.put(105658210, 3);
		liveVideoMap.put(105865538, 3);
		liveVideoMap.put(106164033, 3);
		liveVideoMap.put(106270089, 3);
		liveVideoMap.put(106390343, 3);
		liveVideoMap.put(106702807, 3);
		liveVideoMap.put(106935189, 3);
		liveVideoMap.put(106975457, 3);
		liveVideoMap.put(107167888, 3);
		liveVideoMap.put(107405328, 3);
		liveVideoMap.put(107409172, 3);
		liveVideoMap.put(107409919, 3);
		liveVideoMap.put(107413123, 3);
		liveVideoMap.put(107414218, 3);
		liveVideoMap.put(107666293, 3);
		liveVideoMap.put(107677888, 3);
		liveVideoMap.put(107758567, 3);
		liveVideoMap.put(107814143, 3);
		liveVideoMap.put(108044423, 3);
		liveVideoMap.put(108047551, 3);
		liveVideoMap.put(108067070, 3);
		liveVideoMap.put(108116784, 3);
		liveVideoMap.put(108133019, 3);
		liveVideoMap.put(108159989, 3);
		liveVideoMap.put(108159998, 3);
		liveVideoMap.put(108160044, 3);
		liveVideoMap.put(108160162, 3);
		liveVideoMap.put(108160178, 3);
		liveVideoMap.put(108160199, 3);
		liveVideoMap.put(108268977, 3);
		liveVideoMap.put(108352827, 3);
		liveVideoMap.put(108356413, 3);
		liveVideoMap.put(108357289, 3);
		liveVideoMap.put(108357468, 3);
		liveVideoMap.put(108387647, 3);
		liveVideoMap.put(108392980, 3);
		liveVideoMap.put(108410153, 3);
		liveVideoMap.put(108410656, 3);
		liveVideoMap.put(108438137, 3);
		liveVideoMap.put(108441575, 3);
		liveVideoMap.put(108456241, 3);
		liveVideoMap.put(108457370, 3);
		liveVideoMap.put(108483113, 3);
		liveVideoMap.put(108495680, 3);
		liveVideoMap.put(108496261, 3);
		liveVideoMap.put(108500094, 3);
		liveVideoMap.put(108503260, 3);
		liveVideoMap.put(108645923, 3);
		liveVideoMap.put(108729542, 3);
		liveVideoMap.put(108880494, 3);
		liveVideoMap.put(108941772, 3);
		liveVideoMap.put(108942989, 3);
		liveVideoMap.put(108949416, 3);
		liveVideoMap.put(108954343, 3);
		liveVideoMap.put(109051437, 3);
		liveVideoMap.put(109058975, 3);
		liveVideoMap.put(109121778, 3);
		liveVideoMap.put(109132358, 3);
		liveVideoMap.put(109142745, 3);
		liveVideoMap.put(109174542, 3);
		liveVideoMap.put(109180063, 3);
		liveVideoMap.put(109198719, 3);
		liveVideoMap.put(109199257, 3);
		liveVideoMap.put(109203288, 3);
		liveVideoMap.put(109243596, 3);
		liveVideoMap.put(109401296, 3);
		liveVideoMap.put(109419078, 3);
		liveVideoMap.put(109456064, 3);
		liveVideoMap.put(109475793, 3);
		liveVideoMap.put(109494446, 3);
		liveVideoMap.put(109518863, 3);
		liveVideoMap.put(109572465, 3);
		liveVideoMap.put(109622888, 3);
		liveVideoMap.put(109729473, 3);
		liveVideoMap.put(109834759, 3);
		liveVideoMap.put(109893881, 3);
		liveVideoMap.put(109896281, 3);
		liveVideoMap.put(109897235, 3);
		liveVideoMap.put(109937274, 3);
		liveVideoMap.put(109937500, 3);
		liveVideoMap.put(109937750, 3);
		liveVideoMap.put(109939032, 3);
		liveVideoMap.put(109970614, 3);
		liveVideoMap.put(110000529, 3);
		liveVideoMap.put(110014203, 3);
		liveVideoMap.put(110056121, 3);
		liveVideoMap.put(110131099, 3);
		liveVideoMap.put(110131331, 3);
		liveVideoMap.put(110136469, 3);
		liveVideoMap.put(110138759, 3);
		liveVideoMap.put(110147368, 3);
		liveVideoMap.put(110193967, 3);
		liveVideoMap.put(110248163, 3);
		liveVideoMap.put(110253073, 3);
		liveVideoMap.put(110308878, 3);
		liveVideoMap.put(110309768, 3);
		liveVideoMap.put(110362751, 3);
		liveVideoMap.put(110363871, 3);
		liveVideoMap.put(110487632, 3);
		liveVideoMap.put(110488112, 3);
		liveVideoMap.put(110491767, 3);
		liveVideoMap.put(110583907, 3);
		liveVideoMap.put(110590492, 3);
		liveVideoMap.put(110625753, 3);
		liveVideoMap.put(110633574, 3);
		liveVideoMap.put(110645907, 3);
		liveVideoMap.put(110647886, 3);
		liveVideoMap.put(110746949, 3);
		liveVideoMap.put(110806511, 3);
		liveVideoMap.put(110808072, 3);
		liveVideoMap.put(110813209, 3);
		liveVideoMap.put(110859720, 3);
		liveVideoMap.put(110875478, 3);
		liveVideoMap.put(110875696, 3);
		liveVideoMap.put(110954506, 3);
		liveVideoMap.put(110955651, 3);
		liveVideoMap.put(110955829, 3);
		liveVideoMap.put(111003556, 3);
		liveVideoMap.put(111011510, 3);
		liveVideoMap.put(111169163, 3);
		liveVideoMap.put(111351412, 3);
		liveVideoMap.put(111408244, 3);
		liveVideoMap.put(111566132, 3);
		liveVideoMap.put(111609387, 3);
		liveVideoMap.put(111652193, 3);
		liveVideoMap.put(111658792, 3);
		liveVideoMap.put(111709451, 3);
		liveVideoMap.put(111756407, 3);
		liveVideoMap.put(111868612, 3);
		liveVideoMap.put(112077573, 3);
		liveVideoMap.put(112305521, 3);
		liveVideoMap.put(112339530, 3);
		liveVideoMap.put(112344658, 3);
		liveVideoMap.put(112371068, 3);
		liveVideoMap.put(112371662, 3);
		liveVideoMap.put(112399206, 3);
		liveVideoMap.put(112434685, 3);
		liveVideoMap.put(112437108, 3);
		liveVideoMap.put(112499495, 3);
		liveVideoMap.put(112503213, 3);
		liveVideoMap.put(112564146, 3);
		liveVideoMap.put(112686082, 3);
		liveVideoMap.put(112709089, 3);
		liveVideoMap.put(112723298, 3);
		liveVideoMap.put(112789386, 3);
		liveVideoMap.put(112852722, 3);
		liveVideoMap.put(112859691, 3);
		liveVideoMap.put(112926993, 3);
		liveVideoMap.put(112928489, 3);
		liveVideoMap.put(112955886, 3);
		liveVideoMap.put(112959996, 3);
		liveVideoMap.put(112984519, 3);
		liveVideoMap.put(112985832, 3);
		liveVideoMap.put(112985904, 3);
		liveVideoMap.put(112988599, 3);
		liveVideoMap.put(113062658, 3);
		liveVideoMap.put(113077736, 3);
		liveVideoMap.put(113081918, 3);
		liveVideoMap.put(113121898, 3);
		liveVideoMap.put(113161643, 3);
		liveVideoMap.put(113365578, 3);
		liveVideoMap.put(113423641, 3);
		liveVideoMap.put(113489768, 3);
		liveVideoMap.put(113490597, 3);
		liveVideoMap.put(113512763, 3);
		liveVideoMap.put(113518240, 3);
		liveVideoMap.put(113523612, 3);
		liveVideoMap.put(113550680, 3);
		liveVideoMap.put(113551567, 3);
		liveVideoMap.put(113591580, 3);
		liveVideoMap.put(113685333, 3);
		liveVideoMap.put(113703909, 3);
		liveVideoMap.put(113727507, 3);
		liveVideoMap.put(113761383, 3);
		liveVideoMap.put(113841742, 3);
		liveVideoMap.put(113845175, 3);
		liveVideoMap.put(113869506, 3);
		liveVideoMap.put(113898949, 3);
		liveVideoMap.put(113906649, 3);
		liveVideoMap.put(113910641, 3);
		liveVideoMap.put(113950955, 3);
		liveVideoMap.put(113977554, 3);
		liveVideoMap.put(113977795, 3);
		liveVideoMap.put(113980324, 3);
		liveVideoMap.put(113981413, 3);
		liveVideoMap.put(113985680, 3);
		liveVideoMap.put(114003774, 3);
		liveVideoMap.put(114024347, 3);
		liveVideoMap.put(114027885, 3);
		liveVideoMap.put(114061047, 3);
		liveVideoMap.put(114063336, 3);
		liveVideoMap.put(114121428, 3);
		liveVideoMap.put(114122320, 3);
		liveVideoMap.put(114148075, 3);
		liveVideoMap.put(114171940, 3);
		liveVideoMap.put(114178517, 3);
		liveVideoMap.put(114180207, 3);
		liveVideoMap.put(114182039, 3);
		liveVideoMap.put(114188055, 3);
		liveVideoMap.put(114188858, 3);
		liveVideoMap.put(114223587, 3);
		liveVideoMap.put(114267893, 3);
		liveVideoMap.put(114316844, 3);
		liveVideoMap.put(114319440, 3);
		liveVideoMap.put(114326862, 3);
		liveVideoMap.put(114338999, 3);
		liveVideoMap.put(114359519, 3);
		liveVideoMap.put(114371228, 3);
		liveVideoMap.put(114380136, 3);
		liveVideoMap.put(115072872, 3);
	}
	
	private static Logger logger = Logger.getLogger(UserService.class);	
    
    /**
     * 判断用户是否是真实主播
     * @param userId 用户ID
     * @return true - 是主播， false - 不是主播
     */
    public static boolean isRealActor(int userId) {
        RoomInfo roomInfo = RoomService.getRoomInfo(userId);
        if (roomInfo != null) {
            return true;
        }
        return false;
    }
	
	/**
	 * 判断用户是否带主播属性
	 * @return true/false
	 */
	public static boolean isActor(int userId) {
		return isRealActor(userId);
	}
	
	/**
	 * 获取用户昵称
	 * @param userId
	 * @return
	 */
	public static String getUserNickname(int userId) {
		String nickname = null;
		try {
		    UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
		    if (userProfile != null) {
		        nickname = userProfile.getNickName();
		    }
		} catch (Exception e) {
		    logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
		}
		
		return nickname;
	}
	
	private static ConcurrentHashMap<Class<?>, MethodAccess> accessMap = new ConcurrentHashMap<Class<?>, MethodAccess>();
	
	private static ConcurrentHashMap<Class<?>, List<String>> methodMap = new ConcurrentHashMap<Class<?>, List<String>>();

	public static <T> List<T> addUserExtra(List<T> tList) {
		if (tList == null || tList.size() <= 0) {
			return null;
		}
		MethodAccess access = null;
		Method[] methods = null;
		List<Integer> userIdList = new ArrayList<Integer>();
		List<String> methodName = new ArrayList<String>();
		Class<?> clazz = tList.get(0).getClass();
		try {
			if (methodMap.containsKey(clazz)) {
				methodName = methodMap.get(clazz);
			} else {
				methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					methodName.add(method.getName());
				}
				methodMap.putIfAbsent(clazz, methodName);
			}
			if (accessMap.containsKey(clazz)) {
				access = accessMap.get(clazz);
			} else {
				access = MethodAccess.get(clazz);
				accessMap.putIfAbsent(clazz, access);
			}
			for (T t : tList) {
				Integer userId = (Integer) access.invoke(t, "getUserId");
				if (userId != null) {
					userIdList.add(userId);
				}
			}
			List<UserProfile> profileList = getUserProfileAll(userIdList);
			if (profileList != null && profileList.size() > 0) {
				Map<Integer, UserProfile> profileMap = new HashMap<Integer, UserProfile>();
				for (UserProfile userProfile : profileList) {
					profileMap.put(userProfile.getUserId(), userProfile);
				}
				for (T t : tList) {
					int userId = (int) access.invoke(t, "getUserId");
					if (profileMap.containsKey(userId) && profileMap.get(userId) != null) {
						if (methodName.contains("setActorTag")) {
							access.invoke(t, "setActorTag", profileMap.get(userId).getIsActor());
						}
						if (methodName.contains("setPortrait_path_original") && profileMap.get(userId).getPortrait() != null) {
							access.invoke(t, "setPortrait_path_original", profileMap.get(userId).getPortrait());
						}
						if (methodName.contains("setNickname") && profileMap.get(userId).getNickName() != null) {
							access.invoke(t, "setNickname", profileMap.get(userId).getNickName());
						}
						if (methodName.contains("setGender")) {
							access.invoke(t, "setGender", profileMap.get(userId).getGender());
						}
						if (methodName.contains("setSignature") && profileMap.get(userId).getSignature() != null) {
							access.invoke(t, "setSignature", profileMap.get(userId).getSignature());
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			logger.error("reflect java bean, " + clazz + " : {" + tList + "} catched exception", e);
		}
		return tList;
	}
	
	public static UserRegistry getUserRegistryInfo(int userId) {
		try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			return userService.getUserRegistry(userId);
		} catch (Exception e) {
			logger.error("call KkUserService getUserRegistry catched exception, userId : " + userId, e);
		}
		return null;
	}
	
	/**
     * 检查用户昵称是否存在
     * @param nickname
     * @return true 存在 false 不存在
     */
    public static boolean checkNicknameRepeat(String nickname, int userId) {
        if (StringUtil.strIsNull(nickname)) {
            return true;
        }
        
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            if (userService.isNicknameExist(nickname)) {
                UserProfile userProfile = userService.getUserProfile(userId);
                if (userProfile != null && userProfile.getNickName() != null && userProfile.getNickName().equals(nickname)) {
                    return false;
                }
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Fail to execute User.checkNickname sql, nickname " + nickname, e);
        }
        
        return true;
    }
    
    /**
     * 检查用户昵称是否修改
     * @param nickname
     * @return
     */
    public static boolean checkNicknameChange(String nickname, int userId) {
        if (StringUtil.strIsNull(nickname)) {
            return false;
        }
        
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile != null && userProfile.getNickName() != null && !nickname.equals(userProfile.getNickName())) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Fail to execute UserService.checkNicknameChange, nickname " + nickname, e);
        }
        
        return false;
    }
	
	/**
	 * 从内存数据库中读出用户richLevel
	 * @param consumetotal 等级值
	 * @return
	 */
	public static RichLevel getRichLevel(long consumeTotal) {
		RichLevel richLevel = new RichLevel();
		
		KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
		UserLevel userLevel = kkUserService.getUserLevelByConsumeTotal(consumeTotal);
		if (userLevel != null) {
			if (userLevel.getRichlevel() != null) {
				richLevel.setLevel(userLevel.getRichlevel());
			}
			if (userLevel.getMaxpoint() != null) {
				richLevel.setMaxValue(userLevel.getMaxpoint());
			}
			if (userLevel.getMinpoint() != null) {
				richLevel.setMinValue(userLevel.getMinpoint());
			}
		}
		
		return richLevel;
	}
	
	/**
	 * 从内存数据库中读出用户actorLevel
	 * @param earntotal 等级值
	 * @return
	 */
	public static ActorLevel getActorLevel(long earnTotal) {
		ActorLevel actorLevel = new ActorLevel();
		
		KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
		com.melot.kkcore.user.api.ActorLevel level = kkUserService.getActorLevelByEarnTotal(earnTotal);
		if (level != null) {
			if (level.getActorlevel() != null) {
				actorLevel.setLevel(level.getActorlevel());
			}
			if (level.getMaxpoint() != null) {
				actorLevel.setMaxValue(level.getMaxpoint());
			}
			if (level.getMinpoint() != null) {
				actorLevel.setMinValue(level.getMinpoint());
			}
		}
		
		return actorLevel;
	}
    
	/**
	 * 从内存数据库中读出用户richLevel
	 * @param userId
	 * @return
	 */
	public static int getRichLevel(int userId) {
		try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile != null) {
                return userProfile.getUserLevel();
            }
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
        }
		return 0;
	}
	
	public static UserStaticInfo getStaticInfo(int userId) {
		try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserStaticInfo userStatic = userService.getUserStaticInfo(userId);
            return userStatic;
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
        }
		return null;
	}
	
	public static Map<String, Integer> getUserLevelAll(int userId) {
		Map<String, Integer> userLevel = null;
        try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			UserProfile userProfile = userService.getUserProfile(userId);
			if (userProfile != null) {
				userLevel = new HashMap<String, Integer>();
				userLevel.put("actorLevel", userProfile.getActorLevel());
				userLevel.put("richLevel", userProfile.getUserLevel());
			}
		} catch (Exception e) {
			logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
		}
        return userLevel;
	}
	
	public static List<UserProfile> getUserProfileAll(List<Integer> userIds) {
        try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			return userService.getUserProfileBatch(userIds);
		} catch (Exception e) {
			logger.error("fail to get KkUserService.getUserProfileBatch, userIds: " + userIds, e);
		}
        return null;
	}
	
	/**
	 * 从内存数据库中读出用户actorLevel
	 * @param userId
	 * @return
	 */
	public static int getActorLevel(int userId) {
       try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile != null) {
                return userProfile.getActorLevel();
            }
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
        }
        return 0;
	}
    
    /**
     * 更新 MongoDB 中 dailyCosume 当日的 consumetotal
     * @param userId
     * @return
     */
    public static boolean updateStarLevel(int userId, long consumetotal) {
    	try {
	        StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
	        long newConsume =  starService.updateStarLevel(userId, consumetotal);
	        if (newConsume > 0) {
				return true;
			}
    	}catch (Exception e) {
    		 logger.error("fail to get StarService.updateStarLevel, userId: " + userId+",consumetotal:"+consumetotal, e);
    	}
        return false;
    }
    
    /**
     * 从内存数据库的dailyCosume中获取userConsume,统计用户7天总消费额计算出用户starLevel
     * @param userId
     * @return
     */
    public static int getStarLevel(int userId) {
    	int level = 0;
    	try {
    		StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
    	    com.melot.common.driver.domain.StarInfo starInfo =  starService.getStarInfo(userId);
	        if (starInfo != null) {
	        	level = starInfo.getLevel();
			}
    	}catch (Exception e) {
    		 logger.error("fail to get StarService.getStarInfo, userId: " + userId, e);
    	}
        return level;
    }
    
    /**
     * 从内存数据库的dailyCosume中获取userConsume,统计用户7天总消费额计算出用户starLevel
     * @param userId
     * @return
     */
    public static StarInfo getStarInfo(int userId) {
        StarInfo starInfo = new StarInfo();
        try {
	        StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
	        com.melot.common.driver.domain.StarInfo redisStarInfo =  starService.getStarInfo(userId);
	        try {
	        	if (redisStarInfo != null) {
	        		BeanUtils.copyProperties(starInfo, redisStarInfo);
				}
	        } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
   		 	logger.error("fail to get StarService.getStarInfo, userId: " + userId, e);
        }
        return starInfo;
    }
    
    public static UserInfoDetail getUserInfoDetail(int userId) {
    	try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			UserInfoDetail userInfoDetail = userService.getUserDetailInfo(userId);
			if (userInfoDetail != null && userInfoDetail.getRegisterInfo() != null) {
			    return userInfoDetail;
			} else {
			    return null;
			}
		} catch (Exception e) {
			logger.error("call KkUserService getUserStaticInfo catched exception, userId : " + userId, e);
		}
    	return null;
    }
    
    /**
     * 功能描述：查询私有用户信息
     * 
     * @return userInfo
     */
    public static UserInfo getUserInfo(int userId) {
    	try {
    		UserInfoDetail userInfoDetail = getUserInfoDetail(userId);
			if (userInfoDetail != null) {
				UserInfo userInfo = (UserInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getUserInfo", userId);
				if (userInfo == null) {
					userInfo = new UserInfo();
				}
				if (userInfoDetail.getProfile() != null ) {
					UserProfile userProfile = userInfoDetail.getProfile();
					userInfo.setNickname(userProfile.getNickName());
					userInfo.setGender(userProfile.getGender());
					userInfo.setPhone(userProfile.getPhoneNum());
					userInfo.setSignature(userProfile.getSignature());
					userInfo.setIntroduce(userProfile.getIntroduce());
					userInfo.setBirthday(userProfile.getBirthday());
					userInfo.setActorTag(userProfile.getIsActor());
					userInfo.setActorLevel(userProfile.getActorLevel());
					userInfo.setRichLevel(userProfile.getUserLevel());
					if (liveVideoMap.containsKey(userId) && liveVideoMap.get(userId) != null) {
						userInfo.setLiveVideoQuality(liveVideoMap.get(userId));
					} else {
						userInfo.setLiveVideoQuality(1);
					}
				}
				if (userInfoDetail.getRegisterInfo() != null) {
					UserRegistry userRegistry = userInfoDetail.getRegisterInfo();
					userInfo.setCity(userRegistry.getCityId());
					userInfo.setRegisterTime(new Date(userRegistry.getRegisterTime()));
					userInfo.setOpenPlatform(userRegistry.getOpenPlatform());
				}
				if (userInfoDetail.getAssets() != null) {
					userInfo.setShowMoney((int)userInfoDetail.getAssets().getShowMoney());
				}
				return userInfo;
			}
		} catch (Exception e) {
			 logger.error("Fail to execute getUserInfo sql, userId " + userId, e);
		}
        return null;
    }
    
    /**
     * 获取用户官方号类型
     * 
     * @param userId
     * @return
     */
    public static Integer getUserAdminType(int userId) {
        Integer adminType = null;
        try {
			AdminDataService adminDataService = (AdminDataService) MelotBeanFactory.getBean("adminDataService");
			Result<OfficialIdInfo> result =  adminDataService.officialIdInfoGetInfo(userId);
			if (result.getCode() == AdminApiTagCodes.SUCCESS){
				adminType = result.getData().getType();
			}else {
				logger.error("AdminDataService:"+result.getMsg());
			}
        } catch (Exception e) {
            logger.error("UserService.getUserAdminType(" + "userId:" + userId + ") execute exception.", e);
        }
        return adminType;
    }
    
    /**
     * 功能描述：查询私有用户信息
     * 
     * @return userInfo
     */
    public static UserProfile getUserInfoNew(int userId) {
        return com.melot.kktv.service.UserService.getUserInfoV2(userId);
    }
    
    /**
     * 功能描述：查询用户密码
     * 
     * @return userInfo
     */
    public static String getUserPassword(int userId) {
        String result = null;
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserRegistry userRegistry = userService.getUserRegistry(userId);
            result = userRegistry.getLoginPwd();
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserRegistry, userId: " + userId, e);
        }
        
        return result;
    }
    
    /**
     * 获取用户拥有会员
     * @param userId
     * @return
     */
    public static List<Integer> getUserProps(int userId) {
    	try {
			VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
			return vipService.getUserProp(userId);
		} catch (Exception e) {
			logger.error("call VipService getUserProp catched exception, userId : " + userId, e);
		}
    	return null;
    }
    
    /**
     * 判断是否能被邀请(注册流程)
     * @return
     */
    public static int canInvite(String deviceUId) {
    	if (deviceUId != null) {
    		try {
        		return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getCanInvite", deviceUId);
        	} catch (Exception e) {
        		logger.error("UserService.canInvite( " + deviceUId + ") execute exeception", e);
        	}
    	}
    	return 0;
    }
    
    /**
     * 获取主播K豆
     * @param actorId
     * @return
     */
    public static Long getActorKbi(int actorId) {
        Long kbi = null;
        try {
            kbi = (Long) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG).queryForObject("Actor.getActorKbi", actorId);
        } catch (SQLException e) {
            logger.error("fail to execute sql (Actor.getActorKbi), actorId :" + actorId, e);
        }
        
        return kbi;
    }
    
    /**
     * 获取主播收益列表
     * @param actorId
     * @param offset
     * @param count
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<ActorProfit> getActorProfitList(int actorId, int offset, int count) {
        List<ActorProfit> result = new ArrayList<>();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("actorId", actorId);
            map.put("offset", offset);
            map.put("count", count);
            result = (List<ActorProfit>) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG).queryForList("Actor.getActorProfitList", map);
        } catch(Exception e) {
            logger.error("fail to execute sql (Actor.getActorProfitList, actorId :" + actorId + "offset:" + offset + "count:" + count + ")", e);
        }
        
        return result;
    }
    
    /**
     * 获取主播收益列表总数
     * @param actorId
     * @return
     */
    public static int getActorProfitCount(int actorId) {
        int count = 0;
        try {
            count = (int) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG).queryForObject("Actor.getActorProfitCount", actorId);
        } catch (SQLException e) {
            logger.error("fail to execute sql (Actor.getActorProfitCount), actorId :" + actorId, e);
        }
        
        return count;
    }
    
    public static boolean exchangeKbi(int actorId, int exchangeAmount) {
        boolean result = false;
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("actorId", actorId);
            map.put("exchangeAmount", exchangeAmount);
            String tagcode = null;
            tagcode = (String) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG).queryForObject("Actor.exchangeKbi", map);
            if (TagCodeEnum.SUCCESS.equals(tagcode)) {
                result = true;
            }
        } catch (SQLException e) {
            logger.error("fail to execute Actor.exchangeKbi(actorId :" + actorId + ", " + "exchangeAmount:" + exchangeAmount + ")", e);
        }
        
        return result;
    }
    
    public static void insertKbiHist(int userId, int exchangeAmount) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", userId);
            map.put("kbi", exchangeAmount);
            map.put("toUser", userId);
            map.put("note", "主播K豆兑换");
            map.put("appId", AppIdEnum.AMUSEMENT);
            SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertHist", map);
        } catch(Exception e) {
            logger.error("User.insertHist(userId:" + userId + "kbi:" + exchangeAmount + ") return exception.", e);
        }
    }
	
	/**
	 * 是否为黑名单用户 (illeagle user)
	 * @param userId
	 * @return
	 */
	public static boolean blackListUser(int userId) {
        try {
            return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.isInUserBlackList", userId) > 0;
        } catch (SQLException e) {
            logger.error("UserService.blackListUser( " + userId + ") execute exeception", e);
        }
        
        return false;
    }
	
	public static boolean isValidOperator(int operatorId) {
		try {
            return (Integer) SqlMapClientHelper.getInstance(DB.BACKUP).queryForObject("Index.isValidOperator", operatorId) > 0;
        } catch (SQLException e) {
            logger.error("UserService.isValidOperator( " + operatorId + ") execute exeception", e);
        }
        
        return false;
	}
	
	public static String getMD5Password(String ps) {
		if (!StringUtil.strIsNull(ps) && ps.length() < ConstantEnum.passwordSize) {
			return CommonUtil.md5(ps);
		}
		return ps;
	}
	
	public static int getUserSmsSwitch(int userId) {
		try {
			OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
			return otherLoginService.getUserSmsSwitch(userId);
		} catch (Exception e) {
			logger.error("UserService.getUserSmsSwitch( " + userId + " ) execute exception", e);
			return 1;
		}
	}
	
	public static boolean getUserSmsSwitchState(int userId) {
		return getUserSmsSwitch(userId) == 0;
	}
	
	public static boolean changeUserSmsSwitch(int userId, int state) {
		try {
			OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
			return otherLoginService.changeUserSmsSwitch(userId, state);
		} catch (Exception e) {
			logger.error("UserService.changeUserSmsSwitch (userId : " + userId + ", state : " + state + " ) execute exception", e);
			return false;
		}
	}
	
	public static boolean insertTempUserPassword(int userId, String password) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("password", password);
		map.put("dtime", new Date());
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertUserTempPassword", map);
		} catch (SQLException e) {
			logger.error("UserService.insertTempUserPassword (userId : " + userId + ", password : " + password + " ) execute exception", e);
			return false;
		}
		return true;
	}
	
	public static boolean getGuestFirstRecord(int userId) {
		try {
			OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
			return otherLoginService.getGuestFirstRecord(userId) > 0;
		} catch (Exception e) {
			logger.error("UserService.getGuestFirstRecord(userId : " + userId + " ) execute exception", e);
			return false;
		}
	}
	
	public static boolean insertGuestFirstRecord(int userId, int platform, int appId, int channelId, String deviceUId, Date dtime) {
		try {
			OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
			return otherLoginService.insertGuestFirstRecord(userId, platform, appId, channelId, deviceUId, dtime);
		} catch (Exception e) {
			logger.error("UserService.insertGuestFirstRecord (userId : " + userId + ", platform : " + platform +
					", appId : " + appId + ", channelId : " + channelId + ", deviceUId : " + deviceUId + ", dtime : " + dtime + " ) execute exception", e);
			return false;
		}
	}

	/**
	 * 获取用户秀币
	 *
	 * @param userId 用户ID
	 * @return
	 */
	public static long getUserMoney(int userId) {
		try {
			com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
			if (userService != null) {
				UserAssets userAssets = userService.getUserAssets(userId);
				if (userAssets != null) {
					return userAssets.getShowMoney();
				}
			}
		} catch (Exception e) {
			logger.error("fail to execute KkUserService.getUserAssets, userId: " + userId, e);
		}
		return 0;
	}

	/**
     * 获取用户游戏币
     * @param userId
     * @return
     */
    public static long getUserGameMoney(int userId) {
        try {
            com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
            if (userService != null) {
                UserGameAssets  userGameAssets  = userService.getUserGameAssets(userId);
                if (userGameAssets != null && userGameAssets.getGameMoney() != null) {
                    return userGameAssets.getGameMoney(); 
                }
            }
        } catch (Exception e) {
            logger.error("fail to execute KkUserService.getUserGameAssets, userId: " + userId, e);
        }
        return 0;
    }
    
    /**
     * 获取用户金币
     * @param userId
     * @return
     */
    public static long getUserGoldCoin(int userId) {
        try {
            GoldcoinService goldcoinService = (GoldcoinService) MelotBeanFactory.getBean("goldcoinService");
            if (goldcoinService != null) {
                UserGoldAssets  userGoldAssets  = goldcoinService.getUserGoldAssets(userId);
                if (userGoldAssets != null) {
                    return userGoldAssets.getGoldCoin(); 
                }
            }
        } catch (Exception e) {
            logger.error("fail to execute goldcoinService.getUserGoldAssets, userId: " + userId, e);
        }
        return 0;
    }
    
    public static boolean checkUserIdentify(int userId) {
        boolean result = false;
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserInfoDetail userInfoDetail = userService.getUserDetailInfo(userId);
            UserProfile userProfile = null;
            UserRegistry userRegistry = null;
            if (userInfoDetail != null) {
                userProfile = userInfoDetail.getProfile();
                userRegistry = userInfoDetail.getRegisterInfo();
            }
            if (userProfile == null || StringUtil.strIsNull(userProfile.getIdentifyPhone())) {
                if (userRegistry != null) {
                    int openPlatform = userRegistry.getOpenPlatform();
                    if (openPlatform == LoginTypeEnum.QQ || openPlatform == LoginTypeEnum.WEIBO || openPlatform == LoginTypeEnum.WEIXIN || openPlatform == LoginTypeEnum.ALIPAY) {
                        result = true;
                    }
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            logger.error("call KkUserService getUserStaticInfo catched exception, userId : " + userId, e);
        }
        return result;
    }
    
    /**
     * 校验用户有没有被封号
     * @param userId
     * @return  true—封号，false—没有
     */
    public static boolean checkUserIsLock(int userId) {
        boolean isLock = false;
        try {
            AccountSecurityService accountSecurityService = MelotBeanFactory.getBean("accountSecurityService", AccountSecurityService.class);
            if (accountSecurityService.isLock(userId)) {
                isLock = true;
            }
        } catch (Exception e) {
            logger.error("call AccountSecurityService.isLock(" + userId + ") execute exception", e);
        }
        
        return isLock;
    }
}
