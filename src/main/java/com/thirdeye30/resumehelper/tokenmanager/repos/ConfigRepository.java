package com.thirdeye30.resumehelper.tokenmanager.repos;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thirdeye30.resumehelper.tokenmanager.entities.Config;

@Repository
public interface ConfigRepository extends JpaRepository<Config, String> {
}
