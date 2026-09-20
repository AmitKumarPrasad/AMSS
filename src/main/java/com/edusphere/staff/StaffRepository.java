package com.edusphere.staff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StaffRepository extends JpaRepository<StaffMember, UUID> {
    boolean existsBySchoolIdAndEmployeeCode(UUID schoolId, String employeeCode);

    @Query("""
            select s from StaffMember s
            where s.schoolId = :schoolId
              and s.status = :status
              and (
                    :query is null
                    or lower(s.fullName) like lower(concat('%', :query, '%'))
                    or lower(s.employeeCode) like lower(concat('%', :query, '%'))
                    or lower(coalesce(s.designation, '')) like lower(concat('%', :query, '%'))
              )
            order by s.fullName asc, s.id asc
            """)
    Page<StaffMember> search(@Param("schoolId") UUID schoolId,
                             @Param("status") String status,
                             @Param("query") String query,
                             Pageable pageable);
}
