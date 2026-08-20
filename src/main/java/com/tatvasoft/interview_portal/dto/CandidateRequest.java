package com.tatvasoft.interview_portal.dto;

import lombok.Data;

@Data
public class CandidateRequest {

    private String firstName;
    private String lastName;
    private String email;
    private Integer experience;
    private String designation;
    private Boolean isActive;
}