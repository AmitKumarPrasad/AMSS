package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolClassService {
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
    public List<ClassView> list(UUID schoolId,UUID academicYearId){
        if(schoolId==null||academicYearId==null) bad("schoolId and academicYearId are required");
        yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        return repository.findBySchoolIdAndAcademicYearIdOrderByGradeLevelAscNameAsc(schoolId,academicYearId).stream().map(this::view).toList();
    }
    @Transactional public ClassView changeStatus(UUID schoolId,UUID classId,boolean active){
        SchoolClass c=repository.findById(classId).filter(x->schoolId.equals(x.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Class not found"));
        if(active){ AcademicYear y=yearRepository.findById(c.getAcademicYearId()).filter(x->schoolId.equals(x.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found")); if(!"ACTIVE".equals(y.getStatus())) bad("Class can only be activated for an active academic year"); c.activate(); } else c.deactivate();
        return view(repository.save(c));
    }
    private ClassView view(SchoolClass c){return new ClassView(c.getId(),c.getSchoolId(),c.getAcademicYearId(),c.getName(),c.getGradeLevel(),c.getStatus());}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record CreateClassRequest(String name,Integer gradeLevel){} public record ClassView(UUID id,UUID schoolId,UUID academicYearId,String name,Integer gradeLevel,String status){}
}
