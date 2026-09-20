package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
    @Query("""
            select s from Subject s
            where s.schoolId = :schoolId
              and s.status = 'ACTIVE'
              and (:query is null
                   or lower(s.name) like lower(concat('%', :query, '%'))
                   or lower(s.code) like lower(concat('%', :query, '%')))
            order by s.name asc, s.id asc
            """)
    Page<Subject> search(@Param("schoolId") UUID schoolId,@Param("query") String query,Pageable pageable);
}
