package com.melot.kktv.action;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import redis.clients.jedis.Jedis;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.redis.RedisConfigHelper;

/**
 * KK游戏v1.1版本榜单临时接口
 * KK游戏接口从2002+0061开始
 * @author RC
 *
 */
public class GameRankAction {
	 private static final String ACTOR_TOTAL_RANKING = "actor_total_ranking";
	 private static final String SOURCE_NAME = "GameRankingList";
	 
	/**
	 * 获取游戏达人总榜接口(临时接口20020061)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getRankList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		int platform;
		int count;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 0, Integer.MAX_VALUE);
			count = CommonUtil.getJsonParamInt(jsonObject, "count", Constant.return_rank_count, null, 1, Integer.MAX_VALUE);
		}  catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		Set<String>rankingSet = getRankingList();
		final String pathPrefix = ConfigHelper.getHttpdir();
		
    	JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = new JsonArray();
        JsonObject json = null;
        if(rankingSet!= null && rankingSet.size() > 0){
            for (String string : rankingSet) {
                JsonElement element = jsonParser.parse(string);
                json = element.getAsJsonObject();
                String portraitPath = json.get("portraitPath").getAsString();
                portraitPath = portraitPath.startsWith(pathPrefix) ? portraitPath.substring(pathPrefix.length()) : "/portrait/default/user_head.jpg";
                if(platform == PlatformEnum.WEB) {
                    json.addProperty("portrait_path_48", portraitPath + "!48");
                } else{
                    json.addProperty("portrait_path_128", portraitPath + "!128");
                }
                json.remove("portraitPath");
                jsonArray.add(json);
                if(jsonArray.size() == count){
                    break;
                }
            }
        }
        result.add("rankList", jsonArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", pathPrefix);
		
		return result;
		
	}
	
    private static Set<String> getRankingList() {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.zrange(ACTOR_TOTAL_RANKING , 0, -1);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
	
    private static Jedis getInstance() {
        return RedisConfigHelper.getJedis(SOURCE_NAME);
    }

    private static void freeInstance(Jedis jedis) {
        RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
    }
	
}
