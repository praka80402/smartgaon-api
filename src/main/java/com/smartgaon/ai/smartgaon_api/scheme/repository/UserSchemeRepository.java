package com.smartgaon.ai.smartgaon_api.scheme.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.scheme.entity.*;

public interface UserSchemeRepository extends JpaRepository<Scheme, Long> {

    List<Scheme> findByCategoryIdAndSchemeType(
            Long categoryId,
            SchemeType schemeType
    );

    List<Scheme> findByCategoryIdAndSchemeTypeAndState(
            Long categoryId,
            SchemeType schemeType,
            State state
    );
}
