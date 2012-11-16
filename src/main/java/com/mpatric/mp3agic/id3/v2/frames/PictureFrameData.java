package com.mpatric.mp3agic.id3.v2.frames;


public class PictureFrameData extends FrameData {
    enum PictureType {
        OTHER,
        PNG_ICON,
        OTHER_ICON,
        COVER,
        BACK_COVER,
        LEAFLET_PAGE,
        MEDIA,
        LEAD_ARTIST,
        ARTIST,
        CONDUCTOR,
        BAND,
        COMPOSER,
        RECORDING_LOCATION,
        DURING_RECORDING,
        DURING_PERFORMANCE,
        SCREEN_CAPTURE,
        BRIGHT_FISH,
        ILLUSTRATION,
        ARTIST_LOGO,
        STUDIO_LOGO;
    };
        
        
	protected String mimeType;
	protected PictureType pictureType;
	protected String description;
	protected byte[] imageData;

	public PictureFrameData(String mimeType, PictureType pictureType, String description, byte[] imageData) {
		this.mimeType = mimeType;
		this.pictureType = pictureType;
		this.description = description;
		this.imageData = imageData;
	}

	public PictureFrameData() {
	    
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public PictureType getPictureType() {
		return pictureType;
	}

	public void setPictureType(PictureType pictureType) {
		this.pictureType = pictureType;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	
}
