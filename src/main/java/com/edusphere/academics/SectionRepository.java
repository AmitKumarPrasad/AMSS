package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {
    @Query("""
            select s from Section s
            where s.classId = :classId
              and (:status is null or s.status = :status)
              and (:query is null or lower(s.name) like lower(concat('%', :query, '%'))
                   or lower(coalesce(s.room, '')) like lower(concat('%', :query, '%')))
            order by s.name asc, s.id asc
            """)
    Page<Section> search(@Param("classId") UUID classId,@Param("status") String status,@Param("query") String query,Pageable pageable);
    boolean existsByClassIdAndNameIgnoreCase(UUID classId, String name);
}
