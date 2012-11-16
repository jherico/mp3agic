package com.mpatric.mp3agic;




public class ID3v2UnknownFrameData extends AbstractID3v2FrameData {
	
	protected String hexData;

	public ID3v2UnknownFrameData(boolean unsynchronisation) {
		super(unsynchronisation);
	}
	
	public ID3v2UnknownFrameData(boolean unsynchronisation, byte[] bytes) throws InvalidDataException {
		super(unsynchronisation);
		hexData = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
	}
	
   public String toString() {
        return this.hexData;
    }

}
