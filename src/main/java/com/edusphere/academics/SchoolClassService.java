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
    @Transactional public ClassView create(UUID schoolId,UUID academicYearId,CreateClassRequest r){
        yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        if(r.gradeLevel()<1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"gradeLevel must be positive");
        SchoolClass c=repository.save(new SchoolClass(schoolId,academicYearId,r.name().trim(),r.gradeLevel())); return view(c);
    }
    @Transactional(readOnly=true) public List<ClassView> list(UUID schoolId,UUID academicYearId){
        yearRepository.findById(academicYearId).filter(y->schoolId.equals(y.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic year not found"));
        return repository.findBySchoolIdAndAcademicYearIdOrderByGradeLevelAscNameAsc(schoolId,academicYearId).stream().map(this::view).toList();
    }
    private ClassView view(SchoolClass c){return new ClassView(c.getId(),c.getSchoolId(),c.getAcademicYearId(),c.getName(),c.getGradeLevel());}
    public record CreateClassRequest(String name,Integer gradeLevel){} public record ClassView(UUID id,UUID schoolId,UUID academicYearId,String name,Integer gradeLevel){}
}
