package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AcademicYearService {
    private final AcademicYearRepository repository;
    public AcademicYearService(AcademicYearRepository repository){this.repository=repository;}

    @Transactional
    public AcademicYearView create(UUID schoolId, CreateAcademicYearRequest request) {
        if (request.startsOn().isAfter(request.endsOn()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"startsOn must be on or before endsOn");
        AcademicYear year = repository.save(new AcademicYear(schoolId, request.code().trim(), request.name().trim(), request.startsOn(), request.endsOn()));
        return view(year);
    }

    @Transactional(readOnly=true)
    public List<AcademicYearView> list(UUID schoolId){return repository.findBySchoolIdOrderByStartsOnDesc(schoolId).stream().map(this::view).toList();}
    private AcademicYearView view(AcademicYear y){return new AcademicYearView(y.getId(),y.getSchoolId(),y.getCode(),y.getName(),y.getStartsOn(),y.getEndsOn(),y.getStatus());}

    public record CreateAcademicYearRequest(String code,String name,LocalDate startsOn,LocalDate endsOn){}
    public record AcademicYearView(UUID id,UUID schoolId,String code,String name,LocalDate startsOn,LocalDate endsOn,String status){}
}
