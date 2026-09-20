package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AcademicYearService {
    private static final int MAX_PAGE_SIZE = 100;
    private final AcademicYearRepository repository;
    public AcademicYearService(AcademicYearRepository repository){this.repository=repository;}

    @Transactional
    public AcademicYearView create(UUID schoolId, CreateAcademicYearRequest request) {
        String code = normalize(request.code(), "code");
        String name = normalize(request.name(), "name");
        if (request.startsOn().isAfter(request.endsOn()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"startsOn must be on or before endsOn");
        if (repository.existsBySchoolIdAndCode(schoolId, code))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Academic year code already exists for this school");
        if (repository.existsBySchoolIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
                schoolId, request.endsOn(), request.startsOn()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Academic year dates overlap an existing academic year");
        AcademicYear year = repository.save(new AcademicYear(schoolId, code, name, request.startsOn(), request.endsOn()));
        return view(year);
    }

    @Transactional
    public AcademicYearView changeStatus(UUID schoolId, UUID academicYearId, boolean active) {
        AcademicYear year = repository.findById(academicYearId)
                .filter(y -> schoolId.equals(y.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        if (active) {
            if (repository.existsBySchoolIdAndStatus(schoolId, "ACTIVE") && !"ACTIVE".equals(year.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Another academic year is already active");
            }
            year.activate();
        } else {
            year.deactivate();
        }
        return view(repository.save(year));
    }

    @Transactional(readOnly=true)
    public Page<AcademicYearView> list(UUID schoolId, String status, int page, int size){
        validatePage(page,size);
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        if (normalizedStatus != null && !normalizedStatus.equals("ACTIVE") && !normalizedStatus.equals("INACTIVE"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"status must be ACTIVE or INACTIVE");
        PageRequest request = PageRequest.of(page,size,Sort.by(Sort.Order.desc("startsOn"),Sort.Order.desc("id")));
        return repository.search(schoolId,normalizedStatus,request).map(this::view);
    }

    private void validatePage(int page,int size){
        if(page<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"page must be >= 0");
        if(size<1||size>MAX_PAGE_SIZE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"size must be between 1 and "+MAX_PAGE_SIZE);
    }
    private String normalize(String value, String field) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        return value.trim();
    }
    private AcademicYearView view(AcademicYear y){
        return new AcademicYearView(y.getId(),y.getSchoolId(),y.getCode(),y.getName(),y.getStartsOn(),y.getEndsOn(),y.getStatus());
    }
    public record CreateAcademicYearRequest(String code,String name,LocalDate startsOn,LocalDate endsOn){}
    public record AcademicYearView(UUID id,UUID schoolId,String code,String name,LocalDate startsOn,LocalDate endsOn,String status){}
}
