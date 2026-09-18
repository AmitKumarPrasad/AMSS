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
        AcademicYear year = yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        if (!"ACTIVE".equals(year.getStatus())) bad("Class can only be created for an active academic year");
        if (r.name() == null || r.name().isBlank()) bad("name is required");
        if (r.gradeLevel() == null || r.gradeLevel() < 1) bad("gradeLevel must be positive");
        String name = r.name().trim();
        if (repository.existsBySchoolIdAndAcademicYearIdAndNameIgnoreCase(schoolId, academicYearId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Class name already exists for academic year");
        }
        SchoolClass c=repository.save(new SchoolClass(schoolId,academicYearId,name,r.gradeLevel())); return view(c);
    }

    @Transactional(readOnly=true)
    public List<ClassView> list(UUID schoolId,UUID academicYearId){
        if (schoolId == null || academicYearId == null) bad("schoolId and academicYearId are required");
        yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        return repository.findBySchoolIdAndAcademicYearIdOrderByGradeLevelAscNameAsc(schoolId,academicYearId).stream().map(this::view).toList();
    }

    private ClassView view(SchoolClass c){return new ClassView(c.getId(),c.getSchoolId(),c.getAcademicYearId(),c.getName(),c.getGradeLevel());}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    public record CreateClassRequest(String name,Integer gradeLevel){} public record ClassView(UUID id,UUID schoolId,UUID academicYearId,String name,Integer gradeLevel){}
}
