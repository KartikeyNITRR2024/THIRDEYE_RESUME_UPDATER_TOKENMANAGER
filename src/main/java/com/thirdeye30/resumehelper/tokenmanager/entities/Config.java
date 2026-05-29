package com.thirdeye30.resumehelper.tokenmanager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Config {

    @Id
    private String id = "GLOBAL_CONFIG";

    @Column(name = "max_token_allocated")
    private Long maximumTokenAllocated;

    @Column(name = "max_time_days")
    private Integer maximumTimeForUserInDays;
}
