package com.smartgaon.ai.smartgaon_api.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyBusinessInterestRequest {

    @NotNull(message = "Business id is required")
    private Long businessId;

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name can be max 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number can be max 20 characters")
    private String phone;

    @Size(max = 500, message = "Message can be max 500 characters")
    private String message;
}