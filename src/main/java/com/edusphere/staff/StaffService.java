package com.edusphere.staff;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class StaffService {
    private static final List<String> EMPLOYMENT_TYPES=List.of("FULL_TIME","PART_TIME","CONTRACT");
    private static final List<String> LEAVE_TYPES=List.of("CASUAL","SICK","EARNED","UNPAID","OTHER");
    private final StaffRepository staffRepository; private final LeaveRequestRepository leaveRepository; private final JdbcTemplate jdbc;
    public StaffService(StaffRepository staffRepository, LeaveRequestRepository leaveRepository){this(staffRepository, leaveRepository, null);}
    @Autowired
    public StaffService(StaffRepository staffRepository, LeaveRequestRepository leaveRepository, JdbcTemplate jdbc){this.staffRepository=staffRepository;this.leaveRepository=leaveRepository;this.jdbc=jdbc;}

    @Transactional public StaffView create(UUID schoolId, CreateStaffRequest r){
        if (r == null || r.employeeCode() == null || r.fullName() == null || r.designation() == null)
            bad("employeeCode, fullName and designation are required");
        String code=requiredText(r.employeeCode(),"employeeCode");
        String name=requiredText(r.fullName(),"fullName");
        String designation=requiredText(r.designation(),"designation");
        if(r.joinedOn()!=null && r.joinedOn().isAfter(LocalDate.now())) bad("joinedOn cannot be in the future");
        if(staffRepository.existsBySchoolIdAndEmployeeCode(schoolId,code))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee code already exists");
        String type=normalizeType(r.employmentType());
        StaffMember s=new StaffMember(schoolId,code,name,trim(r.email()),trim(r.phone()),designation,type,r.joinedOn());
        return view(staffRepository.save(s));
    }
    @Transactional public StaffView changeStatus(UUID schoolId, UUID staffId, boolean active){
        StaffMember staff=staffRepository.findById(staffId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        if(active) staff.activate(); else staff.deactivate();
        return view(staffRepository.save(staff));
    }

    @Transactional(readOnly=true) public List<StaffView> list(UUID schoolId){
        return staffRepository.findBySchoolIdAndStatusOrderByFullNameAsc(schoolId,"ACTIVE").stream().map(this::view).toList();
    }
    @Transactional public LeaveView requestLeave(UUID schoolId, UUID staffId, CreateLeaveRequest r){
        if (r == null || r.leaveType() == null || r.startsOn() == null || r.endsOn() == null)
            bad("leaveType, startsOn and endsOn are required");
        StaffMember staff=staffRepository.findById(staffId)
                .filter(s->schoolId.equals(s.getSchoolId())&&"ACTIVE".equals(s.getStatus()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Staff member not found"));
        if(r.startsOn().isAfter(r.endsOn())) bad("startsOn must be on or before endsOn");
        if (r.startsOn().isBefore(LocalDate.now())) bad("startsOn cannot be in the past");
        String type=requiredText(r.leaveType(),"leaveType").toUpperCase(Locale.ROOT);
        if(!LEAVE_TYPES.contains(type)) bad("Invalid leaveType");
        if(leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(staffId,"APPROVED",r.endsOn(),r.startsOn()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave overlaps approved leave");
        if(leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(staffId,"PENDING",r.endsOn(),r.startsOn()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave overlaps pending leave");
        return leave(leaveRepository.save(new LeaveRequest(schoolId,staffId,type,r.startsOn(),r.endsOn(),trim(r.reason()))));
    }
    @Transactional(readOnly=true) public List<LeaveView> leaves(UUID schoolId){
        return leaveRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId).stream().map(this::leave).toList();
    }
    @Transactional public LeaveView review(UUID schoolId,UUID leaveId,String status,UUID reviewer){
        if (status == null || status.isBlank()) bad("status is required");
        if (reviewer == null) bad("reviewer is required");
        if (jdbc != null && !reviewerBelongsToSchool(schoolId, reviewer))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found");
        LeaveRequest l=leaveRepository.findById(leaveId).filter(x->schoolId.equals(x.getSchoolId()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Leave request not found"));
        String s=status.trim().toUpperCase(Locale.ROOT);
        if(!List.of("APPROVED","REJECTED").contains(s)) bad("status must be APPROVED or REJECTED");
        if(!"PENDING".equals(l.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave request already reviewed");
        if("APPROVED".equals(s)&&leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(l.getStaffId(),"APPROVED",l.getEndsOn(),l.getStartsOn()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Leave overlaps approved leave");
        l.review(s,reviewer); return leave(leaveRepository.save(l));
    }
    private boolean reviewerBelongsToSchool(UUID schoolId, UUID reviewerId) {
        return Boolean.TRUE.equals(jdbc.query("SELECT EXISTS (SELECT 1 FROM app_users WHERE id=? AND school_id=? AND status='ACTIVE')",
                rs -> { rs.next(); return rs.getBoolean(1); }, reviewerId, schoolId));
    }

    private String normalizeType(String v){String s=v==null?"FULL_TIME":v.trim().toUpperCase(Locale.ROOT);if(!EMPLOYMENT_TYPES.contains(s))bad("Invalid employmentType");return s;}
    private String requiredText(String value,String field){String s=value==null?null:value.trim();if(s==null||s.isBlank())bad(field+" is required");return s;}
    private String trim(String s){return s==null||s.isBlank()?null:s.trim();}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private StaffView view(StaffMember s){return new StaffView(s.getId(),s.getSchoolId(),s.getEmployeeCode(),s.getFullName(),s.getEmail(),s.getPhone(),s.getDesignation(),s.getEmploymentType(),s.getJoinedOn(),s.getStatus());}
    private LeaveView leave(LeaveRequest l){return new LeaveView(l.getId(),l.getSchoolId(),l.getStaffId(),l.getLeaveType(),l.getStartsOn(),l.getEndsOn(),l.getReason(),l.getStatus(),l.getReviewedBy(),l.getReviewedAt(),l.getCreatedAt());}
    public record CreateStaffRequest(String employeeCode,String fullName,String email,String phone,String designation,String employmentType,LocalDate joinedOn){}
    public record CreateLeaveRequest(String leaveType,LocalDate startsOn,LocalDate endsOn,String reason){}
    public record StaffView(UUID id,UUID schoolId,String employeeCode,String fullName,String email,String phone,String designation,String employmentType,LocalDate joinedOn,String status){}
    public record LeaveView(UUID id,UUID schoolId,UUID staffId,String leaveType,LocalDate startsOn,LocalDate endsOn,String reason,String status,UUID reviewedBy,java.time.Instant reviewedAt,java.time.Instant createdAt){}
}
