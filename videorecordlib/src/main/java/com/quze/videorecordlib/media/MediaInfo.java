/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.quze.videorecordlib.media;

import java.io.Serializable;

public class MediaInfo implements Serializable{

    public String filePath;
    public String thumbnailPath;
    public String mimeType;
    public String title;
    public long startTime;
    public int duration;
    public int id;
    public long addTime;
    public boolean isSquare;
    public int type;
    public double latitude = Double.MAX_VALUE;
    public double longitude = Double.MAX_VALUE;
    @Override
    public boolean equals(Object o) {
        if(o instanceof MediaInfo){
            MediaInfo info = (MediaInfo)o;
            return id == info.id;
        }
        return false;
    }
}
