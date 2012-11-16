package com.mpatric.mp3agic.id3.v2.frames;

import com.google.common.base.Objects;

public class CommentFrameData extends FrameData {
	private String language;
	private String description;
	private String comment;

	public CommentFrameData(String language, String description, String comment) {
		this.language = language;
		this.description = description;
		this.comment = comment;
	}

	public CommentFrameData() {
    }

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean equals(Object obj) {
	    if(obj instanceof CommentFrameData){
	        final CommentFrameData other = (CommentFrameData) obj;
	        return Objects.equal(language, other.language)
	            && Objects.equal(description, other.description)
	            && Objects.equal(comment, other.comment);
	    } 
	    
	    return false;
	}

	public String toString() {
	    return this.language + "/" + this.description + "/" + this.comment;
	}
}
