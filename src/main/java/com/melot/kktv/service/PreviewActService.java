package com.melot.kktv.service;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.melot.kktv.model.PreviewAct;
import com.melot.kktv.util.CollectionEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class PreviewActService {
	
	private static Logger logger = Logger.getLogger(PreviewActService.class);
	
	/**
     * 添加节目预告
     * @param previewAct
     * @return
     */
    public static boolean addPreviewAct(PreviewAct previewAct) {
        if (previewAct == null) {
            return false;
        }
        try {
            int actId = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).insert("PreviewActConf.addPreviewAct", previewAct);
         // 保存mongo
            DBObject previewActObj = new BasicDBObject();
            
            previewActObj.put("actId", actId);
            previewActObj.put("actTitle", previewAct.getActTitle());
            previewActObj.put("actRoom", previewAct.getActRoom());
            previewActObj.put("isHall", previewAct.getIsHall());
            previewActObj.put("useable", previewAct.getUseable());
            previewActObj.put("belong", previewAct.getBelong());
            previewActObj.put("startTime", previewAct.getStime().getTime());
            previewActObj.put("endTime", previewAct.getEtime().getTime());
            CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.ACTCONFIG).insert(previewActObj);
            return true;
        } catch (SQLException e) {
           logger.error("PreviewActService.addPreviewAct excetpion, previewAct : " + new Gson().toJson(previewAct), e);
        }
        return false;
    }
   
   /**
    * 更新节目预告 
    * @param previewAct
    */
   public static boolean updatePreviewAct(PreviewAct previewAct) {
       if (previewAct == null) {
           return false;
       }
       try {
          int count = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).update("PreviewActConf.updatePreviewAct", previewAct);
          if (count == 0) {
              logger.warn("PreviewActService.updatePreviewAct excetpion, previewAct : " + previewAct.getActId());
          }
          DBObject updateObj = new BasicDBObject();
          updateObj.put("actId", previewAct.getActId());
          updateObj.put("useable", previewAct.getUseable());
          if (previewAct.getUseable() == 0) {
              // 删除mongoDB
              DBObject obj = new BasicDBObject();
              obj.put("actId", previewAct.getActId());
              obj.put("actRoom", previewAct.getActRoom());
              CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.ACTCONFIG).remove(obj);
          } else {
              CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.ACTCONFIG)
              .update(new BasicDBObject("actId", previewAct.getActId()), new BasicDBObject("$set", updateObj), false, false);
          }
          //CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.ACTCONFIG)
          //.update(new BasicDBObject("actId", previewAct.getActId()), new BasicDBObject("$set", updateObj), false, false);
          return true;
          
       } catch (SQLException e) {
          logger.error("PreviewActService.updatePreviewAct excetpion, previewAct : " + new Gson().toJson(previewAct), e);
       }
       return false;
   }
   
}
