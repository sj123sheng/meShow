package com.melot.kkcx.model;

import java.util.Map;

/**
 * Title: ImAdminList
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月26日 下午3:59:45
 */
public class ImAdminList {

    private Map<Integer, ImAdmin> list;

    public Map<Integer, ImAdmin> getList() {
        return list;
    }

    public void setList(Map<Integer, ImAdmin> list) {
        this.list = list;
    }
    
}
