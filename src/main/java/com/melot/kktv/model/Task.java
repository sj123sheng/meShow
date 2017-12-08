package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.mongodb.DBObject;

/**
 * 
 * 任务
 * 
 * @author RC
 *
 */
@SuppressWarnings("rawtypes")
public class Task implements Comparable {
	
	private Integer order;
	private Integer taskId;
	private Integer status;
	private String taskDesc;
	private Integer getMoney;
	private Integer versionCode;
	private Integer getGoldCoin;
	private String taskReward;
	private Date rewardTime;
	
	public void toTaskObject(DBObject taskObj) {
		Object t_order = taskObj.get("order");
		Object t_taskId = taskObj.get("taskId");
		Object t_status = taskObj.get("status");
		Object t_getMoney = taskObj.get("getMoney");
		Object t_versionCode = taskObj.get("versionCode");
		Object t_taskReward = taskObj.get("taskReward");
		Object t_taskDesc = taskObj.get("taskDesc");
		try {
			if (t_order instanceof Double) {
				this.order = Double.valueOf(t_order.toString()).intValue();
			} else {
				this.order = Integer.valueOf(t_order.toString());
			}
			if (t_taskId instanceof Double) {
				this.taskId = Double.valueOf(t_taskId.toString()).intValue();
			} else {
				this.taskId = Integer.valueOf(t_taskId.toString());
			}
			if (t_status instanceof Double) {
				this.status = Double.valueOf(t_status.toString()).intValue();
			} else {
				this.status = Integer.valueOf(t_status.toString());
			}
			if (t_getMoney instanceof Double) {
				this.getMoney = Double.valueOf(t_getMoney.toString()).intValue();
			} else {
				this.getMoney = Integer.valueOf(t_getMoney.toString());
			}
			if (t_versionCode instanceof Double) {
				this.versionCode = Double.valueOf(t_versionCode.toString()).intValue();
			} else {
				this.versionCode = Integer.valueOf(t_versionCode.toString());
			}
			this.taskReward = (String) t_taskReward;
			this.taskDesc = (String) t_taskDesc;
		} catch (Exception e) {
			this.taskId = null;
		}
	}
	
	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("taskId", this.getTaskId());
		jObject.addProperty("status", this.getStatus());
		jObject.addProperty("taskdesc", this.getTaskDesc());
		jObject.addProperty("taskReward", this.getTaskReward());
		jObject.addProperty("versionCode", this.getVersionCode());
		jObject.addProperty("getGoldCoin", this.getGetGoldCoin());
		jObject.addProperty("finishTimes", this.getStatus() > 0 ? 1 : 0);
		jObject.addProperty("times", 1);
		return jObject;
	}
	
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTaskDesc() {
		return taskDesc;
	}

	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}

	public Integer getGetMoney() {
		return getMoney;
	}

	public void setGetMoney(Integer getMoney) {
		this.getMoney = getMoney;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getTaskReward() {
		return taskReward;
	}

	public void setTaskReward(String taskReward) {
		this.taskReward = taskReward;
	}
	
	public Date getRewardTime() {
		return rewardTime;
	}

	public void setRewardTime(Date rewardTime) {
		this.rewardTime = rewardTime;
	}
	
    public Integer getGetGoldCoin() {
        return getGoldCoin;
    }

    public void setGetGoldCoin(Integer getGoldCoin) {
        this.getGoldCoin = getGoldCoin;
    }

    @Override
	public int compareTo(Object o) {
		return this.order - ((Task) o).getOrder();
	}

}
