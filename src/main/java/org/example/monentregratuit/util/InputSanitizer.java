package org.example.monentregratuit.util;

import java.util.regex.Pattern;

public class InputSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s.-]{8,20}$");
    
    // Common disposable email domains
    private static final String[] DISPOSABLE_EMAIL_DOMAINS = {
        "tempmail.com", "throwaway.email", "guerrillamail.com", "mailinator.com",
        "10minutemail.com", "trashmail.com", "temp-mail.org", "yopmail.com",
        "maildrop.cc", "getnada.com", "fakeinbox.com", "sharklasers.com"
    };

    public static String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove script tags first
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Remove HTML tags
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        
        // Trim and limit length
        sanitized = sanitized.trim();
        
        return sanitized;
    }

    public static String sanitizeName(String name) {
        if (name == null) {
            return null;
        }
        
        String sanitized = sanitizeText(name);
        
        // Limit length to 100 characters
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        return sanitized;
    }

    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        String sanitized = email.trim().toLowerCase();
        
        // Remove any HTML or script tags
        sanitized = sanitizeText(sanitized);
        
        return sanitized;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isDisposableEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        
        for (String disposableDomain : DISPOSABLE_EMAIL_DOMAINS) {
            if (domain.equals(disposableDomain)) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // Phone is optional in most cases
        }
        
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static String sanitizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        
        String sanitized = sanitizeText(phone);
        
        // Limit length
        if (sanitized.length() > 20) {
            sanitized = sanitized.substring(0, 20);
        }
        
        return sanitized;
    }

    public static String sanitizeMessage(String message) {
        if (message == null) {
            return null;
        }
        
        String sanitized = sanitizeText(message);
        
        // Limit length to 2000 characters
        if (sanitized.length() > 2000) {
            sanitized = sanitized.substring(0, 2000);
        }
        
        return sanitized;
    }

    public static boolean containsSuspiciousContent(String text) {
        if (text == null) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        // Check for common spam indicators
        String[] spamKeywords = {
            "viagra", "cialis", "casino", "lottery", "winner", "congratulations",
            "click here", "buy now", "limited time", "act now", "free money",
            "nigerian prince", "inheritance", "million dollars"
        };
        
        for (String keyword : spamKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        
        // Check for excessive URLs
        int urlCount = text.split("http").length - 1;
        if (urlCount > 3) {
            return true;
        }
        
        return false;
    }
}
