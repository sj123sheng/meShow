package com.melot.kktv.third.service;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: TwitterService
 * <p>
 * Description: TwitterService
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年8月28日 下午2:44:35
 */
public class TwitterService extends BaseService{
    
    private static Logger logger = Logger.getLogger(TwitterService.class);
    
    private final static String clientId = "pTMw4BzFNY0efstRPlPXrVlFa";;
    
    private final static String clientSecret = "MSLUrw2CVvvpzMasafBKkXsSl2nVQGd6eICytTuPrwntKe3bIS";
    
    private String serverUrl;
    
    public String verifyUser(String uuid, String sessionId) {
        String result = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "TwitterService.verifyUser");
        try {
            final OAuth10aService service = new ServiceBuilder(clientId).apiSecret(clientSecret).build(TwitterApi.instance());
            String[] params = sessionId.split(",");
            if (params.length > 1) {
                String token = params[0];
                String tokenSecret = params[1];
                final OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);
                final OAuthRequest request = new OAuthRequest(Verb.GET, serverUrl);
                service.signRequest(accessToken, request);
                final Response response = service.execute(request);
                t.setStatus(Transaction.SUCCESS);
                if (response.getCode() == 200 && !StringUtil.strIsNull(response.getBody())) {
                    JsonObject jsonObj = new JsonParser().parse(response.getBody()).getAsJsonObject();
                    if (jsonObj.get("id_str").getAsString().equals(uuid)) {
                        return TagCodeEnum.SUCCESS;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("twitter服务端验证用户请求异常", e);
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

