package com.edusphere.communication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunicationService {
    private static final Set<String> AUDIENCES=Set.of("SUPER_ADMIN","SCHOOL_ADMIN","TEACHER","ACCOUNTANT","PARENT","STUDENT");
    private static final int MAX_PAGE=100;
    private final AnnouncementRepository announcementRepository; private final AnnouncementReadRepository readRepository;
    public CommunicationService(AnnouncementRepository a,AnnouncementReadRepository r){announcementRepository=a;readRepository=r;}

    @Transactional public AnnouncementView create(UUID schoolId,String title,String body,String audienceRole,UUID createdBy){
        if(schoolId==null||createdBy==null)bad("schoolId and createdBy are required");
        String t=req(title,"title"),b=req(body,"body");
        String a=audienceRole==null||audienceRole.isBlank()?null:audienceRole.trim().toUpperCase(Locale.ROOT);
        if(a!=null&&!AUDIENCES.contains(a))bad("invalid audienceRole");
        return toView(announcementRepository.save(new Announcement(schoolId,t,b,a,createdBy)));
    }
    @Transactional public AnnouncementView publish(UUID schoolId,UUID id){Announcement a=get(schoolId,id);if("PUBLISHED".equals(a.getStatus()))bad("Announcement is already published");a.publish();return toView(announcementRepository.save(a));}

    @Transactional(readOnly=true)
    public Page<AnnouncementView> list(UUID schoolId,boolean publishedOnly,Collection<String> roles,int page,int size){
        if(schoolId==null)bad("schoolId is required");
        if(page<0)bad("page must be zero or greater");
        if(size<1||size>MAX_PAGE)bad("size must be between 1 and "+MAX_PAGE);
        Set<String> allowed=normalizeRoles(roles);
        var p=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,publishedOnly?"publishedAt":"createdAt"));
        Page<Announcement> results;
        if(!publishedOnly) results=announcementRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId,p);
        else if(allowed.contains("SUPER_ADMIN")) results=announcementRepository.findBySchoolIdAndStatusOrderByPublishedAtDesc(schoolId,"PUBLISHED",p);
        else results=announcementRepository.findPublishedVisible(schoolId,"PUBLISHED",allowed.isEmpty()?Set.of("__NO_ROLE__"):allowed,p);
        return results.map(this::toView);
    }
    @Transactional public void markRead(UUID schoolId,UUID id,UUID userId){if(userId==null)bad("userId is required");Announcement a=get(schoolId,id);if(!"PUBLISHED".equals(a.getStatus()))bad("Only published announcements can be marked as read");if(!readRepository.existsByAnnouncementIdAndUserId(id,userId))readRepository.save(new AnnouncementRead(id,userId));}
    private Set<String> normalizeRoles(Collection<String> roles){if(roles==null)return Set.of();return roles.stream().filter(Objects::nonNull).map(v->v.replace("ROLE_","").trim().toUpperCase(Locale.ROOT)).filter(AUDIENCES::contains).collect(Collectors.toUnmodifiableSet());}
    private Announcement get(UUID schoolId,UUID id){return announcementRepository.findById(id).filter(a->schoolId.equals(a.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Announcement not found"));}
    private String req(String v,String f){if(v==null||v.isBlank())bad(f+" is required");return v.trim();} private void bad(String m){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private AnnouncementView toView(Announcement a){return new AnnouncementView(a.getId(),a.getSchoolId(),a.getTitle(),a.getBody(),a.getAudienceRole(),a.getPublishedAt(),a.getStatus(),a.getCreatedBy(),a.getCreatedAt());}
    public record AnnouncementView(UUID id,UUID schoolId,String title,String body,String audienceRole,Instant publishedAt,String status,UUID createdBy,Instant createdAt){}
}