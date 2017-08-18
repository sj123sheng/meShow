package com.melot.kktv.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.melot.common.melot_jedis.RedisDataSourceFactory;
import com.melot.kktv.lottery.WeeklyCheckIn;
import com.melot.kktv.lottery.arithmetic.LotteryArithmetic;
import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache.CacheTypeEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TextFilter;
import com.melot.kktv.util.TlsSig;
import com.melot.kktv.util.WordsFilter;
import com.melot.kktv.util.confdynamic.InitConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.melot.kktv.util.mongodb.MongoDBInstance;
import com.melot.kktv.util.redis.RedisConfigHelper;
import com.melot.kktv.util.cache.EhCache;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ConfigContextListener implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
        // 抽奖销毁
        LotteryArithmetic.destroy();
        
	    // SqlMapClient destroy
        SqlMapClientHelper.destroy();
        
		// MongoDB destroy
		CommonDB.close();
		
		// Redis destroy
		RedisConfigHelper.destroy();
		
		RedisDataSourceFactory.getGlobalInstance().destroy();
		
		// 销毁Spring容器
		MelotBeanFactory.close();
		
		// 销毁缓存
		EhCache.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
	    // 初始化Spring容器
		MelotBeanFactory.init("classpath*:/conf/spring-bean-container*.xml");

		initSqlMapClientConnections();
		
		initMongoConnections();
		
		String configLocation = event.getServletContext().getInitParameter("configLocation");
		if (configLocation != null) {
			ConfigHelper.initConfig(event.getServletContext().getRealPath("/") + configLocation);
		}
		String log4jConfigLocation = event.getServletContext().getInitParameter("log4jConfigLocation");
		if (log4jConfigLocation != null) {
			DOMConfigurator.configureAndWatch(event.getServletContext().getRealPath("/") + log4jConfigLocation);
		}
		String jedisPoolConfigLocation = event.getServletContext().getInitParameter("jedisPoolConfigLocation");
		if (jedisPoolConfigLocation != null) {
			RedisConfigHelper.init(event.getServletContext().getRealPath("/") + jedisPoolConfigLocation);

			RedisDataSourceFactory.getGlobalInstance().appendConfig(event.getServletContext().getRealPath("/") + jedisPoolConfigLocation);
		}
		String textFilterDataLocation = event.getServletContext().getInitParameter("textFilterDataLocation");
		if (textFilterDataLocation != null) {
			TextFilter.init(event.getServletContext().getRealPath("/") + textFilterDataLocation);
		}
		String wordsDictLocation = event.getServletContext().getInitParameter("wordsDictLocation");
		if (wordsDictLocation != null) {
			WordsFilter.init(event.getServletContext().getRealPath("/") + wordsDictLocation);
		}
		String tlsPemLocation = event.getServletContext().getInitParameter("tlsPemLocation");
		String tlsSoLocation = null;
		String osName = System.getProperty("os.name");
        if(osName.startsWith("Windows")) {
            tlsSoLocation = event.getServletContext().getInitParameter("tlsSoLocationWindows");
        }else if(osName.startsWith("Mac OS")){
            tlsSoLocation = event.getServletContext().getInitParameter("tlsSoLocationMac");
        }else {
            tlsSoLocation = event.getServletContext().getInitParameter("tlsSoLocationLinux");
        }
		if (tlsPemLocation != null && tlsSoLocation != null) {
			TlsSig.init(event.getServletContext().getRealPath("/") + tlsPemLocation, event.getServletContext().getRealPath("/") + tlsSoLocation);
		}
		
		// 初始化ehcache缓存
		EhCache.init();
		
		//init config
		String path = event.getServletContext().getRealPath("/") + configLocation;
		InitConfig.init(path);
		
		// 抽奖初始化
		Logger logger = Logger.getLogger(ConfigContextListener.class);
		LotteryArithmetic.init(CacheTypeEnum.REDIS, RedisConfigHelper.getJedisPool("LotteryCache"), null, null, logger);
        try {
            LotteryArithmetic.loadLotteryArithmeticByXml("conf/lottery_conf.xml", new WeeklyCheckIn());
            logger.error("Init Lottery success .......................................");
        } catch (Exception e) {
        	logger.error("Init Lottery exeception .......................................", e);
        }
	}
	
	/**
	 * init oracle mysql postgresql client connections
	 */
	private void initSqlMapClientConnections() {
		
		SqlMapClient masterSqlMapClient = (SqlMapClient) MelotBeanFactory.getBean("sqlMapClient_oracle_master");
		SqlMapClient backupSqlMapClient = (SqlMapClient) MelotBeanFactory.getBean("sqlMapClient_oracle_backup");
		SqlMapClient kkgameSqlMapClient = (SqlMapClient) MelotBeanFactory.getBean("sqlMapClient_pg_kkgame");
		SqlMapClient kkcxSqlMapClient = (SqlMapClient) MelotBeanFactory.getBean("sqlMapClient_pg_kkcx");
		
		SqlMapClientHelper.initSqlMapClient(DB.MASTER, masterSqlMapClient);
		SqlMapClientHelper.initSqlMapClient(DB.BACKUP, backupSqlMapClient);
		SqlMapClientHelper.initSqlMapClient(com.melot.kktv.util.DBEnum.KKGAME_PG, kkgameSqlMapClient);
		SqlMapClientHelper.initSqlMapClient(DB.MASTER_PG, MelotBeanFactory.getBean("sqlMapClient_pg_master", SqlMapClient.class));
		SqlMapClientHelper.initSqlMapClient(DB.SLAVE_PG, MelotBeanFactory.getBean("sqlMapClient_pg_slave", SqlMapClient.class));
		SqlMapClientHelper.initSqlMapClient(com.melot.kktv.util.DBEnum.KKCX_PG, kkcxSqlMapClient);
	}
    
	/**
	 * init mongodb connections
	 */
	private void initMongoConnections() {
		
		MongoDBInstance commonDB = (MongoDBInstance) MelotBeanFactory.getBean("commonDB");
		MongoDBInstance cacheDB = (MongoDBInstance) MelotBeanFactory.getBean("cacheDB");
		
		CommonDB.initInstance(CommonDB.COMMONDB, commonDB.getMongo());
		CommonDB.initInstance(CommonDB.CACHEDB, cacheDB.getMongo());
	}
}
