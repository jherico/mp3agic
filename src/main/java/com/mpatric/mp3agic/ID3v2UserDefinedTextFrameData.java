package com.mpatric.mp3agic;

import com.mpatric.mp3agic.annotations.FrameMember;


public class ID3v2UserDefinedTextFrameData extends AbstractID3v2FrameData {
	
	@FrameMember(ordinal = 0)
	protected Encoding descEncoding;

	@FrameMember(ordinal = 1, encoded = true)
	protected String desc;

	@FrameMember(ordinal = 2, encoded = true)
	protected String data;

	public ID3v2UserDefinedTextFrameData(boolean unsynchronisation) {
		super(unsynchronisation);
	}
	
	public ID3v2UserDefinedTextFrameData(boolean unsynchronisation, byte[] bytes) throws InvalidDataException {
		super(unsynchronisation);
		synchroniseAndUnpackFrameData(bytes);
	}

    public Encoding getDescEncoding() {
        return descEncoding;
    }

    public void setDescEncoding(Encoding descEncoding) {
        this.descEncoding = descEncoding;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
	
    public String toString() {
        return this.data;
    }
    
}
