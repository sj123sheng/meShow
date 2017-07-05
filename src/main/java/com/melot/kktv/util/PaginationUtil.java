package com.melot.kktv.util;

import java.util.List;

import com.melot.kktv.domain.PreviewAct;

/**
 * 该类主要用来进行分页功能
 * <p></p>
 * @author fenggaopan 2015年10月23日 上午10:01:33
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2015年10月23日
 * @modify by reason:{方法名}:{原因}
 */
public class PaginationUtil {
	
	/**
	 * 分页方法,用来对保存到缓存中的数据进行分页查询
	 * @author fenggaopan 2015年10月23日 上午10:02:20
	 * @param start 起始页
	 * @param offset 每页大小
	 * @param acts 需要分页的list集合
	 * @return 返回分页后的集合
	 */
	public static List<PreviewAct> pagination(Integer start,Integer offset,List<PreviewAct> acts) {
	    //页数和offset
	    List<PreviewAct> getPageList = acts.subList(start>acts.size()?acts.size():start,start+offset>acts.size()?acts.size():start+offset);
	    return getPageList ;
	}
	
}
