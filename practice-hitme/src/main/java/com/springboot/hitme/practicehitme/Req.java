package com.springboot.hitme.practicehitme;

public class Req {
	
	private String url;
	private Boolean isParallel;
	private Integer count;
	public Req(String url, Boolean isParallel, Integer count) {
		super();
		this.url = url;
		this.isParallel = isParallel;
		this.count = count;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Boolean getIsParallel() {
		return isParallel;
	}
	public void setIsParallel(Boolean isParallel) {
		this.isParallel = isParallel;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "Req [url=" + url + ", isParallel=" + isParallel + ", count=" + count + "]";
	}
	
	
}
