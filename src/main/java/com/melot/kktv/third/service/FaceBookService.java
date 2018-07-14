package com.melot.kktv.third.service;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: FaceBookService
 * <p>
 * Description: FaceBookService
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年8月28日 下午2:44:35
 */
public class FaceBookService extends BaseService{
    
    private static Logger logger = Logger.getLogger(FaceBookService.class);
    
    private final static String CLIENT_ID = "475289669332579";;
    
    private final static String CLIENT_SECRET = "657ad42924b9397ed80d9e6a5205913c";
    
    private String serverUrl;
    
    public String verifyUser(String uuid, String sessionId) {
        String result = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "FaceBookService.verifyUser");
        try {
            OAuth20Service service = new ServiceBuilder(CLIENT_ID).apiSecret(CLIENT_SECRET).build(FacebookApi.instance());
            OAuth2AccessToken accessToken = new OAuth2AccessToken(sessionId);
            OAuthRequest request = new OAuthRequest(Verb.GET, serverUrl);
            service.signRequest(accessToken, request);
            Response response = service.execute(request);
            t.setStatus(Transaction.SUCCESS);
            if (response.getCode() == 200 && !StringUtil.strIsNull(response.getBody())) {
                JsonObject jsonObj = new JsonParser().parse(response.getBody()).getAsJsonObject();
                JsonArray jsonArray = (JsonArray) jsonObj.get("data");
                if (jsonArray.size() > 0) {
                    for (int i=0; i < jsonArray.size(); i++) {
                        JsonObject jObj = (JsonObject) jsonArray.get(i);
                        if (jObj.get("id").getAsString().equals(uuid)) {
                            return TagCodeEnum.SUCCESS;
                        };
                    }
                }
            }
        } catch (Exception e) {
            logger.error("facebook服务端验证用户请求异常", e);
            t.setStatus(e);
        } finally {
            t.complete();
        }
        return result;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}

