package com.mpatric.mp3agic.id3.v2.frames;

public class UserDefinedTextFrameData extends FrameData {
	protected String description;
	protected String data;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
	
}
