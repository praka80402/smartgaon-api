package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import org.springframework.stereotype.Service;

@Service
public class ReferenceNumberService {

    public String generate() {
        return "GT-" + System.currentTimeMillis();
    }
}

