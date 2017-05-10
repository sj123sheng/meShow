package com.melot.kktv.service;

import java.util.List;
import java.util.regex.Pattern;

import com.melot.module.ModuleService;
import com.melot.news.model.NewsInfo;
import com.melot.resource.driver.domain.Resource;

public class ResourceService {
	
	public static int addResource(Resource resource) {
		com.melot.resource.driver.ResourceService resourceService = (com.melot.resource.driver.ResourceService) ModuleService.getService("ResourceService");
		if (resourceService != null) {
			return resourceService.saveResource(resource);
		}
		return 0;
	}
	
	public static String addResources(List<Resource> resourceList) {
		com.melot.resource.driver.ResourceService resourceService = (com.melot.resource.driver.ResourceService) ModuleService.getService("ResourceService");
		if (resourceService != null) {
			return resourceService.batchAddResource(resourceList);
		}
		return null;
	}
	
	public static Resource getResource(int resId, int type) {
		com.melot.resource.driver.ResourceService resourceService = (com.melot.resource.driver.ResourceService) ModuleService.getService("ResourceService");
		if (resourceService != null) {
			return resourceService.getResource(resId, type);
		}
		return null;
	}
	
	public static boolean delResource(NewsInfo newsInfo) {
		com.melot.resource.driver.ResourceService resourceService = (com.melot.resource.driver.ResourceService) ModuleService.getService("ResourceService");
		if (resourceService == null) {
			return false;
		}
		if (newsInfo.getRefVideo() != null) {
			resourceService.checkResource(Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefVideo()).replaceAll("")), 3, 0, "用户删除", newsInfo.getUserId());
		} else if (newsInfo.getRefImage() != null) {
			String[] resIds = Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll("").split(",");
			for (String resId : resIds) {
				resourceService.checkResource(Integer.valueOf(resId), 3, 0, "用户删除", newsInfo.getUserId());
			}
		}
		return true;
	}
	
	public static List<Resource> getResourceList(String imageUrls) {
		com.melot.resource.driver.ResourceService resourceService = (com.melot.resource.driver.ResourceService) ModuleService.getService("ResourceService");
		if (resourceService == null) {
			return null;
		}
		return resourceService.getImages(imageUrls);
	}
}
