package com.thirdeye30.resumehelper.tokenmanager.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import com.thirdeye30.resumehelper.tokenmanager.enums.CourseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_company_job_url", columnList = "company, jobUrl"),
        @Index(name = "idx_created_time", columnList = "createTime"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String yoe;
    
    @Column(nullable = false)
    private String company;
    
    @Column(name = "jobDescription", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;
    
    @Column(name = "jobUrl", columnDefinition = "TEXT", nullable = false)
    private String jobUrl;
    
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    @Column(name = "course_result", columnDefinition = "TEXT")
    private String courseResult;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "course_status")
    private CourseStatus courseStatus;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}