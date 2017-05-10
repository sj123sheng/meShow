/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis.external;

import com.google.gson.JsonObject;

/**
 * Title: Address
 * <p>
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-21 下午12:00:46 
 */
public class RtmpAddress {

    
    public final static String FLV = ".flv";
    
    public final static String HTTP_DOMAIN = "http://pull-ti.kktv8.com";
    
    public final static String RTMP_DOMAIN = "rtmp://pull-ti.kktv8.com";
    
    public final static int DEFAULT_RTMP_PORT = 1935;
    
    private String ip;
    private int port;
    private String path;
    
    private RtmpAddress(){
        
    }
    
    /***
     *  通过rtmp解析出具体信息
     *  @param rtmpAddress: 1.1.1.1:6758/livekktv/channel
     *  parse then
     *  @see ip = 1.1.1.1; port = 6758 path = /livekktv/channel
     */
    public static RtmpAddress create(String rtmpAddress){
        RtmpAddress result = new RtmpAddress();
        if(rtmpAddress == null || rtmpAddress.isEmpty()){
            return result;
        }
        String []locations = rtmpAddress.split("/");  //切割成三段
        if(locations.length < 3){
            return result;
        }
        String[] urls  = locations[0].split(":");
        result.setIp(urls[0]);
        result.setPort(urls.length < 2 ? DEFAULT_RTMP_PORT : Integer.valueOf(urls[1]));
        StringBuilder path = new StringBuilder(128);
        for (int i = 1,size = locations.length; i < size; i++) {
            path.append("/").append(locations[i]);
        }
        result.setPath(path.toString());
        return result;
    }
    
    public String getClientAddress(){
        if(path != null){
            return HTTP_DOMAIN + this.path + FLV;
        }
        return null;
    }
    
    public JsonObject toJsonObject(){
        JsonObject result = new JsonObject();
        result.addProperty("ip", ip);
        result.addProperty("port", String.valueOf(port));
        return result;
    }
    

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
