package com.mpatric.mp3agic.id3.v2.frames;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class FrameData {
    
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
