package com.mpatric.mp3agic.id3.v2.frames;



public class TextFrameData extends FrameData {
	protected String text;
	
    public TextFrameData() {
    }

    public TextFrameData(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
