package com.auth.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            if (fromEmail != null && !fromEmail.isEmpty()) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject("Your Verification Code");
            
            String htmlContent = createEmailTemplate(otp);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email. Please check email configuration.");
        }
    }

    private String createEmailTemplate(String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f4f4f4; padding: 20px;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">\n" +
                "                    <!-- Header -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px; text-align: center;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;\">Verification Code</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                "                    <!-- Content -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 30px;\">\n" +
                "                            <p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;\">\n" +
                "                                Hello,\n" +
                "                            </p>\n" +
                "                            <p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;\">\n" +
                "                                Your verification code is:\n" +
                "                            </p>\n" +
                "                            \n" +
                "                            <!-- OTP Box -->\n" +
                "                            <div style=\"background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 0 0 30px 0;\">\n" +
                "                                <span style=\"font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px;\">" + otp + "</span>\n" +
                "                            </div>\n" +
                "                            \n" +
                "                            <p style=\"color: #666666; font-size: 14px; line-height: 1.6; margin: 0 0 10px 0;\">\n" +
                "                                This code will expire in <strong>10 minutes</strong>.\n" +
                "                            </p>\n" +
                "                            <p style=\"color: #666666; font-size: 14px; line-height: 1.6; margin: 0;\">\n" +
                "                                If you didn't request this code, please ignore this email.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                "                    <!-- Footer -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;\">\n" +
                "                            <p style=\"color: #999999; font-size: 12px; margin: 0; line-height: 1.5;\">\n" +
                "                                This is an automated message, please do not reply.\n" +
                "                            </p>\n" +
                "                            <p style=\"color: #999999; font-size: 12px; margin: 10px 0 0 0;\">\n" +
                "                                © 2024 Auth Service. All rights reserved.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }
}
