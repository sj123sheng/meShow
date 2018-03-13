/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kktv.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.iprepository.driver.domain.IpInfo;
import com.melot.module.iprepository.driver.service.IpRepositoryService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: CityUtil.java
 * <p>
 * Description:城市id映射城市名称工具类
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2016年1月21日 下午4:12:04
 */
public class CityUtil {

	private static final Logger logger = Logger.getLogger(CityUtil.class);

	/**
	 * 默认城市id：1，北京市
	 */
	private static final Integer DEFAULT_CITY_ID = 1;
	private static final Integer OVERSEA = 35;
	private static final String CENTRAL_CITY = "上海,北京,重庆,天津";
    public static final int DEFAULT_CHANNEL = 1;
	private static final Map<Integer, String> CENTRAL_CITY_ID = new HashMap<Integer, String>();
 	private static ConcurrentHashMap<Integer, String> cityMap = new ConcurrentHashMap<Integer, String>();
    private static ConcurrentHashMap<Integer, Integer> parentMap = new ConcurrentHashMap<Integer, Integer>();
    static {
    	CENTRAL_CITY_ID.put(1, "北京");
    	CENTRAL_CITY_ID.put(2, "上海");
    	CENTRAL_CITY_ID.put(3, "重庆");
    	CENTRAL_CITY_ID.put(30, "天津");
    }
    
	@SuppressWarnings("unchecked")
	private static void init() {
	    if (cityMap != null && cityMap.size() > 0 && parentMap != null && parentMap.size() > 0) {
            return;
        }
        
        try {
            List<Map<String, Object>> list = SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForList("Other.getCityInfo");
            if (list != null && list.size() > 0) {
                for (Map<String, Object> map : list) {
                	if (CENTRAL_CITY_ID.containsKey((Integer) map.get("parentId"))) {
                		cityMap.putIfAbsent((Integer) map.get("cityId"), CENTRAL_CITY_ID.get((Integer) map.get("parentId")));
                	} else {
                		cityMap.putIfAbsent((Integer) map.get("cityId"), (String) map.get("cityName"));
                	}
                    parentMap.putIfAbsent((Integer) map.get("cityId"), (Integer) map.get("parentId"));
                }
            }
        } catch (SQLException e) {
            logger.error("fail to execute sql Other.getCityInfo", e);
        }
	}

	/**
	 * 获取城市名称
	 * 
	 * @param cityId
	 * @return
	 */
    public static String getCityName(Integer cityId) {
        if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
        
        if (cityId != null && cityMap.containsKey(Math.abs(cityId))) {
            return cityMap.get(Math.abs(cityId));
        } else {
            return cityMap.get(DEFAULT_CITY_ID);
        }
    }

    /**
     * 获取城市名称
     * 
     * @param cityId
     * @return
     */
    public static Integer getParentCityIdNoDefault(Integer cityId) {
        if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
        
        if (parentMap != null && parentMap.containsKey(Math.abs(cityId))) {
            return parentMap.get(Math.abs(cityId));
        }
        
        return null;
    }

    /**
     * 获取城市名称
     * 
     * @param cityId
     * @return
     */
    public static int getParentCityId(Integer cityId) {
        if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
        
        if (parentMap != null && parentMap.containsKey(Math.abs(cityId))) {
            return parentMap.get(Math.abs(cityId));
        } else {
            return DEFAULT_CITY_ID;
        }
    }

    /**
     * 获取城市名称
     * 
     * @param cityId
     * @return
     */
    public static String getParentCityName(Integer cityId) {
        if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
        
        if (cityId != null && parentMap != null && parentMap.containsKey(Math.abs(cityId))) {
            return cityMap.get(parentMap.get(Math.abs(cityId)));
        } else {
            return cityMap.get(DEFAULT_CITY_ID);
        }
    }

	/**
	 * 根据ip获取城市id
	 * 
	 * @param ipAddr
	 * @return
	 */
	public static Integer getCityIdByIpAddr(String ipAddr) {
		if (StringUtil.strIsNull(ipAddr)) {
			return DEFAULT_CITY_ID;
		}
		if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
		try {
		    IpRepositoryService ipRepositoryService = MelotBeanFactory.getBean("ipRepositoryService", IpRepositoryService.class);
		    IpInfo ipInfo = ipRepositoryService.getIpInfo(ipAddr);
		    if (ipInfo != null) {
		        Integer cityId = null;
		        String city;
		        Integer provinceId;
		        if (ipInfo.getNationalCode() != null && ipInfo.getNationalCode().equals("86")) {
		        	if (ipInfo.getProvince() != null) {
		        		provinceId = getCityIdByCityName(ipInfo.getProvince());
		        		if (CENTRAL_CITY.contains(ipInfo.getProvince()) || ipInfo.getCity() == null) {
		        			return provinceId == null ? DEFAULT_CITY_ID : provinceId;
		        		} else {
		        			city = ipInfo.getCity();
		        			if (provinceId != null) {
		        				cityId = getCityIdByCityNameWithProvince(city, provinceId);
		        				if (cityId != null) {
		        					return cityId;
		        				} else {
		        					cityId = insertCityInfo(provinceId, city);
	        						if (cityId != null) {
	        							cityMap.putIfAbsent(cityId, city);
	        							parentMap.putIfAbsent(cityId, provinceId);
	        							return cityId;
	        						}
		        				}
		        			}
		        		}
		        	}
		        } else {
		            return OVERSEA;
		        }
		    }
        } catch (Exception e) {
            logger.error("ipRepositoryService.getIpInfo(" + ipAddr + ") execute exception.", e);
        }
		return DEFAULT_CITY_ID;
	}
	
	public static Integer insertCityInfo(int parentId, String name) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("parentId", parentId);
			map.put("name", name);
			return (Integer) SqlMapClientHelper.getInstance(DB.MASTER_PG).insert("Other.insertCityInfo", map);
		} catch (SQLException e) {
			logger.error("fail to execute sql insertCityInfo, parentId : " + parentId + ", cityName : " + name, e);
		}
		return null;
	}
	
	public static Integer getCityIdByCityNameWithProvince(String cityName, int provinceId) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cityName", cityName);
			map.put("provinceId", provinceId);
			return (Integer) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getCityIdByCityNameNewWithProvince", map);
		} catch (SQLException e) {
			logger.error("fail to execute sql getCityIdByCityName, cityName : " + cityName, e);
		}
		return null;
	}
	
	public static Integer getCityIdByCityName(String cityName) {
		try {
			return (Integer) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getCityIdByCityNameNew", cityName);
		} catch (SQLException e) {
			logger.error("fail to execute sql getCityIdByCityName, cityName : " + cityName, e);
		}
		return null;
	}
	
	/**
	 * 判断城市ID是否在数据库中存在（省市都可以）
	 * @param cityId
	 * @return
	 */
	public static boolean isValidCity(Integer cityId) {
		if (cityId == null) {
			return false;
		}
		if (cityMap == null || cityMap.size() == 0 || parentMap == null || parentMap.size() == 0) {
            init();
        }
		return cityMap.containsKey(Math.abs(cityId));
	}
	
    /**
     * 判断城市ID是否在数据库中存在（省市都可以）
     * @param cityId
     * @return
     */
    public static int getCityDefaultChannel(int cityId) {
        try {
            Integer result = (Integer) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getCityDefaultChannel", cityId);
            if (result != null) {
                return result;
            }
        } catch (SQLException e) {
            logger.error("fail to execute sql getCityDefaultChannel, cityId : " + cityId, e);
        }
        return DEFAULT_CHANNEL;
    }
}
