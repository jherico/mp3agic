package com.mpatric.mp3agic.id3.v2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.mpatric.mp3agic.AbstractID3v2FrameData;
import com.mpatric.mp3agic.BufferTools;
import com.mpatric.mp3agic.Encoding;
import com.mpatric.mp3agic.ID3v1Genres;
import com.mpatric.mp3agic.ID3v2CommentFrameData;
import com.mpatric.mp3agic.ID3v2Frame;
import com.mpatric.mp3agic.ID3v2FrameSet;
import com.mpatric.mp3agic.ID3v2ObseleteFrame;
import com.mpatric.mp3agic.ID3v2ObseletePictureFrameData;
import com.mpatric.mp3agic.ID3v2PictureFrameData;
import com.mpatric.mp3agic.ID3v2TextFrameData;
import com.mpatric.mp3agic.ID3v2UrlFrameData;
import com.mpatric.mp3agic.ID3v2UserDefinedTextFrameData;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NoSuchTagException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.id3.v2.frames.Frame;
import com.mpatric.mp3agic.id3.v2.frames.FrameData;


public abstract class Tag {
	public static final byte[] HEADER_TAG = new byte[] { 'I', 'D', '3' };
    public static final byte[] FOOTER_TAG = new byte[] { '3', 'D', 'I' };
	public static final int HEADER_LENGTH = 10;
	public static final int FOOTER_LENGTH = 10;
	public static final int MAJOR_VERSION_OFFSET = 3;
	public static final int MINOR_VERSION_OFFSET = 4;
	public static final int FLAGS_OFFSET = 5;
	public static final int DATA_LENGTH_OFFSET = 6;
	
	public static final byte FLAG_FOOTER = 0x10;
	public static final byte FLAG_EXPERIMENTAL = 0x20;
    public static final byte FLAG_EXTENDED_HEADER = 0x40;
    public static final byte FLAG_COMPRESSION = 0x40;
    public static final byte FLAG_UNSYNCHRONISATION = (byte) 0x80;
    public static final int PADDING_LENGTH = 256;
    public static final String ITUNES_COMMENT_DESCRIPTION = "iTunNORM";
	
	protected boolean padding = false;
	protected String version;
	private int dataLength = 0;
	private int extendedHeaderLength;
	private byte[] extendedHeaderData;
    private byte flags;
	private boolean obseleteFormat = false;
	
	private final Map<Frame, FrameData> frameSets = new HashMap<Frame, FrameData>();

	public Tag() {
	}

	public Tag(byte[] bytes) {
		unpackTag(bytes);
	}

	public static void sanityCheckTag(byte[] bytes) {
        if (bytes.length < HEADER_LENGTH) {
            throw new IllegalArgumentException("Buffer too short");
        }
        if (!Arrays.equals(HEADER_TAG, Arrays.copyOfRange(bytes, 0, HEADER_TAG.length))) {
            throw new IllegalArgumentException("Can't find ID3 tag marker in buffer");
        }
        if ((0 != (FOOTER_TAG & bytes[FLAGS_OFFSET])) && 
                !Arrays.equals(HEADER_TAG, Arrays.copyOfRange(bytes, 0, HEADER_TAG.length))) {
            
        }
    }

	private void unpackTag(byte[] bytes) {
		sanityCheckTag(bytes);
        int majorVersion = bytes[MAJOR_VERSION_OFFSET];
        int minorVersion = bytes[MINOR_VERSION_OFFSET];
        if (majorVersion != 2 && majorVersion != 3 && majorVersion != 4) {
            throw new IllegalStateException("Unsupported version 2." + majorVersion + "." + minorVersion);
        }
        this.flags = bytes[FLAGS_OFFSET]; 
        this.version = majorVersion + "." + minorVersion;
        dataLength = BufferTools.unpackSynchsafeInteger(bytes[DATA_LENGTH_OFFSET], bytes[DATA_LENGTH_OFFSET + 1], bytes[DATA_LENGTH_OFFSET + 2], bytes[DATA_LENGTH_OFFSET + 3]);
        if (dataLength < 1) {
            throw new IllegalStateException("Zero size tag");
        }
        int offset = HEADER_LENGTH;
		if (0 != (flags & FLAG_EXTENDED_HEADER)) {
			offset = unpackExtendedHeader(bytes, offset);
		}

		int frameLength = dataLength;
		if (0 != (flags & FLAG_FOOTER)) {
		      if (! FOOTER_TAG.equals(BufferTools.byteBufferToStringIgnoringEncodingIssues(bytes, offset, FOOTER_TAG.length()))) {
		            throw new InvalidDataException("Invalid footer");
		        }

            offset = unpackFrames(bytes, offset, dataLength - FOOTER_LENGTH);
            offset = unpackFooter(bytes, dataLength);
		} else {
		    unpackFrames(bytes, offset, dataLength);
		}
	}

	private int unpackExtendedHeader(byte[] bytes, int offset) {
		extendedHeaderLength = BufferTools.unpackSynchsafeInteger(bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]) + 4;
		extendedHeaderData = BufferTools.copyBuffer(bytes, offset + 4, extendedHeaderLength);
		return extendedHeaderLength;
	}

	protected int unpackFrames(byte[] bytes, int offset, int framesLength) {
		int currentOffset = offset;
		while (currentOffset <= framesLength) {
			ID3v2Frame frame;
			try {
				frame = createFrame(bytes, currentOffset);
				addFrame(frame, false);
				currentOffset += frame.getLength();
			} catch (InvalidDataException e) {
				break;
			}
		}
		return currentOffset;
	}

	private void addFrame(ID3v2Frame frame, boolean replace) {
		ID3v2FrameSet frameSet = frameSets.get(frame.getId());
		if (frameSet == null) {
			frameSet = new ID3v2FrameSet(frame.getId());
			frameSet.addFrame(frame);
			frameSets.put(frame.getId(), frameSet);
		} else if (replace) {
			frameSet.clear();
			frameSet.addFrame(frame);
		} else {
			frameSet.addFrame(frame);
		}
	}
	
	protected ID3v2Frame createFrame(byte[] bytes, int currentOffset) throws InvalidDataException {
		if (obseleteFormat) return new ID3v2ObseleteFrame(bytes, currentOffset);
		return new ID3v2Frame(bytes, currentOffset);
	}
	
	protected ID3v2Frame createFrame(String id, byte[] data) {
		if (obseleteFormat) return new ID3v2ObseleteFrame(id, data);
		else return new ID3v2Frame(id, data);
	}
	

	public byte[] toBytes() throws NotSupportedException {
		byte[] bytes = new byte[getLength()];
		packTag(bytes);
		return bytes;
	}

	public void packTag(byte[] bytes) throws NotSupportedException {
		int offset = packHeader(bytes, 0);
		if (extendedHeader) {
			offset = packExtendedHeader(bytes, offset);
		}
		offset = packFrames(bytes, offset);
		if (footer) {
			offset = packFooter(bytes, dataLength);
		}
	}
	
	private int packHeader(byte[] bytes, int offset) {
		BufferTools.stringIntoByteBuffer(TAG, 0, TAG.length(), bytes, offset);
		String s[] = version.split("\\.");
		if (s.length > 0) {
			byte majorVersion = Byte.parseByte(s[0]);
			bytes[offset + MAJOR_VERSION_OFFSET] = majorVersion;
		}
		if (s.length > 1) {
			byte minorVersion = Byte.parseByte(s[1]);
			bytes[offset + MINOR_VERSION_OFFSET] = minorVersion;
		}
		packFlags(bytes, offset);
		BufferTools.packSynchsafeInteger(getDataLength(), bytes, offset + DATA_LENGTH_OFFSET);
		return offset + HEADER_LENGTH;
	}

	protected abstract void packFlags(byte[] bytes, int i);
	
	private int packExtendedHeader(byte[] bytes, int offset) {
		BufferTools.packSynchsafeInteger(extendedHeaderLength, bytes, offset);
		BufferTools.copyIntoByteBuffer(extendedHeaderData, 0, extendedHeaderData.length, bytes, offset + 4);
		return offset + 4 + extendedHeaderData.length;
	}

	public int packFrames(byte[] bytes, int offset) throws NotSupportedException {
		int newOffset = packSpecifiedFrames(bytes, offset, null, "APIC");
		newOffset = packSpecifiedFrames(bytes, newOffset, "APIC", null);
		return newOffset;
	}
	
	private int packSpecifiedFrames(byte[] bytes, int offset, String onlyId, String notId) throws NotSupportedException {
		Iterator<ID3v2FrameSet> setIterator = frameSets.values().iterator();		
		while (setIterator.hasNext()) {
			ID3v2FrameSet frameSet = setIterator.next();
			if ((onlyId == null || onlyId.equals(frameSet.getId())) && (notId == null || !notId.equals(frameSet.getId()))) { 			
				Iterator<ID3v2Frame> frameIterator = frameSet.getFrames().iterator();
				while (frameIterator.hasNext()) {
					ID3v2Frame frame = (ID3v2Frame) frameIterator.next();
					if (frame.getDataLength() > 0) {
						byte[] frameData = frame.toBytes();
						BufferTools.copyIntoByteBuffer(frameData, 0, frameData.length, bytes, offset);
						offset += frameData.length;
					}
				}
			}
		}	
		return offset;
	}
	
	private int packFooter(byte[] bytes, int offset) {
		BufferTools.stringIntoByteBuffer(FOOTER_TAG, 0, FOOTER_TAG.length(), bytes, offset);
		String s[] = version.split(".");
		if (s.length > 0) {
			byte majorVersion = Byte.parseByte(s[0]);
			bytes[offset + MAJOR_VERSION_OFFSET] = majorVersion;
		}
		if (s.length > 1) {
			byte minorVersion = Byte.parseByte(s[0]);
			bytes[offset + MINOR_VERSION_OFFSET] = minorVersion;
		}
		packFlags(bytes, offset);
		BufferTools.packSynchsafeInteger(getDataLength(), bytes, offset + DATA_LENGTH_OFFSET);
		return offset + FOOTER_LENGTH;
	}

	private int calculateDataLength() {
		int length = 0;
		if (extendedHeader) length += extendedHeaderLength;  
		if (footer) length += FOOTER_LENGTH;
		else if (padding) length += PADDING_LENGTH;
		Iterator<ID3v2FrameSet> setIterator = frameSets.values().iterator();
		while (setIterator.hasNext()) {
			ID3v2FrameSet frameSet = setIterator.next();
			Iterator<ID3v2Frame> frameIterator = frameSet.getFrames().iterator();
			while (frameIterator.hasNext()) {
				ID3v2Frame frame = (ID3v2Frame) frameIterator.next();
				length += frame.getLength(); 
			}
		}
		return length;
	}
	
	public boolean useFrameUnsynchronisation() {
		return false;
	}

	public String getVersion() {
		return version;
	}
		
	private void invalidateDataLength() {
		dataLength = 0;
	}

	public int getDataLength() {
		if (dataLength == 0) {
			dataLength = calculateDataLength();
		}
		return dataLength;
	}
	
	public int getLength() {
		return getDataLength() + HEADER_LENGTH;
	}
	
	public Map<String, ID3v2FrameSet> getFrameSets() {
		return frameSets;
	}
	
	public boolean getPadding() {
		return padding;
	}

	public void setPadding(boolean padding) {
		if (this.padding != padding) {
			invalidateDataLength();
			this.padding = padding;
		}
	}
	
	public boolean hasFooter() {
		return footer;
	}

	public void setFooter(boolean footer) {
		if (this.footer != footer) {
			invalidateDataLength();
			this.footer = footer;
		}
	}

	public boolean hasUnsynchronisation() {
		return unsynchronisation;
	}

	public void setUnsynchronisation(boolean unsynchronisation) {
		if (this.unsynchronisation != unsynchronisation) {
			invalidateDataLength();
			this.unsynchronisation = unsynchronisation;
		}
	}
	
	public boolean getObseleteFormat() {
		return obseleteFormat;
	}

	private String getTextFrameText(String frameId) {
		ID3v2TextFrameData frameData = extractTextFrameData(frameId);
		if (frameData != null && frameData.getText() != null) return frameData.getText().toString();
		return null;
	}
	
	private void setTextFrameText(String frameId, String text) {
		if (text != null && text.length() > 0) {
			invalidateDataLength();
			ID3v2TextFrameData frameData = new ID3v2TextFrameData(useFrameUnsynchronisation(), text);
			addFrame(createFrame(frameId, frameData.toBytes()), true);
		}
	}

	public String getTrack() {
		return getTextFrameText(obseleteFormat ? ID_TRACK_OBSELETE : ID_TRACK);
	}

	public void setTrack(String track) {
		setTextFrameText(ID_TRACK, track);
	}

	public String getArtist() {
		return getTextFrameText(obseleteFormat ? ID_ARTIST_OBSELETE : ID_ARTIST);
	}

	public void setArtist(String artist) {
		setTextFrameText(ID_ARTIST, artist);
	}
	
	public String getAlbumArtist() {
		return getTextFrameText(obseleteFormat ? ID_ALBUM_ARTIST_OBSELETE : ID_ALBUM_ARTIST);
	}

	public void setAlbumArtist(String albumArtist) {
		setTextFrameText(ID_ALBUM_ARTIST, albumArtist);
	}

	public String getTitle() {
		return getTextFrameText(obseleteFormat ? ID_TITLE_OBSELETE : ID_TITLE);
	}

	public void setTitle(String title) {
		setTextFrameText(ID_TITLE, title);
	}

	public String getAlbum() {
		return getTextFrameText(obseleteFormat ? ID_ALBUM_OBSELETE : ID_ALBUM);
	}

	public void setAlbum(String album) {
		setTextFrameText(ID_ALBUM, album);
	}
	
	public String getYear() {
		return getTextFrameText(obseleteFormat ? ID_YEAR_OBSELETE : ID_YEAR);
	}

	public void setYear(String year) {
		setTextFrameText(ID_YEAR, year);
	}

	protected String getGenreText() {
		return getTextFrameText(obseleteFormat ? ID_GENRE_OBSELETE : ID_GENRE);
	}

	protected void setGenreText(String genreText) {
		setTextFrameText(ID_GENRE, genreText);
	}
	
	public int getGenre() {
		String text = getGenreText();
		if (text == null || text.length() == 0) return -1;

		Matcher m = GENRE_REGEX.matcher(text);
		if (!m.matches()) {
			return -1;
		}

		return Integer.parseInt(m.group(1));
	}

	public void setGenre(int genre) {
		if (genre >= 0) {
			String genreDescription = (ID3v1Genres.GENRES.length > genre) ? 
					ID3v1Genres.GENRES[genre] : ""; 
			setGenreText(String.format("(%d)%s", genre, genreDescription));
		}
	}
	
	public String getGenreDescription() {
		String genreText = getGenreText();

		Matcher m = GENRE_REGEX.matcher(genreText);
		if (!m.matches()) {
			return null;
		}

		int genreNum = Integer.parseInt(m.group(1));
		if (genreNum >= 0) {
			try {
				return ID3v1Genres.GENRES[genreNum];
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		
		return m.group(2);
	}
	
	public String getComment() {
		ID3v2CommentFrameData frameData;
		if (obseleteFormat) frameData = extractCommentFrameData(ID_COMMENT_OBSELETE, false);
		else frameData = extractCommentFrameData(ID_COMMENT, false);
		if (frameData != null && frameData.getComment() != null) return frameData.getComment().toString();
		return null;
	}

	public void setComment(String comment) {
		if (comment != null && comment.length() > 0) {
			invalidateDataLength();
			ID3v2CommentFrameData frameData = new ID3v2CommentFrameData(useFrameUnsynchronisation(), "eng", null, comment);
			addFrame(createFrame(ID_COMMENT, frameData.toBytes()), true);
		}
	}
	
	public String getItunesComment() {
		ID3v2CommentFrameData frameData;
		if (obseleteFormat) frameData = extractCommentFrameData(ID_COMMENT_OBSELETE, true);
		else frameData = extractCommentFrameData(ID_COMMENT, true);
		if (frameData != null && frameData.getComment() != null) return frameData.getComment().toString();
		return null;
	}
	
	public void setItunesComment(String itunesComment) {
		if (itunesComment != null && itunesComment.length() > 0) {
			invalidateDataLength();
			ID3v2CommentFrameData frameData = new ID3v2CommentFrameData(useFrameUnsynchronisation(), ITUNES_COMMENT_DESCRIPTION, null, itunesComment);
			addFrame(createFrame(ID_COMMENT, frameData.toBytes()), true);
		}
	}


	public String getComposer() {
		return getTextFrameText(obseleteFormat ? ID_COMPOSER_OBSELETE : ID_COMPOSER);
	}

	public void setComposer(String composer) {
		setTextFrameText(ID_COMPOSER, composer);
	}
	
	public String getPublisher() {
		return getTextFrameText(obseleteFormat ? ID_PUBLISHER_OBSELETE : ID_PUBLISHER);
	}

	public void setPublisher(String publisher) {
		setTextFrameText(ID_PUBLISHER, publisher);
	}
	
	public String getOriginalArtist() {
		return getTextFrameText(obseleteFormat ? ID_ORIGINAL_ARTIST_OBSELETE : ID_ORIGINAL_ARTIST);
	}

	public void setOriginalArtist(String originalArtist) {
		setTextFrameText(ID_ORIGINAL_ARTIST, originalArtist);
	}

	public String getCopyright() {
		return getTextFrameText(obseleteFormat ? ID_COPYRIGHT_OBSELETE : ID_COPYRIGHT);
	}

	public void setCopyright(String copyright) {
		setTextFrameText(ID_COPYRIGHT, copyright);
	}

	public String getUrl() {
		ID3v2UrlFrameData frameData;
		if (obseleteFormat) frameData = extractUrlFrameData(ID_URL_OBSELETE);
		else frameData = extractUrlFrameData(ID_URL); 
		if (frameData != null) return frameData.getUrl();
		return null;
	}

	public void setUrl(String url) {
		if (url != null && url.length() > 0) {
			invalidateDataLength();
			ID3v2UrlFrameData frameData = new ID3v2UrlFrameData(useFrameUnsynchronisation(), null, url);
			addFrame(createFrame(ID_URL, frameData.toBytes()), true);
		}
	}

	public String getEncoder() {
		return getTextFrameText(obseleteFormat ? ID_ENCODER_OBSELETE : ID_ENCODER);
	}

	public void setEncoder(String encoder) {
		setTextFrameText(ID_ENCODER, encoder);
	}
	
	public byte[] getAlbumImage() {
		ID3v2PictureFrameData frameData;
		if (obseleteFormat) frameData = createPictureFrameData(ID_IMAGE_OBSELETE);
		else frameData = createPictureFrameData(ID_IMAGE); 
		if (frameData != null) return frameData.getImageData();
		return null;
	}

	public void setAlbumImage(byte[] albumImage, String mimeType) {
		if (albumImage != null && albumImage.length > 0 && mimeType != null && mimeType.length() > 0) { 
			invalidateDataLength();
			ID3v2PictureFrameData frameData = new ID3v2PictureFrameData(useFrameUnsynchronisation(), 
					Encoding.getDefault(), mimeType, (byte)0, null, albumImage); 
			addFrame(createFrame(ID_IMAGE, frameData.toBytes()), true);
		}
	}

	public String getAlbumImageMimeType() {
		ID3v2PictureFrameData frameData;
		if (obseleteFormat) frameData = createPictureFrameData(ID_IMAGE_OBSELETE);
		else frameData = createPictureFrameData(ID_IMAGE);
		if (frameData != null && frameData.getMimeType() != null) return frameData.getMimeType();
		return null;
	}
	
	public void clearFrameSet(String id) {
		if (frameSets.remove(id) != null) {
			invalidateDataLength();
		}
	}

	private ID3v2TextFrameData extractTextFrameData(String id) {
		ID3v2FrameSet frameSet = frameSets.get(id);
		if (frameSet != null) {
			ID3v2Frame frame = (ID3v2Frame) frameSet.getFrames().get(0);
			ID3v2TextFrameData frameData;
			try {
				frameData = new ID3v2TextFrameData(useFrameUnsynchronisation(), frame.getData());
				return frameData;
			} catch (InvalidDataException e) {
				// do nothing
			}
		}
		return null;
	}
	
	private ID3v2UrlFrameData extractUrlFrameData(String id) {
		ID3v2FrameSet frameSet = frameSets.get(id);
		if (frameSet != null) {
			ID3v2Frame frame = (ID3v2Frame) frameSet.getFrames().get(0);
			ID3v2UrlFrameData frameData;
			try {
				frameData = new ID3v2UrlFrameData(useFrameUnsynchronisation(), frame.getData());
				return frameData;
			} catch (InvalidDataException e) {
				// do nothing
			}
		}
		return null;
	}
	
	private ID3v2CommentFrameData extractCommentFrameData(String id, boolean itunes) {
		ID3v2FrameSet frameSet = frameSets.get(id);
		if (frameSet != null) {
			Iterator<ID3v2Frame> iterator = frameSet.getFrames().iterator();
			while (iterator.hasNext()) {
				ID3v2Frame frame = (ID3v2Frame) iterator.next();
				ID3v2CommentFrameData frameData;
				try {
					frameData = new ID3v2CommentFrameData(useFrameUnsynchronisation(), frame.getData());
					if (itunes && ITUNES_COMMENT_DESCRIPTION.equals(frameData.getDescription().toString())) {
						return frameData;
					} else if (! itunes) {
						return frameData;
					}
				} catch (InvalidDataException e) {
					// Do nothing
				}
			}
		}
		return null;
	}

	private ID3v2PictureFrameData createPictureFrameData(String id) {
		ID3v2FrameSet frameSet = frameSets.get(id);
		if (frameSet != null) {
			ID3v2Frame frame = (ID3v2Frame) frameSet.getFrames().get(0);
			ID3v2PictureFrameData frameData;
			try {
				if (obseleteFormat) frameData = new ID3v2ObseletePictureFrameData(useFrameUnsynchronisation(), frame.getData());
				else frameData = new ID3v2PictureFrameData(useFrameUnsynchronisation(), frame.getData());
				return frameData;
			} catch (InvalidDataException e) {
				// do nothing
			}
		}
		return null;
	}
	
	
    public Map<String, List<String>> getFrameData() {
        Map<String, ID3v2FrameSet> frameSet = getFrameSets();
        final boolean sync = useFrameUnsynchronisation();
        
        Map<String, List<AbstractID3v2FrameData>> result = Maps.newHashMap();
        for (String key : frameSet.keySet()) {
            List<ID3v2Frame> frames = frameSet.get(key).getFrames();
            List<AbstractID3v2FrameData> frameData = Lists.newArrayList(
                Collections2.transform(frames, 
                    new Function<ID3v2Frame, AbstractID3v2FrameData>() {
                        public AbstractID3v2FrameData apply(ID3v2Frame frame) {
                            return Frame.frameDataFromFrame(frame, sync);
                        }
                    }
                )   
            );
            result.put(key, Lists.newArrayList(frameData));
        }


        Map<String, List<String>> realResult = Maps.newHashMap();
        if (result.containsKey("TXXX")) {
            LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder().
                build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String key) { return Lists.newLinkedList(); }
                } );

            for (AbstractID3v2FrameData adata : result.get("TXXX")) {
                ID3v2UserDefinedTextFrameData data = (ID3v2UserDefinedTextFrameData) adata;
                try {
                    cache.get("TXXX." + data.getDesc() ).add(data.getData());
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }

            result.remove("TXXX");
            realResult.putAll(cache.asMap());
        }

        for (String key : result.keySet()) {
            List<AbstractID3v2FrameData> frames = result.get(key);
            Collection<String> stringFrames = Collections2.transform(frames, 
                Functions.toStringFunction());
            realResult.put(key, Lists.newArrayList(stringFrames));
        }
        
        return realResult;
    }

    
//    
//    for (T t : ts) {
//        R key = mappingFunction.apply(t);
//        try {
//            result.get(key).add(t);
//        } catch (ExecutionException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//    return result.asMap();


	public boolean equals(Object obj) {
		if (! (obj instanceof Tag)) return false;
		if (super.equals(obj)) return true;
		Tag other = (Tag) obj;
		if (unsynchronisation != other.unsynchronisation) return false;
		if (extendedHeader != other.extendedHeader) return false;
		if (experimental != other.experimental) return false;
		if (footer != other.footer) return false;
		if (compression != other.compression) return false;
		if (dataLength != other.dataLength) return false;
		if (extendedHeaderLength != other.extendedHeaderLength) return false;
		if (version == null) {
			if (other.version != null) return false;
		} else if (other.version == null) return false;
		else if (! version.equals(other.version)) return false;
		if (frameSets == null) {
			if (other.frameSets != null) return false;
		} else if (other.frameSets == null) return false;
		else if (! frameSets.equals(other.frameSets)) return false;
		return true;
	}
}
