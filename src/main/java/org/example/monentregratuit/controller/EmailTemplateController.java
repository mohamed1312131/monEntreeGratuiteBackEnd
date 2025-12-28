package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.EmailTemplateDTO;
import org.example.monentregratuit.DTO.EmailTemplateImageDTO;
import org.example.monentregratuit.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/email-templates")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @PostMapping
    public ResponseEntity<EmailTemplateDTO> createTemplate(@RequestBody EmailTemplateDTO dto) {
        try {
            EmailTemplateDTO created = emailTemplateService.createTemplate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<EmailTemplateDTO>> getAllTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmailTemplateDTO> templates = emailTemplateService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmailTemplateDTO>> getActiveTemplates() {
        List<EmailTemplateDTO> templates = emailTemplateService.getActiveTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateDTO> getTemplateById(@PathVariable Long id) {
        try {
            EmailTemplateDTO template = emailTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplateDTO dto) {
        try {
            EmailTemplateDTO updated = emailTemplateService.updateTemplate(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTemplate(@PathVariable Long id) {
        try {
            emailTemplateService.deleteTemplate(id);
            return ResponseEntity.ok("Template désactivé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<EmailTemplateImageDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            EmailTemplateImageDTO image = emailTemplateService.uploadImage(id, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(image);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<EmailTemplateImageDTO>> getTemplateImages(@PathVariable Long id) {
        List<EmailTemplateImageDTO> images = emailTemplateService.getTemplateImages(id);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{templateId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable Long templateId,
            @PathVariable Long imageId) {
        try {
            emailTemplateService.deleteImage(templateId, imageId);
            return ResponseEntity.ok("Image supprimée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
