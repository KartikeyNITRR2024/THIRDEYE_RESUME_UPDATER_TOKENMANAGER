package com.thirdeye30.resumehelper.tokenmanager.repos;

import com.thirdeye30.resumehelper.tokenmanager.entities.Course;
import com.thirdeye30.resumehelper.tokenmanager.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createTime >= :startDate " +
           "AND c.courseStatus IN (:statuses)")
    long countByCreateTimeAfterAndStatusIn(
        @Param("startDate") LocalDateTime startDate, 
        @Param("statuses") List<CourseStatus> statuses
    );
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.email = :email " +
    	       "AND c.createTime >= :startOfDay " +
    	       "AND c.courseStatus IN (:statuses)")
    long countByEmailAndDateAndStatusIn(
    	 @Param("email") String email,
    	 @Param("startOfDay") LocalDateTime startOfDay,
    	 @Param("statuses") List<CourseStatus> statuses
    );
    
    List<Course> findByEmail(String email);
    
    @Modifying
    @Query("UPDATE Course c SET c.courseStatus = :status, c.result = :result WHERE c.id = :id")
    int updateStatusAndResultById(
        @Param("id") UUID id, 
        @Param("status") CourseStatus status, 
        @Param("result") String result
    );
}
