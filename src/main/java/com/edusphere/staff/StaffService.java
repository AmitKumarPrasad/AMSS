package com.edusphere.staff;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class StaffService {
    private static final String[] TYPES={"FULL_TIME","PART_TIME","CONTRACT"};
    private final StaffRepository staffRepository; private final LeaveRequestRepository leaveRepository;
    public StaffService(StaffRepository staffRepository, LeaveRequestRepository leaveRepository){this.staffRepository=staffRepository;this.leaveRepository=leaveRepository;}
    @Transactional public StaffView create(UUID schoolId, CreateStaffRequest r){
        String code=r.employeeCode().trim(); if(staffRepository.existsBySchoolIdAndEmployeeCode(schoolId,code)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee code already exists");
        String type=normalizeType(r.employmentType()); StaffMember s=new StaffMember(schoolId,code,r.fullName().trim(),trim(r.email()),trim(r.phone()),r.designation().trim(),type,r.joinedOn()); return view(staffRepository.save(s));
    }
    @Transactional(readOnly=true) public List<StaffView> list(UUID schoolId){return staffRepository.findBySchoolIdAndStatusOrderByFullNameAsc(schoolId,"ACTIVE").stream().map(this::view).toList();}
    @Transactional public LeaveView requestLeave(UUID schoolId, UUID staffId, CreateLeaveRequest r){
        StaffMember staff=staffRepository.findById(staffId).filter(s->schoolId.equals(s.getSchoolId())&&"ACTIVE".equals(s.getStatus())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Staff member not found"));
        if(r.startsOn()==null||r.endsOn()==null||r.startsOn().isAfter(r.endsOn())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"startsOn must be on or before endsOn");
        String type=r.leaveType().trim().toUpperCase(Locale.ROOT); if(!List.of("CASUAL","SICK","EARNED","UNPAID","OTHER").contains(type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid leaveType");
        if(leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(staff.getId(),"APPROVED",r.endsOn(),r.startsOn())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave overlaps approved leave");
        return leave(leaveRepository.save(new LeaveRequest(schoolId,staff.getId(),type,r.startsOn(),r.endsOn(),trim(r.reason()))));
    }
    @Transactional(readOnly=true) public List<LeaveView> leaves(UUID schoolId){return leaveRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId).stream().filter(l->schoolId.equals(l.getSchoolId())).map(this::leave).toList();}
    @Transactional public LeaveView review(UUID schoolId,UUID leaveId,String status,UUID reviewer){
        LeaveRequest l=leaveRepository.findById(leaveId).filter(x->schoolId.equals(x.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Leave request not found"));
        String s=status.trim().toUpperCase(Locale.ROOT); if(!List.of("APPROVED","REJECTED").contains(s)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"status must be APPROVED or REJECTED");
        if(!"PENDING".equals(l.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave request already reviewed");
        if("APPROVED".equals(s)&&leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(l.getStaffId(),"APPROVED",l.getEndsOn(),l.getStartsOn())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave overlaps approved leave");
        l.review(s,reviewer); return leave(leaveRepository.save(l));
    }
    private String normalizeType(String v){String s=v==null?"FULL_TIME":v.trim().toUpperCase(Locale.ROOT);if(!List.of(TYPES).contains(s))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid employmentType");return s;}
    private String trim(String s){return s==null||s.isBlank()?null:s.trim();}
    private StaffView view(StaffMember s){return new StaffView(s.getId(),s.getSchoolId(),s.getEmployeeCode(),s.getFullName(),s.getEmail(),s.getPhone(),s.getDesignation(),s.getEmploymentType(),s.getJoinedOn(),s.getStatus());}
    private LeaveView leave(LeaveRequest l){return new LeaveView(l.getId(),l.getSchoolId(),l.getStaffId(),l.getLeaveType(),l.getStartsOn(),l.getEndsOn(),l.getReason(),l.getStatus(),l.getReviewedBy(),l.getReviewedAt(),l.getCreatedAt());}
    public record CreateStaffRequest(String employeeCode,String fullName,String email,String phone,String designation,String employmentType,LocalDate joinedOn){}
    public record CreateLeaveRequest(String leaveType,LocalDate startsOn,LocalDate endsOn,String reason){}
    public record StaffView(UUID id,UUID schoolId,String employeeCode,String fullName,String email,String phone,String designation,String employmentType,LocalDate joinedOn,String status){}
    public record LeaveView(UUID id,UUID schoolId,UUID staffId,String leaveType,LocalDate startsOn,LocalDate endsOn,String reason,String status,UUID reviewedBy,java.time.Instant reviewedAt,java.time.Instant createdAt){}
}
