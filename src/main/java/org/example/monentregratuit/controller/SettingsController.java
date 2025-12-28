package org.example.monentregratuit.controller;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.AboutUs;
import org.example.monentregratuit.entity.AboutUsQA;
import org.example.monentregratuit.entity.SocialLinks;
import org.example.monentregratuit.entity.Video;
import org.example.monentregratuit.service.SettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@AllArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // About Us endpoints
    @GetMapping("/about-us")
    public List<AboutUs> getAllAboutUs() {
        return settingsService.getAllAboutUs();
    }

    @GetMapping("/about-us/active")
    public List<AboutUs> getActiveAboutUs() {
        return settingsService.getActiveAboutUs();
    }

    @GetMapping("/about-us/{id}")
    public ResponseEntity<AboutUs> getAboutUsById(@PathVariable Long id) {
        try {
            AboutUs aboutUs = settingsService.getAboutUsById(id);
            return ResponseEntity.ok(aboutUs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/about-us")
    public ResponseEntity<AboutUs> createAboutUs(@RequestBody AboutUs aboutUs) {
        AboutUs created = settingsService.createAboutUs(aboutUs);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/about-us/{id}")
    public ResponseEntity<AboutUs> updateAboutUs(@PathVariable Long id, @RequestBody AboutUs aboutUs) {
        try {
            AboutUs updated = settingsService.updateAboutUs(id, aboutUs);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/about-us/{id}")
    public ResponseEntity<Void> deleteAboutUs(@PathVariable Long id) {
        settingsService.deleteAboutUs(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/about-us/{id}/toggle")
    public ResponseEntity<AboutUs> toggleAboutUsActive(@PathVariable Long id) {
        try {
            AboutUs updated = settingsService.toggleAboutUsActive(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Video endpoints
    @GetMapping("/videos")
    public List<Video> getAllVideos() {
        return settingsService.getAllVideos();
    }

    @GetMapping("/videos/active")
    public List<Video> getActiveVideos() {
        return settingsService.getActiveVideos();
    }

    @GetMapping("/videos/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        try {
            Video video = settingsService.getVideoById(id);
            return ResponseEntity.ok(video);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/videos")
    public ResponseEntity<Video> createVideo(@RequestBody Video video) {
        Video created = settingsService.createVideo(video);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/videos/{id}")
    public ResponseEntity<Video> updateVideo(@PathVariable Long id, @RequestBody Video video) {
        try {
            Video updated = settingsService.updateVideo(id, video);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        settingsService.deleteVideo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/videos/{id}/toggle")
    public ResponseEntity<Video> toggleVideoActive(@PathVariable Long id) {
        try {
            Video updated = settingsService.toggleVideoActive(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Social Links endpoints
    @GetMapping("/social-links")
    public ResponseEntity<SocialLinks> getSocialLinks() {
        SocialLinks socialLinks = settingsService.getSocialLinks();
        return ResponseEntity.ok(socialLinks);
    }

    @PutMapping("/social-links")
    public ResponseEntity<SocialLinks> updateSocialLinks(@RequestBody SocialLinks socialLinks) {
        SocialLinks updated = settingsService.updateSocialLinks(socialLinks);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/about-us/{aboutUsId}/qa")
    public List<AboutUsQA> getQAByAboutUsId(@PathVariable Long aboutUsId) {
        return settingsService.getQAByAboutUsId(aboutUsId);
    }

    @PostMapping("/about-us/{aboutUsId}/qa")
    public ResponseEntity<AboutUsQA> createQA(@PathVariable Long aboutUsId, @RequestBody AboutUsQA qa) {
        AboutUsQA created = settingsService.createQA(aboutUsId, qa);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/qa/{qaId}")
    public ResponseEntity<AboutUsQA> updateQA(@PathVariable Long qaId, @RequestBody AboutUsQA qa) {
        try {
            AboutUsQA updated = settingsService.updateQA(qaId, qa);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/qa/{qaId}")
    public ResponseEntity<Void> deleteQA(@PathVariable Long qaId) {
        settingsService.deleteQA(qaId);
        return ResponseEntity.ok().build();
    }
}
