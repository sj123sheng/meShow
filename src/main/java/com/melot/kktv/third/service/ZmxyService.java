/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kktv.third.service;

import com.antgroup.zmxy.openplatform.api.DefaultZhimaClient;
import com.antgroup.zmxy.openplatform.api.ZhimaApiException;
import com.antgroup.zmxy.openplatform.api.request.ZhimaCustomerCertificationCertifyRequest;
import com.antgroup.zmxy.openplatform.api.request.ZhimaCustomerCertificationInitializeRequest;
import com.antgroup.zmxy.openplatform.api.request.ZhimaCustomerCertificationQueryRequest;
import com.antgroup.zmxy.openplatform.api.response.ZhimaCustomerCertificationInitializeResponse;
import com.antgroup.zmxy.openplatform.api.response.ZhimaCustomerCertificationQueryResponse;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.BizCodeEnum;
import com.melot.kktv.util.DateUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;

/**
 * Title: 芝麻认证
 * @author: shengjian
 * @date: 2017/9/6
 * @copyright: Copyright (c)2017
 * @company: melot
 * <p>
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2017/9/6         shengjian     1.0
 */
public class ZmxyService extends BaseService {

    private static Logger logger = Logger.getLogger(ZmxyService.class);

    // 芝麻信用网关地址
    public static final String serverUrl = "https://zmopenapi.zmxy.com.cn/openapi.do";

    // 芝麻认证产品码
    private static final String PRODUCT_CODE = "w1010100000000002978";

    // 芝麻认证商户ID
    public static final String MERCHANT_ID = "268821000000367101014";

    // 芝麻认证前缀
    private static final String PREFIX = "KKZMRZ";

    public static final String APP_ID = "1004605";
    
    private static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAL9TH/38znm+HjXVMstB+Tw8mVcXTfwt3NRSjwzwuXviMqC3L63uk3ILsTEaHKl/3o/BS6NrE1ASVYwjUDxxsD+8eplvoZ6eHHBkGLArA1UJCdH1icJ+jovEcEjWm6B9MFzuoDU15e+R3qMha6Ml8Fi/pbcK+ZQVtsmSJcrLhzWtAgMBAAECgYEAgNJYhSyWXdSMBNUzaTZXuZ5X5RMU+EptGS0pkp33Vhmc2yfc27D66FxFB3m8eMhXM9YDttne3BgsL7qLSNeHwodUY8KRjHorHSsgma0HRKBHA3+s84mSlUgVLxaCM8frRnbYUkUkTls2ycXBDkhWIQxj5okjUrJ9Gx+slBDwyAUCQQDxyCqJeKxxiSHkoOSuN6/kRUkJERj4iMlrL7iT8wyRMD3vshhcVojmgZhpoaavXIBNfTAkw2bfB/z5jPS4sj5LAkEAypNdsTbYkXb7aIlFGut2USsPJEpHSmIOGZWj/iu9pqHi2Iga+Sm9VYNQY9lZIhDLay9uW+UD9e8iMpMeFXMA5wJAFsjmOU5F8e0aZJFE+6YDzV86IsCZudIqKQ+2NTsSVBxXud7urMT64b4Uvt+c9amh7at+ffhsGViHXn0KEt25QQJAR1GknbyCeIwcLQyO8NBPInp3ZT5lZPGqpyVAzD5YEa9S7wrT/D7Osql9hvnBYLXq9/yF7QJu54neT1HuOHz5pwJBAKQGYkO/uUZOiS1zVGBmG8czrxHxWdZWJp6b80o1uJ/Gwho67I4k7FcCOs/Ds0doGK1LPsDphT39h3Q+pfzRDp4=";
    
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQEjlFUI0wt2lY9BTo46GXOiR8ls7FpNVVNCXZSHp+WKApsPJzBJ3L+ZN9u7aC3Vv00FeXtcWEYIXE7mkL2nKLtr3E5D7LZY6MTDrVMSGCl+xyp93kkh8bKiO7cI6yk0Qi/5Ob44IYWrlDQV0FVqvAZBWYeZYDeoHNIQgiigEnfQIDAQAB";

    private static DefaultZhimaClient getZhimaClient() {
        return new DefaultZhimaClient(serverUrl, APP_ID, PRIVATE_KEY, PUBLIC_KEY);
    }
    
    /** 
     * 生产一个业务流水凭证（transaction_id）
     * @return 
     */
    public static String generateTransactionId(int userId) {

        String transactionId = PREFIX + userId + "_" + DateUtil.formatDateTime(new Date(), "yyyyMMddHHmmss");
        return transactionId;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        //String bizNo = getBizNo(7023202, 1, "盛健", "362330198805228999").getBizNo();
        //getUrl(bizNo,"http://www.taobao.com");
        getResult("ZM201709073000000454500729761813");
    }
    
    /**
     * 获取本次认证的标识
     * @return
     */
    public static ZhimaCustomerCertificationInitializeResponse getBizNo(int userId, int bizCode, String certName, String certNo) {
        ZhimaCustomerCertificationInitializeResponse result = null;
        
        ZhimaCustomerCertificationInitializeRequest request = new ZhimaCustomerCertificationInitializeRequest();
        String transactionId = generateTransactionId(userId);
        request.setTransactionId(transactionId);// 必要参数
        request.setProductCode(PRODUCT_CODE);// 必要参数
        request.setBizCode(BizCodeEnum.parseId(bizCode).getValue());// 必要参数
        request.setIdentityParam(getIdentityParam(certName, certNo));// 必要参数
        request.setExtBizParam("{}");// 必要参数
        try {
            ZhimaCustomerCertificationInitializeResponse response = getZhimaClient().execute(request);
            logger.error("zmrz bizNo: " + response.getBizNo());
            System.out.println("zmrz bizNo: " + response.getBizNo());
            result = response;
            result.setBody(transactionId);
        } catch (ZhimaApiException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    private static String getIdentityParam(String certName, String certNo) {
        Map<String, String> identityParamMap = Maps.newTreeMap();
        identityParamMap.put("identity_type", "CERT_INFO");
        identityParamMap.put("cert_type", "IDENTITY_CARD");
        identityParamMap.put("cert_name", certName);
        identityParamMap.put("cert_no", certNo);
        String identityParam = new Gson().toJson(identityParamMap);
        return identityParam;
    }

    /**
     * 支付宝端内认证 需要生成认证请求 URL
     * @param bizNo
     * @return
     */
    public static String getUrl(String bizNo, String returnUrl) {
        String result = null;
        
        try {
            ZhimaCustomerCertificationCertifyRequest request = new ZhimaCustomerCertificationCertifyRequest();
            request.setBizNo(bizNo);// 必要参数
            // 设置回调地址,必填. 如果需要直接在支付宝APP里面打开回调地址使用alipay协议
            // alipay://www.taobao.com 或者 alipays://www.taobao.com,分别对应http和https请求
            request.setReturnUrl(returnUrl);// 必要参数
            String url = getZhimaClient().generatePageRedirectInvokeUrl(request);
            System.out.println("zmrz return_url: " + url);
            logger.error("zmrz return_url: " + url);
            result = url;
        } catch (ZhimaApiException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 
     * @param bizNo
     * @return
     */
    public static ZhimaCustomerCertificationQueryResponse getResult(String bizNo) {
        ZhimaCustomerCertificationQueryRequest req = new ZhimaCustomerCertificationQueryRequest();
        ZhimaCustomerCertificationQueryResponse response = null;
        req.setBizNo(bizNo);// 必要参数
        try {
            response = getZhimaClient().execute(req);
            System.out.println(new Gson().toJson(response));
            logger.error("zmrz result: " + new Gson().toJson(response));
        } catch (ZhimaApiException e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public String verifyUser(String openId, String sessionId) {
        return null;
    }
}
