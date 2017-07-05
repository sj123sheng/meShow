package com.melot.kktv.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapSession;
import com.melot.kkcx.service.GeneralService;
import com.melot.kktv.domain.TagInfo;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.redis.HotDataSource;

public class TagService {
	
	private static Logger logger = Logger.getLogger(TagService.class);
	
	/**
	 * 贴标签
	 * @param userId 贴标签用户
	 * @param userRole 贴标签用户角色
	 * @param ownerId 被贴标签用户
	 * @param tagName 标签名称
	 * @return true - 成功, false - 失败
	 */
	public static boolean tagging(int userId, int userRole, int ownerId, int tagId, String tagName) {
		SqlMapSession session = SqlMapClientHelper.getInstance(DB.MASTER).openSession();
		try {
			session.startTransaction();
			// 插入贴标签记录(oracle)
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("ownerId", ownerId);
			map.put("userId", userId);
			map.put("userRole", userRole);
			map.put("time", new Date());
			session.insert("Tag.insertTagHist", map);
			// 添加标签到用户标签信息中(redis)
			String tagArray = HotDataSource.getHotFieldValue(String.valueOf(ownerId), "tags");
			if (tagArray != null && !tagArray.isEmpty()) {
				tagArray = tagArray + "," + tagName;
			} else {
				tagArray = tagName;
			}
			HotDataSource.setHotFieldValue(String.valueOf(ownerId), "tags", tagArray);
			session.commitTransaction();
			return true;
		} catch (Exception e) {
			logger.error("Fail to execute tagging sql", e);
		} finally {
			try {
				session.endTransaction();
			} catch (SQLException e) {
				logger.error("Fail to end transaction", e);
			} finally {
				session.close();
			}
		}
		return false;
	}
	
	/**
	 * 读取标签
	 * @param tagId 标签ID
	 * @param tagName 标签名称
	 * @return 成功返回标签信息，否则 null
	 */
	public static TagInfo getTagInfo(int tagId, String tagName) {
		TagInfo tagInfo = null;
		if (tagId > 0) {
			// 根据tagId获取tagInfo
			try{
				tagInfo = (TagInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Tag.getTagInfoById", tagId);
			}catch(Exception e){
				 logger.error("Fail to execute getTagInfo sql", e);
			}
		} else {
			// 根据tagName获取tagInfo
			if (tagName != null && !tagName.isEmpty()) {
				try{
					tagInfo = (TagInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Tag.getTagInfoByName", tagName);
				}catch(Exception e){
					 logger.error("Fail to execute getTagInfo sql", e);
				}
			}
		}
		return tagInfo;
	}
	
	/**
	 * 读取用户标签
	 * @param userId 用户ID
	 * @return 标签字符串拼接起来的值
	 */
	public static String getUserTags(int userId) {
		String tagString = null;
    	try {
    		tagString =  (String) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Tag.getUserTags", userId);
    	} catch(Exception e) {
    		logger.error("Fail to execute getUserTag sql", e);
    	}
		return tagString;
	}
	
	/**
	 * 验证用户是否已贴该标签
	 * @param userId
	 * @param tagName
	 * @return true/false
	 */
	public static boolean checkUserTag(int userId, int tagId) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("tagId", tagId);
			Integer result = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Tag.checkUserTag", map);
			if (result != null && result.intValue() > 0) return true;
		} catch (SQLException e) {
			 logger.error("Fail to execute checkUserTag sql", e);
		}
		return false;
	}
	
	/**
	 * 创建标签
	 * @param tagCreater 创建标签的操作员ID
	 * @param tagSource 标签来源：1 - 用户, 2 - 官方, 3 - 快播
	 * @param tagName 标签名字
	 * @return 成功返回标签Id 失败返回0
	 */
	public static int createTag(int tagCreater, int tagSource, String tagName) {
		int tagId = 0;
		if (tagName != null && !tagName.isEmpty()) {
			//tagName标签名称非法过滤
			if (!GeneralService.hasSensitiveWords(tagCreater, tagName)) {
				// 验证标签是否存在
				TagInfo existTag = getTagInfo(0, tagName);
				if (existTag != null && existTag.getTagId() != null
						&& existTag.getTagId().intValue() > 0) {
					tagId = existTag.getTagId().intValue();
				} else {
					//创建标签
					try {
						TagInfo tagInfo = new TagInfo();
						tagInfo.setTagCreater(tagCreater);
						tagInfo.setTagSource(tagSource);
						tagInfo.setTagName(tagName);
						if (tagSource == 2) {
							tagInfo.setTagStatus(1);// 官方标签默认审核通过
						} else {
							tagInfo.setTagStatus(0);
						}
						Integer ret = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).insert("Tag.createTag", tagInfo);
						if (ret != null && ret.intValue() > 0)
							tagId = ret.intValue();
					} catch (Exception e) {
						logger.error("Fail to execute createTag sql", e);
					}
				}
			}
		}
		return tagId;
	}
	
	/**
	 * 审核标签
	 * @param tagId
	 * @param status(1.审核通过2.审核驳回)
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static boolean checkTags(int checker, int status, int... tagIds){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("checker", checker);
		map.put("checkTime", new Date());
		map.put("status", status);
		map.put("tagIds", tagIds);
		int count = 0;
		SqlMapSession session = SqlMapClientHelper.getInstance(DB.MASTER).openSession();
		try{
			session.startTransaction();
			count = session.update("Tag.updateCheckStatus",map);
			//如status为2，则删除被贴该标签用户的数据(redis/mongodb)
			if(status == 2){
				for (int tagId : tagIds) {
					//根据tagName读取标签信息
					TagInfo tagInfo = TagService.getTagInfo(tagId, null);
					if (tagInfo != null) {
						List<Integer> userList= session.queryForList("Tag.getTaggingUser", tagId);
						if (userList != null && userList.size() > 0) {
							deleteTagUser(tagInfo.getTagId(), tagInfo.getTagName(), userList.toArray(new Integer[0]));
						}
					}
				}
			}
			session.commitTransaction();
		}catch(Exception e){
			 logger.error("Fail to execute updateCheckStatus or getTaggingUser sql", e);
		}finally{
			try {
				session.endTransaction();
			} catch (SQLException e) {
				logger.error("Fail to end transaction", e);
			} finally {
				session.close();
			}
		}
		if(count==0){
			return false;
		}
		return true;
		
	}
	
	/**
	 * 删除标签用户
	 * @param tagId 标签ID
	 * @param tagName 标签名称
	 * @param userIds 用户ID
	 * @return
	 */
	public static boolean deleteTagUser(int tagId, String tagName, Integer... userIds) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("userIds", userIds);
			int count = SqlMapClientHelper.getInstance(DB.MASTER).delete("Tag.deleteTagUser", map);
			if (count < 1) {
				return true;
			}
			for (int userId : userIds) {
				String tagArray = HotDataSource.getHotFieldValue(String.valueOf(userId), "tags");
				if (tagArray != null && !tagArray.isEmpty()) {
					String[] tags = tagArray.split(",");
					StringBuffer buffer = new StringBuffer();
					for (String tag : tags) {
						if (!tag.equals(tagName))
							buffer.append(tag).append(",");
					}
					String newTagArray = buffer.toString();
					if (!newTagArray.isEmpty() && newTagArray.endsWith(","))
						newTagArray = newTagArray.substring(0, newTagArray.length()-1);
					HotDataSource.setHotFieldValue(String.valueOf(userId), "tags", newTagArray);
				}
			}
		} catch (Exception e) {
			logger.error("fail to delete", e);
			return false;
		}

		return true;
	}
	
	/**
	 * 根据标签查找用户
	 * @param tagName
	 * @return 用户列表
	 */
	@SuppressWarnings("unchecked")
	public static List<Integer> selectUserByTag(int tagId) {
		try {
			 return SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Tag.selectUserByTag", tagId);
		} catch(Exception e){
			 logger.error("Fail to execute selectUserByTag sql", e);
		}
		return null;
	}
	
}
