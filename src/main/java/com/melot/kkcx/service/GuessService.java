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
