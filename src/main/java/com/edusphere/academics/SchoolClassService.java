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
public class SchoolClassService {
    private static final int MAX_PAGE_SIZE = 100;
    private final SchoolClassRepository repository; private final AcademicYearRepository yearRepository;
    public SchoolClassService(SchoolClassRepository repository,AcademicYearRepository yearRepository){this.repository=repository;this.yearRepository=yearRepository;}
    @Transactional
    public ClassView create(UUID schoolId,UUID academicYearId,CreateClassRequest r){
        if (schoolId == null || academicYearId == null || r == null) bad("schoolId, academicYearId and class request are required");
        AcademicYear year = yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        if (!"ACTIVE".equals(year.getStatus())) bad("Class can only be created for an active academic year");
        if (r.name() == null || r.name().isBlank()) bad("name is required");
        if (r.gradeLevel() == null || r.gradeLevel() < 1) bad("gradeLevel must be positive");
        String name=r.name().trim();
        if(repository.existsBySchoolIdAndAcademicYearIdAndNameIgnoreCase(schoolId,academicYearId,name)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Class name already exists for academic year");
        return view(repository.save(new SchoolClass(schoolId,academicYearId,name,r.gradeLevel())));
    }
    @Transactional(readOnly=true)
    public Page<ClassView> list(UUID schoolId,UUID academicYearId,int page,int size){
        if(schoolId==null||academicYearId==null) bad("schoolId and academicYearId are required");
        validatePage(page,size);
        yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        PageRequest request=PageRequest.of(page,size,Sort.by(Sort.Order.asc("gradeLevel"),Sort.Order.asc("name"),Sort.Order.asc("id")));
        return repository.search(schoolId,academicYearId,request).map(this::view);
    }
    @Transactional public ClassView changeStatus(UUID schoolId,UUID classId,boolean active){
        SchoolClass c=repository.findById(classId).filter(x->schoolId.equals(x.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Class not found"));
        if(active){ AcademicYear y=yearRepository.findById(c.getAcademicYearId()).filter(x->schoolId.equals(x.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found")); if(!"ACTIVE".equals(y.getStatus())) bad("Class can only be activated for an active academic year"); c.activate(); } else c.deactivate();
        return view(repository.save(c));
    }
    private ClassView view(SchoolClass c){return new ClassView(c.getId(),c.getSchoolId(),c.getAcademicYearId(),c.getName(),c.getGradeLevel(),c.getStatus());}
    private void validatePage(int page,int size){if(page<0)bad("page must be >= 0");if(size<1||size>MAX_PAGE_SIZE)bad("size must be between 1 and "+MAX_PAGE_SIZE);}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record CreateClassRequest(String name,Integer gradeLevel){} public record ClassView(UUID id,UUID schoolId,UUID academicYearId,String name,Integer gradeLevel,String status){}
}
