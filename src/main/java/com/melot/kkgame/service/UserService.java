package com.melot.kkgame.service;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.melot.kkgame.domain.GameUserInfo;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.DBEnum;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class UserService {
	
	private static Logger logger = Logger.getLogger(UserService.class);
	
	/***
     * 获取KKGame 主播获取礼物信息 
     * 
     */
    public static GameUserInfo getUserActorInfoByUserId(int userId){
        try {
            GameUserInfo gameUserInfo = (GameUserInfo) SqlMapClientHelper.getInstance(DBEnum.KKGAME_PG)
                    .queryForObject("User.getUserActorInfoByUserId", userId);
            if (gameUserInfo != null) {
                gameUserInfo.setOpusCout(LiveVideoService.getUserOpusCount(userId, AppIdEnum.GAME, 1, false));
                return gameUserInfo;
            }
        } catch (SQLException e) {
            logger.error("Fail to execute getUserActorInfoByUserId sql, userId " + userId, e);
        }
        return new GameUserInfo();
    }

}