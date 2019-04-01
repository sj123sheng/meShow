/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkcx.functions;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.melot_utils.CollectionUtils;
import com.melot.kk.contract.api.constant.CertificateStatusEnum;
import com.melot.kk.contract.api.constant.ContractStatusEnum;
import com.melot.kk.contract.api.constant.SignStatusEnum;
import com.melot.kk.contract.api.constant.SignTypeEnum;
import com.melot.kk.contract.api.dto.ResSignCertificateDTO;
import com.melot.kk.contract.api.dto.SignContractDTO;
import com.melot.kk.contract.api.service.ContractService;
import com.melot.kktv.base.Page;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: ContractFunctions
 * <p>
 * Description: 上上签
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月14日 下午5:41:04
 */
public class ContractFunctions {
    
    private static Logger logger = Logger.getLogger(ContractFunctions.class);
    
    @Resource
    ContractService contractService;
    
    /**
     * 获取协议列表(51011501)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSignContractList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, pageIndex, countPerPage;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Page<SignContractDTO> resp = contractService.listSignContracts(null, null, userId, null, ContractStatusEnum.SIGNATURING, SignTypeEnum.ACTOR_SIGNER, pageIndex, countPerPage);
            if (resp != null && CollectionUtils.isNotEmpty(resp.getList())) {
                int count = resp.getCount();
                List<SignContractDTO> signContractDTOList = resp.getList();
                JsonArray jsonArray = new JsonArray();
                for (SignContractDTO signContractDTO : signContractDTOList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("contractName", signContractDTO.getContractTypeName());
                    jsonObj.addProperty("familyName", signContractDTO.getFamilyName());
                    jsonObj.addProperty("contractNo", signContractDTO.getContractNo());
                    int signStatus = signContractDTO.getSignStatus();
                    if (signContractDTO.getContractEndTime().before(new Date())) {
                        signStatus = -1;
                    }
                    jsonObj.addProperty("signStatus", signStatus);
                    if (signContractDTO.getCheckUrl() != null && signStatus == SignStatusEnum.UN_SIGN) {
                        jsonObj.addProperty("checkUrl", signContractDTO.getCheckUrl());
                    }
                    jsonArray.add(jsonObj);
                }
                result.add("contractList", jsonArray);
                result.addProperty("count", count);
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getSignCertificateInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 获取证书详情(51011502)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSignCertificateInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            int status = CertificateStatusEnum.UN_AUDITED;
            ResSignCertificateDTO resSignCertificateDTO = contractService.getResSignCertificateDTO(userId);
            if (resSignCertificateDTO != null) {
                status = resSignCertificateDTO.getStatus();
                if (status == CertificateStatusEnum.CERTIFIED) {
                    Date certEndTime = resSignCertificateDTO.getCertEndTime();
                    if (certEndTime.before(new Date())) {
                        //证书已过期
                        status = CertificateStatusEnum.EXPIRED;
                    } else {
                        result.addProperty("certId", resSignCertificateDTO.getCertId());
                        result.addProperty("certType", resSignCertificateDTO.getCertType());
                        result.addProperty("certStartTime", resSignCertificateDTO.getCertStartTime().getTime());
                        result.addProperty("certEndTime", resSignCertificateDTO.getCertEndTime().getTime());
                    }
                }
            } 

            result.addProperty("status", status);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getSignCertificateInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
    
    /**
     * 更新证书（51011503）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject renewalSignCertificate(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            ResSignCertificateDTO resSignCertificateDTO = contractService.getResSignCertificateDTO(userId);
            if (resSignCertificateDTO != null 
                    && resSignCertificateDTO.getStatus() != CertificateStatusEnum.UN_AUDITED) {
                if (resSignCertificateDTO.getStatus() == CertificateStatusEnum.GENERATING 
                        || resSignCertificateDTO.getCertEndTime().after(new Date())) {
                    result.addProperty("TagCode", "5101150302");
                } else {
                    contractService.confirmSignCertificate(userId);
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                }
            } else {
                result.addProperty("TagCode", "5101150301");
            }
        } catch (Exception e) {
            logger.error("Error renewalSignCertificate()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
    
}
