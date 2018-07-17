/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.melot.api.menu.sdk.dao.RecommendDao;
import com.melot.api.menu.sdk.dao.RoomSubCatalogDao;
import com.melot.api.menu.sdk.dao.SysMenuDao;
import com.melot.api.menu.sdk.dao.domain.HomePage;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.service.HomeService;
import com.melot.api.menu.sdk.service.PartService;
import com.melot.api.menu.sdk.service.RoomTopService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: HallPageService
 * <p>
 * Description: 
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-10-12 上午11:26:13
 */
public class HallPageService {
    
    public List<HomePage> getFistPagelist(int appId, int quDaoId, int platform){
        return getFistPagelist(appId, quDaoId, platform, true);
    }
    
    
    
    /**
     *  直播的web端, 前4个使用基于人气的排序 
     * 
     */
    @SuppressWarnings("unchecked")
    public List<HomePage> getFistPagelist(int appId, int quDaoId, int platform, boolean isFilter) {
        List<HomePage> result = new ArrayList<HomePage>();
        HashSet<Integer> roomSet = new HashSet<Integer>();
        HomeService homeService = MelotBeanFactory.getBean("homeService", HomeService.class);
        Map<String, PartService> partServiceMap = (Map<String, PartService>) MelotBeanFactory.getBean("partServiceMap");
        List<HomePage> list = homeService.getPartListInHome(appId, quDaoId, platform);
        int iterator = 0;
        for (HomePage pageHomeItem : list) {
            if (pageHomeItem.getSeatType() == 3) {
                pageHomeItem.setCdnState(1);
                result.add(pageHomeItem);
            } else {
                Integer titleId = pageHomeItem.getDetailId();
                PartService partService = partServiceMap.get(String.valueOf(pageHomeItem.getDataSourceType()));
                
                int liveTotal = partService.getPartLiveCount(titleId,null,null);
                pageHomeItem.setRoomTotal(partService.getPartRoomCount(titleId,null,null)); //设置房间总人数
                pageHomeItem.setLiveTotal(liveTotal); //设置房间在播总房间数
                List<RoomInfo> rooms = null;
                if(platform == 1 && iterator < 4){ //直播web版的前4个采用人气的方式,不采取随机
                    rooms = getPartRoomList(pageHomeItem,roomSet,list.size(), liveTotal,false);
                }else{
                    rooms = getPartRoomList(pageHomeItem,roomSet,list.size(), liveTotal,true);
                }
                if(isFilter){
                    rooms = filterUnLiveRoom(rooms,liveTotal,platform);
                }
                if(rooms != null && rooms.size() > 0){//非空房间需要添置到返回结果中
                    addSetRooms(rooms,roomSet);
                    pageHomeItem.setRooms(rooms);
                    result.add(pageHomeItem);
                }
            }
            iterator++;
        }
        roomSet = null;
        return result;
    }
    
    private List<RoomInfo> getPartRoomList(HomePage pageHomeItem,HashSet<Integer> roomSet,Integer totalPartSize, Integer liveTotal, boolean isShuffle){
        List<RoomInfo> result = new ArrayList<RoomInfo>();
        Integer titleId = pageHomeItem.getDetailId();
        Integer num = pageHomeItem.getHomeShowRooms();
        
        int startIndex = 0;//分页起始位置,默认为0;
        int demandCount = num == null ? 4: num ; //需要获取的记录数,默认为4
        int remainCount = demandCount;
        if (titleId == null || titleId <= 0) {
            return new ArrayList<RoomInfo>();
        }
        List<RoomInfo> topRoomList = getTopRoomListByTitleId(titleId);//查询置顶在播房间
        remainCount -= topRoomList.size(); //除去置顶房间数剩余房间数
        List<RoomInfo> remainingRoomList = null;//非置顶房间
        
        int numToQuery = demandCount + liveTotal +totalPartSize * 2; 
        remainingRoomList = getRemainningRoomList(titleId, pageHomeItem.getDataSourceType(), startIndex, numToQuery); //取出栏目正在播的列表
        int livePoint = getLivePoint(remainingRoomList);
        List<RoomInfo> tempList = remainingRoomList.subList(0, livePoint);
        
        if(isShuffle){
            Collections.shuffle(tempList);
        }
        
        for(RoomInfo roomInfo : remainingRoomList){
           Integer actorId = roomInfo.getActorId();
           if(roomSet.contains(actorId) || isOnTopRoom(actorId) || topRoomList.contains(roomInfo)){ //本栏目下非置顶房间在其他栏目可能置顶
               continue;
           }
           result.add(roomInfo);
           if(result.size() >= remainCount){
               break;
           }
        }
        //加入置顶房间
        for (RoomInfo roomInfo : topRoomList) {
            int index = roomInfo.getPartPosition()-1;
            if(index > result.size()){
                result.add(roomInfo);
            }else{
                result.add(index, roomInfo);
            }
        }
        return result;
    }
    
    
    /**
     *  获取正在播出的直播数 
     * 
     */
    private int getLivePoint(List<RoomInfo> rooms){
        int i = 0; 
        if (CollectionUtils.isEmpty(rooms)) {
            return i;
        }
        for (int size = rooms.size(); i < size; i++) {
            RoomInfo roomInfo = rooms.get(i);
            if(roomInfo.getLiveEndtime() != null){
                break;
            }
        }
        return i;
    }
    
    private boolean isOnTopRoom(Integer actorId) {
        RoomTopService roomTopService = MelotBeanFactory.getBean("roomTopService", RoomTopService.class);
        return roomTopService.getRoomTopByroomId(actorId);
    }
    
    private List<RoomInfo> getRemainningRoomList(Integer titleId,Integer sourceType ,Integer start, Integer count) {
        RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
        if(sourceType.intValue() == 1){ //兴趣推荐
            RecommendDao recommendDao = MelotBeanFactory.getBean("recommendDao", RecommendDao.class);
            List<RoomInfo> recommendList = recommendDao.getRecommendList(start, count);
            return recommendList;
        }
        if(sourceType.intValue() == 2){ //周边达人
            return  new ArrayList<RoomInfo>();
        }
        if(sourceType.intValue() == 3){ //汇聚栏目
            return  roomSubCatalogDao.getEntireSubRoomList(titleId, start, count);
        }
        
        return roomSubCatalogDao.getPartRoomList(titleId, start, count); //default
    }
    
    private List<RoomInfo> getTopRoomListByTitleId(Integer titleId) {
        RoomTopService roomTopService = MelotBeanFactory.getBean("roomTopService", RoomTopService.class);
        SysMenuDao sysMenuDao = MelotBeanFactory.getBean("sysMenuDao", SysMenuDao.class);
        SysMenu sysMenu = sysMenuDao.getSysMenuById(titleId);
        return roomTopService.getOnLiveTopRoomsBytitleId(titleId,sysMenu.getTopRoomCount());
    }
    
    private void addSetRooms(List<RoomInfo> rooms,HashSet<Integer>roomSet){
        for (RoomInfo roomInfo : rooms) {
            roomSet.add(roomInfo.getActorId());
        }
    }
    
    
    public List<RoomInfo> filterUnLiveRoom(List<RoomInfo> rooms,int liveTotal, int platform){
        if(liveTotal == 0 ||rooms == null ||rooms.size() == 0 ){
            return null;
        }
        int liveCount = 0;
        Iterator<RoomInfo>it = rooms.iterator();
        while(it.hasNext()){ //队列本身已经根据开播状态做好了排序 
            if(it.next().isOnLive()){  
                liveCount++;
            }else{
                break; //break while; 
            }
        }  
        
        if(liveCount == 0){ //栏目下房间都是未开播状态
            return null;
        }
        
        switch(platform){
            case 1:liveCount = downToQuadro(liveCount);break;
            default:liveCount = downToEven(liveCount);break;
        }
        try {
            //若栏目下配置的总数小于向上取整,会造成数组越界,采取返回一半的方式
            return rooms.subList(0, liveCount > rooms.size() ? liveCount >> 1 :liveCount);
        } catch (Exception e) {
            return rooms;
        }
    }
    
    /**
     * 向下取4整数倍 
     * e.g: 1-->0; 2-->0; 7-->4;8-->8
     */
    private int downToQuadro(int input){
        return input - input%4;// input%4 == 0 ? input: (1 + (int)Math. ceil(input/4) ) * 4;
    }
 
    /**
     * 向下取偶数
     * e.g: 0-->0; 1-->0; 2-->2; 3-->2; 
     */
    private int downToEven(int input){
        return input - input%2;
    }

    
    
    
}