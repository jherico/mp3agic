package com.mpatric.mp3agic.id3.v2.frames;

public class UrlFrameData extends FrameData {

	protected String description;
	protected String url;
	
	public UrlFrameData() {
	}
	
	public UrlFrameData(String description, String url) {
		this.description = description;
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
