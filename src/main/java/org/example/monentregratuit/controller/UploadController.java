package org.example.monentregratuit.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@AllArgsConstructor
public class UploadController {

    private final Cloudinary cloudinary;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Upload with explicit parameters to prevent any compression or optimization
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "quality", "100",
                "flags", "no_override",
                "transformation", ObjectUtils.asMap("quality", "100")
            );
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            // Get the original URL and ensure it uses fl_lossy.false to prevent compression
            String baseUrl = uploadResult.get("secure_url").toString();
            
            // Construct URL with explicit quality parameters to prevent Cloudinary from optimizing
            String publicId = uploadResult.get("public_id").toString();
            String format = uploadResult.get("format").toString();
            String cloudName = cloudinary.config.cloudName;
            
            // Build URL with q_100 (quality 100) and fl_lossy.false flags
            String highQualityUrl = String.format("https://res.cloudinary.com/%s/image/upload/q_100,fl_lossy.false/%s.%s", 
                cloudName, publicId, format);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", highQualityUrl);
            response.put("public_id", publicId);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping("/video")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // Upload video to Cloudinary with resource_type video
            Map<String, Object> params = ObjectUtils.asMap(
                "resource_type", "video"
            );
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", uploadResult.get("secure_url").toString());
            response.put("public_id", uploadResult.get("public_id").toString());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload video: " + e.getMessage());
        }
    }
}
