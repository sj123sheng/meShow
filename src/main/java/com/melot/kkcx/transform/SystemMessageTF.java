package com.melot.kkcx.transform;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.melot.kk.message.api.dto.SystemMessage;
import com.melot.kktv.model.KkSystemNotice;

public class SystemMessageTF {
    private SystemMessageTF() {}

    public static List<KkSystemNotice> getKkSystemNotices(List<SystemMessage> systemMessages) {
        if (CollectionUtils.isEmpty(systemMessages)) {
            return Lists.newArrayList();
        }
        List<KkSystemNotice> kkSystemNotices = Lists.newArrayList();
        for (SystemMessage systemMessage : systemMessages) {
            KkSystemNotice kkSystemNotice = getKkSystemNotice(systemMessage);
            if (kkSystemNotice != null) {
                kkSystemNotices.add(kkSystemNotice);
            }
        }
        return kkSystemNotices;
    }

    private static KkSystemNotice getKkSystemNotice(SystemMessage systemMessage) {
        if (systemMessage == null) {
            return null;
        }
        KkSystemNotice kkSystemNotice = new KkSystemNotice();
        kkSystemNotice.setId((long)systemMessage.getId());
        kkSystemNotice.setDescribe(systemMessage.getDescribe());
        kkSystemNotice.setMsgtime(systemMessage.getTime());
        kkSystemNotice.setTitle(systemMessage.getTitle());
        kkSystemNotice.setType(systemMessage.getType());
        kkSystemNotice.setUserId(systemMessage.getRefid2());
        return kkSystemNotice;
    }

}
