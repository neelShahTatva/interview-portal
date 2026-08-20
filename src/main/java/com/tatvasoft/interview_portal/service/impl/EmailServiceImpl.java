package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendResetPasswordEmail(String toEmail,
                                       String resetLink) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);

            helper.setSubject("Reset Password - Interview Portal");

            String htmlContent = """
                    
                    <div style="
                        font-family: Arial, sans-serif;
                        max-width: 600px;
                        margin: auto;
                        padding: 20px;
                        border: 1px solid #e5e7eb;
                        border-radius: 10px;
                        background-color: #f9fafb;
                    ">
                    
                        <h2 style="color: #2563eb;">
                            Interview Portal
                        </h2>

                        <p style="font-size: 16px; color: #374151;">
                            We received a request to reset your password.
                        </p>

                        <p style="font-size: 15px; color: #374151;">
                            Click the button below to reset your password:
                        </p>

                        <div style="margin: 30px 0;">
                            <a href="%s"
                               style="
                                   background-color: #2563eb;
                                   color: white;
                                   padding: 12px 20px;
                                   text-decoration: none;
                                   border-radius: 6px;
                                   font-size: 16px;
                                   font-weight: bold;
                                   display: inline-block;
                               ">
                                Reset Password
                            </a>
                        </div>

                        <p style="font-size: 14px; color: #6b7280;">
                            This link will expire in 15 minutes.
                        </p>

                        <p style="font-size: 14px; color: #6b7280;">
                            If you did not request a password reset,
                            please ignore this email.
                        </p>

                        <hr style="margin-top: 30px;" />

                        <p style="
                            font-size: 13px;
                            color: #9ca3af;
                            text-align: center;
                        ">
                            © 2026 Interview Portal. All rights reserved.
                        </p>

                    </div>
                    """.formatted(resetLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException("Failed to send email");
        }
    }
}