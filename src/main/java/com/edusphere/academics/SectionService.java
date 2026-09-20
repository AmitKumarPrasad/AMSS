package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class SectionService {
    private static final int MAX_PAGE_SIZE=100;
    private final SectionRepository sectionRepository; private final SchoolClassRepository classRepository;
    public SectionService(SectionRepository sectionRepository, SchoolClassRepository classRepository){this.sectionRepository=sectionRepository;this.classRepository=classRepository;}
    @Transactional public SectionView create(UUID schoolId,UUID classId,CreateSectionRequest request){
        SchoolClass schoolClass=requireClass(schoolId,classId);
        if(!"ACTIVE".equals(schoolClass.getStatus())) bad("Section can only be created for an active class");
        if(request==null||request.name()==null||request.name().isBlank()) bad("name is required");
        String name=request.name().trim(); String room=request.room()==null||request.room().isBlank()?null:request.room().trim();
        if(sectionRepository.existsByClassIdAndNameIgnoreCase(classId,name)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Section name already exists for class");
        return toView(sectionRepository.save(new Section(classId,name,room)));
    }
    @Transactional(readOnly = true)
    public Page<SectionView> list(UUID schoolId,UUID classId,String status,String query,int page,int size){
        requireClass(schoolId,classId); validatePage(page,size);
        String normalizedStatus=status==null||status.isBlank()?null:status.trim().toUpperCase();
        if(normalizedStatus!=null&&!normalizedStatus.equals("ACTIVE")&&!normalizedStatus.equals("INACTIVE")) bad("status must be ACTIVE or INACTIVE");
        String normalizedQuery=query==null||query.isBlank()?null:query.trim();
        PageRequest request=PageRequest.of(page,size,Sort.by(Sort.Order.asc("name"),Sort.Order.asc("id")));
        return sectionRepository.search(classId,normalizedStatus,normalizedQuery,request).map(this::toView);
    }
    @Transactional public SectionView changeStatus(UUID schoolId,UUID sectionId,boolean active){
        Section section=sectionRepository.findById(sectionId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Section not found"));
        SchoolClass schoolClass=requireClass(schoolId,section.getClassId());
        if(active&&!"ACTIVE".equals(schoolClass.getStatus())) bad("Section can only be activated for an active class");
        if(active)section.activate();else section.deactivate(); return toView(sectionRepository.save(section));
    }
    private SchoolClass requireClass(UUID schoolId,UUID classId){if(schoolId==null||classId==null)bad("schoolId and classId are required");return classRepository.findById(classId).filter(c->schoolId.equals(c.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Class not found"));}
    private void validatePage(int page,int size){if(page<0)bad("page must be >= 0");if(size<1||size>MAX_PAGE_SIZE)bad("size must be between 1 and "+MAX_PAGE_SIZE);}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private SectionView toView(Section s){return new SectionView(s.getId(),s.getClassId(),s.getName(),s.getRoom(),s.getStatus());}
    public record CreateSectionRequest(String name,String room){} public record SectionView(UUID id,UUID classId,String name,String room,String status){}
}
