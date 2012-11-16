package com.mpatric.mp3agic.id3.v2.frames;

import com.mpatric.mp3agic.AbstractID3v2FrameData;
import com.mpatric.mp3agic.ID3v2CommentFrameData;
import com.mpatric.mp3agic.ID3v2PictureFrameData;
import com.mpatric.mp3agic.ID3v2TextFrameData;
import com.mpatric.mp3agic.ID3v2UrlFrameData;
import com.mpatric.mp3agic.ID3v2UserDefinedTextFrameData;

public enum Type {
    TEXT,
    URL,
    IMAGE,
    COMMENT,
    USER_TEXT,
    USER_URL;
}
