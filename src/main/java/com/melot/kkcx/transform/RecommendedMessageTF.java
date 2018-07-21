package com.melot.kkcx.transform;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.Lists;
import com.melot.kk.message.api.dto.RecommendedMsg;
import com.melot.kktv.model.RecommendedMessage;

public class RecommendedMessageTF {
    private RecommendedMessageTF() {}

    public static List<RecommendedMessage> getRecommendedMessages(List<RecommendedMsg> recommendedMsgs) {
        List<RecommendedMessage> recommendedMessages = Lists.newArrayList(); 
        if (CollectionUtils.isEmpty(recommendedMsgs)) {
            return recommendedMessages;
        }
        for (RecommendedMsg recommendedMsg : recommendedMsgs) {
            RecommendedMessage recommendedMessage = getRecommendedMessage(recommendedMsg);
            if (recommendedMessage != null) {
                recommendedMessages.add(recommendedMessage);
            }
        }
        return recommendedMessages;
    }

    private static RecommendedMessage getRecommendedMessage(RecommendedMsg recommendedMsg) {
        if (recommendedMsg == null) {
            return null;
        }
        RecommendedMessage recommendedMessage = new RecommendedMessage();
        BeanUtils.copyProperties(recommendedMsg, recommendedMessage);
        recommendedMessage.setImgUrl(recommendedMsg.getImgurl());
        recommendedMessage.setMsgId(recommendedMsg.getMsgid());
        recommendedMessage.setImgUrlIOS(recommendedMsg.getImgurlios());
        recommendedMessage.setStartTime(recommendedMsg.getStarttime());
        return recommendedMessage;
    }
    
    
}
