package com.melot.kkcx.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.melot.client.api.TimSystemService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: SendTimMsgService
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月14日 下午5:34:56
 */
public class SendTimMsgService implements Runnable {
    
    private static Logger logger = Logger.getLogger(SendTimMsgService.class);
    private static BlockingQueue<Map<String, Object>> factoryQueue = new LinkedBlockingQueue<Map<String, Object>>();

    private static Thread instance = new Thread(new SendTimMsgService());
    static {
        instance.start();
    }

    
    public static void add(String userId, String kkAdmin, String msg) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("kkAdmin", kkAdmin);
        map.put("msg", msg);
        try {
            factoryQueue.offer(map);
        } catch (Exception ex) {
            logger.error("SendMsgService execute factoryQueue.offer,userId:" + userId + ",admin:" + kkAdmin + ",msg:" + msg + " ,exception.", ex);
        }
    }
    
    public static void add(String userId, String kkAdmin, String msg, boolean isPush) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("kkAdmin", kkAdmin);
        map.put("msg", msg);
        map.put("isPush", isPush);
        try {
            factoryQueue.offer(map);
        } catch (Exception ex) {
            logger.error("SendMsgService execute factoryQueue.offer,userId:" + userId + ",admin:" + kkAdmin + ",msg:" + msg + ",isPush:" + isPush + " ,exception.", ex);
        }
    }

    @Override
    public void run() {
        TimSystemService timService = (TimSystemService) MelotBeanFactory.getBean("timSystemService");
        Map<String, Object> map = null;
        List<String> tos;
        while (true) {
            try {
                while ((map = factoryQueue.poll()) != null) {
                	String userId = map.get("userId").toString();
                	String msg = map.get("msg").toString();
                	String  kkAdmin = map.get("kkAdmin").toString();
                	
                	if (map.containsKey("isPush")) {
                		boolean isPush = (Boolean) map.get("isPush");
                        
                        tos = new ArrayList<String>();
                        tos.add(userId);
                        timService.sendKKUserMsg(kkAdmin, tos, URLEncoder.encode(msg, "UTF-8"), isPush);
					}else{
						timService.sendSystemMsg(kkAdmin, userId, URLEncoder.encode(msg, "UTF-8"));
					}
                }
                Thread.sleep(1000);
            } catch (Exception ex) {
                if (map != null) {
                    logger.error("SendMsgService.add(" + new Gson().toJson(map) + ") exception.", ex);
                } else {
                    logger.error("SendMsgService execute exception.", ex);
                }
            }
        }
    }
}