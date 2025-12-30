package org.example.monentregratuit.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret.key:}")
    private String recaptchaSecretKey;

    @Value("${recaptcha.verify.url:https://www.google.com/recaptcha/api/siteverify}")
    private String recaptchaVerifyUrl;

    private static final double MINIMUM_SCORE = 0.5;

    public boolean verifyRecaptcha(String token) {
        if (recaptchaSecretKey == null || recaptchaSecretKey.isEmpty()) {
            // If reCAPTCHA is not configured, allow the request
            // In production, you should make this mandatory
            return true;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            Map<String, String> body = new HashMap<>();
            body.put("secret", recaptchaSecretKey);
            body.put("response", token);

            String response = restTemplate.postForObject(
                recaptchaVerifyUrl + "?secret={secret}&response={response}",
                null,
                String.class,
                recaptchaSecretKey,
                token
            );

            Gson gson = new Gson();
            Map<String, Object> responseMap = gson.fromJson(response, Map.class);

            Boolean success = (Boolean) responseMap.get("success");
            
            if (success != null && success) {
                // For reCAPTCHA v3, check the score
                Object scoreObj = responseMap.get("score");
                if (scoreObj != null) {
                    double score = ((Number) scoreObj).doubleValue();
                    return score >= MINIMUM_SCORE;
                }
                return true;
            }

            return false;
        } catch (Exception e) {
            // Log the error but don't block the request if reCAPTCHA service is down
            System.err.println("reCAPTCHA verification failed: " + e.getMessage());
            return true; // Fail open - in production you might want to fail closed
        }
    }
}
