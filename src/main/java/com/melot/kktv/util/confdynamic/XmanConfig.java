package com.melot.kktv.util.confdynamic;

import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.melot.module.config.Config;

public class XmanConfig {
	
	public static final String TABLENAME = "CONF_XMAN";
	
	@SuppressWarnings("unchecked")
	public static XmanConf getXmanInfo(int xmanId) {
		Map<String, Object> resultMap = Config.find(TABLENAME, xmanId);
		if (resultMap == null || resultMap.size() == 0) {
			return null;	
		} else {
			XmanConf xmanConf = new XmanConf();
			xmanConf.setXmanId(Integer.parseInt(String.valueOf(resultMap.get("XMAN_ID"))));
			xmanConf.setTag(Integer.parseInt(String.valueOf(resultMap.get("TAG"))));
			return xmanConf;
		}
	}
	
}

 class XmanConf {
	 
	private Integer xmanId;
	private Integer tag;
	
	public Integer getXmanId() {
		return xmanId;
	}
	
	public void setXmanId(Integer xmanId) {
		this.xmanId = xmanId;
	}
	
	public Integer getTag() {
		return tag;
	}
	
	public void setTag(Integer tag) {
		this.tag = tag;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
