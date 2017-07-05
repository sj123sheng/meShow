package com.melot.kkgame.logger;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Hadoop统计
 * @author Administrator
 *
 */
public class HadoopLogger {

	private static Logger hadoopLogger = Logger.getLogger("hadoopLogger");
	
	public static void signInLog(int userId, int roomid, Date recordTime, int appId, int platform) {
		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(recordTime);
        } catch (Exception e) {}
		
		hadoopLogger.info("user_sign_v1:"
				+ (userId > 0 ? userId + "" : "") + "^" 
				+ (roomid > 0 ? roomid + "" : "") + "^"
				+ (dateString == null ? "" : dateString + "") + "^"
				+ (appId > 0 ? appId + "" : "") + "^"
				+ (platform > 0 ? platform + "" : "") + "^");
	}
	
	
	/**
	 * 用户参与投注日志 
	 * 格式  user_gamble_v1:$userId^$roomid^$recordTime^$amount^$appId^$platform^
	 */
	public static void gambleLog(int userId, int roomid, Date recordTime, Integer amount, int appId, int platform) {
        String dateString = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(recordTime);
        } catch (Exception e) {}
        
        hadoopLogger.info("user_gamble_v1:"
                + (userId > 0 ? userId : "") + "^" 
                + (roomid > 0 ? roomid : "") + "^"
                + (amount > 0 ? amount :"") + "^"
                + (dateString == null ? "" : dateString + "") + "^"
                + (appId > 0 ? appId : "") + "^"
                + (platform > 0 ? platform  : "") + "^");
    }


    public static Logger getHadoopLogger() {
        return hadoopLogger;
    }
}
