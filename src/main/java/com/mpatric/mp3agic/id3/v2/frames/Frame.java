package com.mpatric.mp3agic.id3.v2.frames;

public enum Frame {
    
    // ID frames
    TIT1(Type.TEXT, "Content Group"),
    TIT2(Type.TEXT, "Title"),
    TIT3(Type.TEXT, "Sub-title"),
    TALB(Type.TEXT, "Album"),
    TOAL(Type.TEXT, "Original Album"),
    TRCK(Type.TEXT, "Track Number"),
    TPOS(Type.TEXT, "Set Number"),
    TSST(Type.TEXT, "Set Subtitle"),
    TSRC(Type.TEXT, "ISRC"),
    
    // People frames
    TPE1(Type.TEXT, "Artist"),
    TPE2(Type.TEXT, "Accompaniment"),
    TPE3(Type.TEXT, "Conductor"),
    TPE4(Type.TEXT, "Remixed"),
    TOPE(Type.TEXT, "Original artist/performer"),
    TEXT(Type.TEXT, "Lyricist/Text writer"),
    TOLY(Type.TEXT, "Original lyricist/text writer"),
    TCOM(Type.TEXT, "Composer"),
    TMCL(Type.TEXT, "Musician credits list"),
    TIPL(Type.TEXT, "Involved people list"),
    TENC(Type.TEXT, "Encoder"),

    // Derived / subjective frames
    TBPM(Type.TEXT, "Beats per minute"),
    TLEN(Type.TEXT, "Length (ms)"),
    TKEY(Type.TEXT, "Initial key (scale)"),
    TLAN(Type.TEXT, "Language"),
    TCON(Type.TEXT, "Content Type"),
    TFLT(Type.TEXT, "File Type"),
    TMED(Type.TEXT, "Media Type"),
    TMOO(Type.TEXT, "Mood"),
    
    // Rights frames
    TCOP(Type.TEXT, "Copyright"),
    TPRO(Type.TEXT, "Produced"),
    TPUB(Type.TEXT, "Publisher"),
    TOWN(Type.TEXT, "Owner"),
    TRSN(Type.TEXT, "Internet radio station name"),
    TRSO(Type.TEXT, "Internet radio station owner"),

    // Other
    TOFN(Type.TEXT, "Original filename"),
    TDLY(Type.TEXT, "Playlist delay"),
    TDEN(Type.TEXT, "Encoding time"),
    TDOR(Type.TEXT, "Original release time"),
    TDRC(Type.TEXT, "Recording time"),
    TDRL(Type.TEXT, "Release time"),
    TDTG(Type.TEXT, "Tagging time"),
    TSSE(Type.TEXT, "Encoding settings"),
    TSOA(Type.TEXT, "Album sort order"),
    TSOP(Type.TEXT, "Performer sort order"),
    TSOT(Type.TEXT, "Title sort order"),

    // User defined text frames
    TXXX(Type.USER_TEXT, "User defined text"),

    // URL link frames
    WCOM(Type.URL, "Commercial information"),
    WCOP(Type.URL, "Copyright/Legal information"),
    WOAF(Type.URL, "Official audio file webpage"),
    WOAR(Type.URL, "'Official artist/performer webpage"),
    WOAS(Type.URL, "Official audio source webpage"),
    WORS(Type.URL, "Official Internet radio station homepage"),
    WPAY(Type.URL, "Payment"),
    WPUB(Type.URL, "Publishers official webpage"),
    WXXX(Type.USER_URL, "User defined URL"),

    // Other types
    APIC(Type.IMAGE, "Attached Picture"),
    COMM(Type.COMMENT, "COMMENT");
    
//    private final static Map<String, Frame> byId;
//    
//    static {
//        Map<String, Frame> map = Maps.newHashMap();
//        for (Frame ft: Frame.values()) {
//            map.put(ft.toString(), ft);
//            for (String alias : ft.aliases) {
//                map.put(alias, ft);
//            }
//        }
//        byId = ImmutableMap.copyOf(map);
//        
//    }

    final String description;
    final Type contentType;
    final String[] aliases;
    
    
    Frame(Type contentType, String description, String...aliases) {
        this.description = description;
        this.contentType = contentType;
        this.aliases = (null != aliases) ? aliases : new String[] {};
    }
    
}
