package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.model.GuessInfo;
import com.melot.kktv.model.GuessOptionInfo;
import com.melot.kktv.model.UserGuessInfo;
import com.melot.kktv.redis.GuessSource;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class GuessService {
	private static Logger logger = Logger.getLogger(GuessService.class);
	
//	@SuppressWarnings("deprecation")
//    public static int betGuessOption(int userId, 
//			int guessId, int optionId, int showMoney) throws Exception {
//		
//		int iRet = 40010207;//
//		
//		if(userId <= 0 || guessId <= 0 || optionId <= 0 || showMoney < 0)
//			return 40010205;//参数错误，不存在  --错误的竞猜参数
//			
//		SqlMapSession session = null;
//		//check guessId/
//		try{
//			do{
//				session = SqlMapClientHelper.getInstance(DB.MASTER).openSession();
//				//0.判断是否为主播
//				if (UserService.isActor(userId)) {
//					iRet = 40010208;//主播不能参与
//					break;
//				}
//				//1.判断用户是否已经下注
//				Map<Object, Object> map = new HashMap<Object, Object>();
//				map.put("userId", userId);
//				map.put("guessId", guessId);
//				Integer c = (Integer) session.queryForObject("Guess.isUserGuessed", map);
//				if(c >= 1){
//					iRet = 40010203;//用户已经下注
//					break;
//				}
//				//2. 判断Guess和Option是否存在, 判断是否还能下注
//				map.put("optionId", optionId);
//				Date d = (Date) session.queryForObject("Guess.isGuessOptionExists", map);
//				if(d == null){
//					iRet = 40010205; //竞猜不存在或者选项不存在--错误的竞猜参数
//					break;
//				}
//				//3.扣币  如果存在该userid则更新;否则不更新
//				DBObject queryDBObject = new BasicDBObject();
//				queryDBObject.put("userId", userId);
//				queryDBObject.put("showMoney", new BasicDBObject("$gte",showMoney));
//				DBObject updateDBObject = new BasicDBObject();
//				updateDBObject.put("showMoney", -showMoney);
//				DBObject updatedDBObject = new BasicDBObject("$inc", updateDBObject);
//				WriteResult wr = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.USERLIST)
//						.update(queryDBObject, updatedDBObject, false, false, WriteConcern.NORMAL);
//				if(wr.getError()!=null || wr.getN() < 1){
//					iRet = 40010202; //用户秀币不足
//					break;
//				}
//
//				//4. 创建 下注消费记录
//				try {
//					session.startTransaction();
//					c = (Integer) session.queryForObject("Guess.generateHistId");
//					map.put("histId", c);
//					map.put("ntype", 11);//11 is for guess
//					map.put("showMoney", showMoney);
//					map.put("typeDesc", "竞猜");
//					map.put("product", guessId);
//					map.put("count", 1);
//					session.insert("Guess.insertConsumeHistory", map);
//					session.insert("Guess.insertGuessHistory", map);
//					session.commitTransaction();
//				}catch(Exception e){
//					logger.error("未能正常执行数据库语句  userId:"+userId + " guessId:" + guessId +
//							" ptionId:" + optionId + " showMoney:" + showMoney, e);// Make safe: 钱扣了，数据库失败
//					iRet = 40010206; //未能正常执行数据库语句
//					break;
//				}finally{
//					try {
//						session.endTransaction();
//					} catch (SQLException e) {
//						logger.error("未能正常结束数据库操作事务", e);
//					}
//				}
//				
//				//5. 更新Redis中Guess记录
//				String sOption = String.format("{\"optionId\":%d, \"amount\":%d}", optionId, showMoney);
//				GuessSource.setUserOption(String.valueOf(userId), String.valueOf(guessId), 
//						sOption, d.getTime()/1000);
//				//6. 更新Redis中下注总数
//				GuessSource.incOptionUserCount(String.valueOf(guessId), String.valueOf(optionId), 
//						d.getTime()/1000);
//				
//				iRet = 0;
//			}while(false);
//		}catch(Exception e){
//			logger.error("系统错误：", e);
//		}finally{
//			if(session != null)
//				session.close();
//		}
//		
//		return iRet;
//	}
	
	@SuppressWarnings("unchecked")
    public static List<GuessInfo> getGuessList(int userId, String topic, int count, int type, int platform) throws Exception {
		List<GuessInfo> guessList = null;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("topic", topic);
			map.put("count", count);
			map.put("type", type);
			// get guess list from oracle
			guessList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Guess.selectTopicGuessList", map);
			if (guessList != null && guessList.size() > 0) {
				for (GuessInfo guessInfo : guessList) {
					// get option list from redis
					JsonArray options = GuessSource.getOptionList(String.valueOf(guessInfo.getGuessId()));
					if (options == null || options.size() == 0) {
						// get option list from oracle
						List<GuessOptionInfo> optionList = SqlMapClientHelper.getInstance(DB.MASTER)
								.queryForList("Guess.selectOptionList", guessInfo.getGuessId());
						if (optionList != null && optionList.size() > 0) {
							try {
								String optionListStr = new Gson().toJson(optionList);
								options = new JsonParser().parse(optionListStr).getAsJsonArray();
								// set option list to redis
								GuessSource.setOptionList(String.valueOf(guessInfo.getGuessId()),
										optionListStr, guessInfo.getEndTime()/1000);
							} catch (Exception e) {
								logger.error("Convert oracle option list to json array", e);
								continue;
							}
						} else {
							continue;
						}
					}
					guessInfo.setOptions(options);
					int userTotalCount = 0;
					for (int i = 0; i < options.size(); i++)
						userTotalCount += options.get(i).getAsJsonObject().get("userCount").getAsInt();
					guessInfo.setUserTotalCount(userTotalCount);
					if (userId > 0) {
						// get user option from redis
						JsonObject userOption = GuessSource.getUserOption(String.valueOf(userId), String.valueOf(guessInfo.getGuessId()));
						guessInfo.setUserOption(userOption);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Fail to execute sql", e);
		}
		return guessList;
	}

	@SuppressWarnings("unchecked")
    public static List<UserGuessInfo> getUserGuessList(int userId, int platform) {
		List<UserGuessInfo> userGuessList = null;
		try {
			// get guess list from oracle
			userGuessList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Guess.selectUserGuessList", userId);
		} catch (SQLException e) {
			logger.error("Fail to execute sql", e);
		}
		return userGuessList;
	}
	
}
