package com.melot.kkcx.model;

import java.util.Map;

/**
 * Title: TimMsgList
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月14日 下午5:31:10
 */
public class TimMsgList {
    
    private Map<String, TimMsg> list;

    public Map<String, TimMsg> getList() {
        return list;
    }

    public void setList(Map<String, TimMsg> list) {
        this.list = list;
    }
}
