package com.melot.kktv.model;

public class Catalog {
	
	private Integer catalogid;
	private String catalogname;
	private String catalogtag;
	private Integer catalogsort;
	
	public Integer getCatalogid() {
		return catalogid;
	}
	public void setCatalogid(Integer catalogid) {
		this.catalogid = catalogid;
	}
	public String getCatalogname() {
		return catalogname;
	}
	public void setCatalogname(String catalogname) {
		this.catalogname = catalogname;
	}
	public String getCatalogtag() {
		return catalogtag;
	}
	public void setCatalogtag(String catalogtag) {
		this.catalogtag = catalogtag;
	}
	public Integer getCatalogsort() {
		return catalogsort;
	}
	public void setCatalogsort(Integer catalogsort) {
		this.catalogsort = catalogsort;
	}

}
