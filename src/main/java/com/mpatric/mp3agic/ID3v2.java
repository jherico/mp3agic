package com.mpatric.mp3agic;

import java.util.List;
import java.util.Map;

public interface ID3v2 extends ID3v1 {
	String getComposer();
	void setComposer(String composer);
	
	String getPublisher();
	void setPublisher(String publisher);
	
	String getOriginalArtist();
	void setOriginalArtist(String originalArtist);
	
	String getAlbumArtist();
	void setAlbumArtist(String albumArtist);
	
	String getCopyright();
	void setCopyright(String copyright);
	
	String getUrl();
	void setUrl(String url);
	
	String getEncoder();
	void setEncoder(String encoder);
	
	byte[] getAlbumImage();
	void setAlbumImage(byte[] albumImage, String mimeType);
	String getAlbumImageMimeType();
	
	Map<String, ID3v2FrameSet> getFrameSets();
	Map<String, List<String>> getFrameData();
	void clearFrameSet(String id);
}
