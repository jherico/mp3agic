package com.mpatric.mp3agic.id3.v2.frames;




public class UnknownFrameData extends FrameData {
	
	protected byte [] data;

	public UnknownFrameData() {
	}
	
	public UnknownFrameData(byte[] bytes) {
	    this.data = bytes;
	}
	
   public String toString() {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
    }
}
