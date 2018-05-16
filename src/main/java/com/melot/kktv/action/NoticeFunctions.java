/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kktv.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.notice.driver.domain.NoticeCatalog;
import com.melot.notice.driver.domain.NoticeInfo;
import com.melot.notice.driver.domain.PageNoticeInfo;
import com.melot.notice.driver.domain.ResNoticeCatalog;
import com.melot.notice.driver.service.NoticeService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: NoticeFunctions
 * <p>
 * Description: 公告&帮助相关接口
 * </p>
 * 
 * @author 褚菲<a href="mailto:fei.chu@melot.cn">
 * @version V1.0
 * @since 2016年5月12日 上午10:57:57
 */
public class NoticeFunctions {
    
    private static Logger logger = Logger.getLogger(NoticeFunctions.class);
    
    /**
     * 获取大厅公告列表(56000001)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getHallNoticeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray jsonArray = new JsonArray();
        try {
            NoticeService noticeService = (NoticeService) MelotBeanFactory.getBean("noticeService");
            List<NoticeInfo> list = noticeService.getHallNoticeList(appId);
            
            if (list != null && list.size() > 0) {
                for (NoticeInfo temp : list) {
                    JsonObject jObject = new JsonObject();
                    jObject.addProperty("title", temp.getTitle());
                    jObject.addProperty("noticeUrl", temp.getNoticeUrl());
                    jObject.addProperty("noticeId", temp.getNoticeId());
                    jObject.addProperty("dtime", temp.getDtime().getTime());
                    ResNoticeCatalog resNoticeCatalog = noticeService.getNoticeCatalogAndParent(temp.getCataId());
                    if (resNoticeCatalog != null) {
                        jObject.addProperty("cataName", resNoticeCatalog.getParentCataName() != null ? resNoticeCatalog.getParentCataName() : resNoticeCatalog.getCataName());
                    }                    
                    jsonArray.add(jObject);
                }
            }
        } catch (Exception e) {
            logger.error("fail to execute NoticeService.getHallNoticeList, appId: " + appId, e);
        }
        
        result.add("noticeList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取栏目首页(56000002)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNoticeIndex(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int cataId, pageNum, pageCount;
        String appId;
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, TagCodeEnum.CATAID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamString(jsonObject, "a", null, TagCodeEnum.APPID_MISSING, 0, 32);
            pageNum = CommonUtil.getJsonParamInt(jsonObject, "pageNum", 1, null, 1, Integer.MAX_VALUE);
            pageCount = CommonUtil.getJsonParamInt(jsonObject, "pageCount", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            NoticeService noticeService = (NoticeService) MelotBeanFactory.getBean("noticeService");
            
            String cataList = noticeService.getNoticeCatalogTree();
            result.add("cataList", new JsonParser().parse(cataList).getAsJsonArray());
            
            // 根据cataId判断返回对象还是列表
            NoticeCatalog noticeCatalog = noticeService.getNoticeCatalogById(cataId);
            if (noticeCatalog != null && noticeCatalog.getNoticeId() != null) {
                // 返回公告内容
                NoticeInfo noticeInfo = noticeService.getNoticeInfoById(noticeCatalog.getNoticeId());
                if (noticeInfo != null) {
                    result.addProperty("title", noticeInfo.getTitle());
                    result.addProperty("content", noticeInfo.getContent());
                    result.addProperty("dtime", noticeInfo.getDtime().getTime());
                    result.addProperty("noticeUrl", noticeInfo.getNoticeUrl());
                    result.addProperty("lastOperator", noticeInfo.getLastOperator());
                }
            } else {
                // 返回公告列表
                PageNoticeInfo pageNoticeInfo = noticeService.getNoticeInfoByCataId(cataId, appId, null, pageNum, pageCount);
                JsonArray jsonArray = new JsonArray();
				if (pageNoticeInfo != null) {
					if (pageNoticeInfo.getList() != null && pageNoticeInfo.getList().size() > 0) {
						for (NoticeInfo temp : pageNoticeInfo.getList()) {
							JsonObject jObject = new JsonObject();
							jObject.addProperty("noticeId", temp.getNoticeId());
							jObject.addProperty("title", temp.getTitle());
							jObject.addProperty("dtime", temp.getDtime().getTime());
							jObject.addProperty("noticeUrl", temp.getNoticeUrl());
							jObject.addProperty("lastOperator", temp.getLastOperator());
							jsonArray.add(jObject);
						}
					}

					result.add("noticeList", jsonArray);
					result.addProperty("recordTotal", pageNoticeInfo.getRecordTotal());
				}
            }
            
            ResNoticeCatalog resNoticeCatalog = noticeService.getNoticeCatalogAndParent(cataId);
            if (resNoticeCatalog != null) {
                result.addProperty("cataName", resNoticeCatalog.getCataName());
                result.addProperty("parentCataName", resNoticeCatalog.getParentCataName());
            }
        } catch (Exception e) {
            logger.error("fail to execute getNoticeIndex, cataId: " + cataId + ", appId: " + appId + ", pageNum: " + pageNum + ", pageCount: " + pageCount, e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取公告列表(56000003)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNoticeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int cataId, pageNum, pageCount, locationType;
        String appId;
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, TagCodeEnum.CATAID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamString(jsonObject, "a", null, TagCodeEnum.APPID_MISSING, 0, 32);
            locationType = CommonUtil.getJsonParamInt(jsonObject, "locationType", 0, null, 1, Integer.MAX_VALUE);
            pageNum = CommonUtil.getJsonParamInt(jsonObject, "pageNum", 1, null, 1, Integer.MAX_VALUE);
            pageCount = CommonUtil.getJsonParamInt(jsonObject, "pageCount", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray jsonArray = new JsonArray();
        try {
            NoticeService noticeService = (NoticeService) MelotBeanFactory.getBean("noticeService");
            PageNoticeInfo pageNoticeInfo = noticeService.getNoticeInfoByCataId(cataId, appId, locationType == 0 ? null : locationType, pageNum, pageCount);
            ResNoticeCatalog resNoticeCatalog = noticeService.getNoticeCatalogAndParent(cataId);
            if (resNoticeCatalog != null) {
                result.addProperty("cataName", resNoticeCatalog.getCataName());
                result.addProperty("parentCataName", resNoticeCatalog.getParentCataName());
            }
			if (pageNoticeInfo != null) {
				if (pageNoticeInfo.getList() != null && pageNoticeInfo.getList().size() > 0) {
					for (NoticeInfo temp : pageNoticeInfo.getList()) {
						JsonObject jObject = new JsonObject();
						jObject.addProperty("noticeId", temp.getNoticeId());
						jObject.addProperty("title", temp.getTitle());
						jObject.addProperty("dtime", temp.getDtime().getTime());
						jObject.addProperty("noticeUrl", temp.getNoticeUrl());
						jObject.addProperty("lastOperator", temp.getLastOperator());
						jsonArray.add(jObject);
					}
				}
				result.addProperty("recordTotal", pageNoticeInfo.getRecordTotal());
			}
        } catch (Exception e) {
            logger.error("fail to execute getNoticeList, cataId: " + cataId + ", appId: " + appId + ", pageNum: " + pageNum + ", pageCount: " + pageCount, e);
        }
        
        result.add("noticeList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取公告详情(56000004)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNoticeDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int noticeId;
        try {
            noticeId = CommonUtil.getJsonParamInt(jsonObject, "noticeId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            NoticeService noticeService = (NoticeService) MelotBeanFactory.getBean("noticeService");
            NoticeInfo noticeInfo = noticeService.getNoticeInfoById(noticeId);
            
            if (noticeInfo != null) {
                ResNoticeCatalog resNoticeCatalog = null;
                if (noticeInfo.getCataId() != null) {
                    resNoticeCatalog = noticeService.getNoticeCatalogAndParent(noticeInfo.getCataId());
                }
                if (resNoticeCatalog != null) {
                    result.addProperty("cataId", resNoticeCatalog.getCataId());
                    result.addProperty("cataName", resNoticeCatalog.getCataName());
                    result.addProperty("parentCataId", resNoticeCatalog.getParentCataId());
                    result.addProperty("parentCataName", resNoticeCatalog.getParentCataName());
                }
                result.addProperty("title", noticeInfo.getTitle());
                result.addProperty("content", noticeInfo.getContent());
                result.addProperty("dtime", noticeInfo.getDtime().getTime());
                result.addProperty("noticeUrl", noticeInfo.getNoticeUrl());
                result.addProperty("lastOperator", noticeInfo.getLastOperator());
            }
        } catch (Exception e) {
            logger.error("fail to execute NoticeService.getNoticeInfoById, noticeId: " + noticeId, e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
}
