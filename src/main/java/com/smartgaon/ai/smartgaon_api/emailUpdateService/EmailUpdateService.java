package com.smartgaon.ai.smartgaon_api.emailUpdateService;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailUpdateService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        String otp;
        long expiryTime;
        OtpData(String otp, long expiryTime) { this.otp = otp; this.expiryTime = expiryTime; }
    }

    public enum OtpStatus { VALID, INVALID, EXPIRED, NOT_FOUND }

    public String sendOtp(String newEmail) {
        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(1000 + random.nextInt(9000));
        otpStorage.put(newEmail, new OtpData(otp, System.currentTimeMillis() + 10 * 60 * 1000));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(newEmail);
            helper.setSubject("SmartGaon - Email Verification Code");

String logoUrl = "https://smartgaonvideosconverter-source71e471f1-ky0nypesuuxx.s3.ap-south-1.amazonaws.com/forum_media/2729fee7-2d9f-4ce4-8844-c24ad6622095-adaptive-icon.png";
String htmlContent = """
<div style="font-family: 'Inter', 'Segoe UI', Arial, sans-serif; background-color: #f5f7f6; padding: 32px;">
  <div style="max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 30px rgba(0,0,0,0.06);">
    <div style="background: linear-gradient(135deg, #1b5e20, #43a047); padding: 28px; text-align: center;">
     <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin: 0 auto;">
  <tr>
    <td style="vertical-align: middle; padding-right: 14px;">
      <img src="%s" alt="SmartGaon Logo" width="42" height="42" style="width: 42px; height: 42px; border-radius: 50%%; background: #ffffff; border: 2px solid rgba(255,255,255,0.4); object-fit: cover; display: block;">
    </td>
    <td style="vertical-align: middle;">
      <h1 style="color: white; margin: 0; font-size: 26px; font-weight: 700; letter-spacing: 0.5px; line-height: 1;">SmartGaon</h1>
    </td>
  </tr>
</table>
      <p style="color: #c8e6c9; margin: 8px 0 0 0; font-size: 13px; letter-spacing: 1.5px; text-transform: uppercase;">Smart Village Platform</p>
    </div>
    <div style="padding: 36px 32px;">
      <h2 style="color: #212121; margin: 0 0 12px 0; font-size: 20px; font-weight: 600;">Verify Your Email</h2>
      <p style="color: #616161; font-size: 15px; line-height: 1.7; margin: 0;">
        You requested to update your email address. Please use the verification code below to continue:
      </p>
      <div style="text-align: center; margin: 32px 0;">
        <div style="background: #f1f8e9; border: 2px dashed #43a047; border-radius: 12px; padding: 18px 32px; display: inline-block;">
          <span style="font-size: 36px; font-weight: 800; letter-spacing: 10px; color: #2e7d32;">%s</span>
        </div>
        <p style="color: #9e9e9e; font-size: 12px; margin-top: 12px;">This code is valid for 10 minutes</p>
      </div>
      <div style="background: #fff8e1; border-left: 4px solid #ffb300; padding: 14px 16px; border-radius: 6px;">
        <p style="margin: 0; color: #f57f17; font-size: 13px; font-weight: 700;">⚠ DO NOT SHARE THIS CODE</p>
        <p style="margin: 6px 0 0 0; color: #6d4c41; font-size: 13px; line-height: 1.5;">
          SmartGaon will never ask you for this code. Keep it confidential and do not forward this email to anyone.
        </p>
      </div>
      <p style="color: #9e9e9e; font-size: 12px; text-align: center; margin-top: 32px; border-top: 1px solid #eeeeee; padding-top: 20px; line-height: 1.6;">
        If you didn't request this change, you can safely ignore this email.<br>
        © 2026 SmartGaon AI. All rights reserved.
      </p>
    </div>
  </div>
</div>
""".formatted(logoUrl, otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("OTP " + otp + " sent to " + newEmail);
            return otp;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public OtpStatus verifyOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);
        if (data == null) return OtpStatus.NOT_FOUND;
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(email);
            return OtpStatus.EXPIRED;
        }
        if (!data.otp.equals(otp)) return OtpStatus.INVALID;
        otpStorage.remove(email);
        return OtpStatus.VALID;
    }
}