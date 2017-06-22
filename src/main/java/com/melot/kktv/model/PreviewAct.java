package com.melot.kktv.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.melot.kkactivity.driver.domain.ActInfo;

/*
 * actid		节目编号
 * acttitle		节目标题
 * actbanner	节目banner
 * acturl		节目URL
 * actroom		节目房间
 * starttime	开始时间
 * endtime		结束时间
 * actStatus	节目状态(已开始 1 未开始 0)
 * isPrior		是否优先显示
 * isSub		是否订阅 (读取redis)
 */
public class PreviewAct {

	private String actBanner;
	private Integer actId;
	private String actRoom;
	private String actTitle;
	private String actUrl;
	private Date stime;
	private Long startTime;
	private Long endTime;
	private Date etime;
	private Integer actStatus;//
	private Integer isPrior;//
	private boolean isSub = false;
	private Integer dayOfWeek;
	private Integer SDTime;//固定节目开始时间
	private Integer EDTime;//固定节目结束时间
	
	private Integer roomMode;
	
	private Integer roomSource;
	
	/**
	 * 显示场景（0:默认状态，1：唱响家族房显示）
	 */
	private Integer belong;
	
	/**
	 * 是否大厅显示
	 */
	private Integer isHall;
	
	/**
	 * 是否可用 （0： 终止，1：启用）
	 */
	private Integer useable;
	
	private Integer appId;
	
	private Integer aid;
	
	/**
	 * 节目具体描述
	 */
	private String actDesc;
	
    /**
     * 转成JsonObject
     *
     * @return
     */
    public static JsonObject toJsonObject(ActInfo actInfo, int ishall, int platform, long nowtime, long startTime, long endTime) {
        long stime = 0L;
        long etime = 0L;
        long nowWeek;
        long diff = 0;
        int dayWeek = 0;
        if (actInfo == null) {
            return null;
        }
        if (nowtime == 0l){
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(nowtime));
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayWeek = 7;
            dayWeek = 7;
        } else {
            dayWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        JsonObject jObject = new JsonObject();
        if (actInfo.getActStartTime() != null) {
            stime = actInfo.getActStartTime().getTime();
        } else if (actInfo.getSDTime() != null && actInfo.getDayWeek() != null) {
            nowWeek = actInfo.getDayWeek();
            if (nowWeek >= dayWeek) {
                diff = nowWeek - dayWeek;
            } else {
                diff = 7 + nowWeek - dayWeek;
            }
            stime = nowtime + diff*24*3600*1000 + actInfo.getSDTime();
            if (stime < startTime || stime > endTime) {
                return null;
            }
        }
        if (actInfo.getActEndTime() != null) {
            etime = actInfo.getActEndTime().getTime();
        } else if(actInfo.getEDTime() != null) {
            nowWeek = actInfo.getDayWeek();
            if (nowWeek >= dayWeek) {
                diff = nowWeek-dayWeek;
            } else {
                diff = 7+nowWeek-dayWeek;
            }
            etime = nowtime + diff*24*3600*1000 + actInfo.getEDTime();
        }
        if (actInfo.getActRoom() == null || actInfo.getActRoom().trim().isEmpty()) {
            return null;
        } else {
            jObject.addProperty("actRoom", actInfo.getActRoom().trim());
        }
        if (actInfo.getActId() <= 0) {
            return null;
        } else {
            jObject.addProperty("actId", actInfo.getActId());
        }
        if (actInfo.getActTitle() == null || actInfo.getActTitle().trim().isEmpty()) {
            return null;
        } else {
            jObject.addProperty("actTitle", actInfo.getActTitle());
        }
        if (actInfo.getActUrl() != null && !actInfo.getActUrl().trim().isEmpty()) {
            jObject.addProperty("actUrl", actInfo.getActUrl());
        }
        if (actInfo.getActDesc() != null) {
            jObject.addProperty("actDesc", actInfo.getActDesc());
        }
        jObject.addProperty("startTime", stime);
        if (System.currentTimeMillis() >= stime && System.currentTimeMillis() <=etime) { // 直播中
            jObject.addProperty("actStatus", 1);
        } else { // 未开始
            jObject.addProperty("actStatus", 0);
        }
        if (ishall != 1) { // 预告
            if (actInfo.getActBanner() != null) {
                Map map = (Map) JSON.parse(actInfo.getActBanner()); 
                switch (platform) {
                  case 1:
                      jObject.addProperty("actBanner", (String) map.get("w"));
                    break;
                  case 2:
                      jObject.addProperty("actBanner", (String) map.get("a"));
                    break;
                  case 3:
                      jObject.addProperty("actBanner", (String) map.get("i"));
                    break;
                  case 4:
                      jObject.addProperty("actBanner", (String) map.get("i"));
                    break;
                  default: break;
                }       
            }
            jObject.addProperty("endTime", etime);
            
        }
        return jObject;
    }
	
	public Integer getActId() {
		return actId;
	}

	public void setActId(Integer actId) {
		this.actId = actId;
	}

	public String getActTitle() {
		return actTitle;
	}

	public void setActTitle(String actTitle) {
		this.actTitle = actTitle;
	}

	public String getActBanner() {
		return actBanner;
	}

	public void setActBanner(String actBanner) {
		this.actBanner = actBanner;
	}

	public String getActUrl() {
		return actUrl;
	}

	public void setActUrl(String actUrl) {
		this.actUrl = actUrl;
	}
	
	public String getActRoom() {
		return actRoom;
	}

	public void setActRoom(String actRoom) {
		this.actRoom = actRoom;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public Integer getIsPrior() {
		return isPrior;
	}

	public void setIsPrior(Integer isPrior) {
		this.isPrior = isPrior;
	}

	public boolean isSub() {
		return isSub;
	}

	public void setSub(boolean isSub) {
		this.isSub = isSub;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Integer getSDTime() {
		return SDTime;
	}

	public void setSDTime(Integer sDTime) {
		SDTime = sDTime;
	}

	public Integer getEDTime() {
		return EDTime;
	}

	public void setEDTime(Integer eDTime) {
		EDTime = eDTime;
	}

	public Integer getActStatus() {
		return actStatus;
	}

	public void setActStatus(Integer actStatus) {
		this.actStatus = actStatus;
	}

	public Integer getRoomMode() {
		return roomMode;
	}

	public void setRoomMode(Integer roomMode) {
		this.roomMode = roomMode;
	}

    public Integer getRoomSource() {
        return roomSource;
    }

    public void setRoomSource(Integer roomSource) {
        this.roomSource = roomSource;
    }

    public Integer getBelong() {
        return belong;
    }

    public void setBelong(Integer belong) {
        this.belong = belong;
    }

    public Integer getIsHall() {
        return isHall;
    }

    public void setIsHall(Integer isHall) {
        this.isHall = isHall;
    }

    public Integer getUseable() {
        return useable;
    }

    public void setUseable(Integer useable) {
        this.useable = useable;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }

    public Date getStime() {
        return stime;
    }

    public Date getEtime() {
        return etime;
    }

    public void setStime(Date stime) {
        this.stime = stime;
    }

    public void setEtime(Date etime) {
        this.etime = etime;
    }

	public String getActDesc() {
		return actDesc;
	}

	public void setActDesc(String actDesc) {
		this.actDesc = actDesc;
	}

}
