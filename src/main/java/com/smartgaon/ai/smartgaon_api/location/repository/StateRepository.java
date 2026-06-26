package com.smartgaon.ai.smartgaon_api.location.repository;

import com.smartgaon.ai.smartgaon_api.location.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findAllByOrderByNameAsc();
}