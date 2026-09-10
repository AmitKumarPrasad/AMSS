package com.edusphere.communication;

import java.io.Serializable;
import java.util.UUID;

public class AnnouncementReadId implements Serializable {
    private UUID announcementId;
    private UUID userId;

    public AnnouncementReadId() {}
    public AnnouncementReadId(UUID announcementId, UUID userId) { this.announcementId=announcementId; this.userId=userId; }
    public UUID getAnnouncementId(){return announcementId;} public UUID getUserId(){return userId;}
    @Override public boolean equals(Object o){
        if(this==o)return true; if(!(o instanceof AnnouncementReadId that))return false;
        return java.util.Objects.equals(announcementId,that.announcementId)&&java.util.Objects.equals(userId,that.userId);
    }
    @Override public int hashCode(){return java.util.Objects.hash(announcementId,userId);}
}
