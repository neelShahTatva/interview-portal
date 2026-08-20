package com.tatvasoft.interview_portal.service;

public interface EmailService {

    void sendResetPasswordEmail(String toEmail, String resetLink);
}