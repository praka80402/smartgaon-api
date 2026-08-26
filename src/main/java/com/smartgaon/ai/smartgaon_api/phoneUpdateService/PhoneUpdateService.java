package com.smartgaon.ai.smartgaon_api.phoneUpdateService;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneUpdateService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        String otp;
        long expiryTime;

        OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public enum OtpStatus {
        VALID,
        INVALID,
        EXPIRED,
        NOT_FOUND
    }

    public String sendOtp(String phoneNumber) {
        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(1000 + random.nextInt(9000));
        otpStorage.put(
            phoneNumber,
            new OtpData(
                otp,
                System.currentTimeMillis() + 10 * 60 * 1000
            )
        );
        System.out.println("OTP " + otp + " generated for " + phoneNumber);

        return otp;
    }

    public OtpStatus verifyOtp(String phoneNumber, String otp) {

        OtpData data = otpStorage.get(phoneNumber);

        if (data == null) {
            return OtpStatus.NOT_FOUND;
        }

        if (System.currentTimeMillis() > data.expiryTime) {

            otpStorage.remove(phoneNumber);

            return OtpStatus.EXPIRED;
        }

        if (!data.otp.equals(otp)) {
            return OtpStatus.INVALID;
        }

        otpStorage.remove(phoneNumber);

        return OtpStatus.VALID;
    }
}