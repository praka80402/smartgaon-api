package com.smartgaon.ai.smartgaon_api.admin.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginRequest {
    private String email;
    private String password;
}

