package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class SectionService {
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
    @Transactional(readOnly = true) public List<SectionView> list(UUID schoolId,UUID classId){requireClass(schoolId,classId);return sectionRepository.findByClassIdOrderByNameAsc(classId).stream().map(this::toView).toList();}
    @Transactional public SectionView changeStatus(UUID schoolId,UUID sectionId,boolean active){
        Section section=sectionRepository.findById(sectionId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Section not found"));
        SchoolClass schoolClass=requireClass(schoolId,section.getClassId());
        if(active&& !"ACTIVE".equals(schoolClass.getStatus())) bad("Section can only be activated for an active class");
        if(active)section.activate();else section.deactivate(); return toView(sectionRepository.save(section));
    }
    private SchoolClass requireClass(UUID schoolId,UUID classId){if(schoolId==null||classId==null)bad("schoolId and classId are required");return classRepository.findById(classId).filter(c->schoolId.equals(c.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Class not found"));}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private SectionView toView(Section s){return new SectionView(s.getId(),s.getClassId(),s.getName(),s.getRoom(),s.getStatus());}
    public record CreateSectionRequest(String name,String room){} public record SectionView(UUID id,UUID classId,String name,String room,String status){}
}
