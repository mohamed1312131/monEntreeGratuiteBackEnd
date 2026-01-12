package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.CreateCustomTemplateRequest;
import org.example.monentregratuit.DTO.CustomTemplateDTO;
import org.example.monentregratuit.service.CustomTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/custom-templates")
@CrossOrigin(origins = "*")
public class CustomTemplateController {

    private final CustomTemplateService customTemplateService;

    public CustomTemplateController(CustomTemplateService customTemplateService) {
        this.customTemplateService = customTemplateService;
    }

    @PostMapping
    public ResponseEntity<CustomTemplateDTO> createTemplate(@RequestBody CreateCustomTemplateRequest request) {
        System.out.println("=== CustomTemplateController.createTemplate() START ===");
        System.out.println("Request: " + request);
        try {
            CustomTemplateDTO template = customTemplateService.createTemplate(request);
            System.out.println("Template created successfully: " + template.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(template);
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.createTemplate() ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CustomTemplateDTO>> getAllTemplates() {
        System.out.println("=== CustomTemplateController.getAllTemplates() START ===");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        try {
            System.out.println("Calling customTemplateService.getAllTemplates()...");
            List<CustomTemplateDTO> templates = customTemplateService.getAllTemplates();
            System.out.println("Templates retrieved successfully. Count: " + templates.size());
            System.out.println("=== CustomTemplateController.getAllTemplates() SUCCESS ===");
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.getAllTemplates() ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomTemplateDTO> getTemplateById(@PathVariable Long id) {
        System.out.println("=== CustomTemplateController.getTemplateById() START ===");
        System.out.println("Template ID: " + id);
        try {
            CustomTemplateDTO template = customTemplateService.getTemplateById(id);
            System.out.println("Template found: " + template.getName());
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.getTemplateById() ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CustomTemplateDTO> getTemplateBySlug(@PathVariable String slug) {
        System.out.println("=== CustomTemplateController.getTemplateBySlug() START ===");
        System.out.println("Slug: " + slug);
        try {
            CustomTemplateDTO template = customTemplateService.getTemplateBySlug(slug);
            System.out.println("Template found: " + template.getName());
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.getTemplateBySlug() ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody CreateCustomTemplateRequest request) {
        System.out.println("=== CustomTemplateController.updateTemplate() START ===");
        System.out.println("Template ID: " + id);
        System.out.println("Request: " + request);
        try {
            CustomTemplateDTO template = customTemplateService.updateTemplate(id, request);
            System.out.println("Template updated successfully");
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.updateTemplate() ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        System.out.println("=== CustomTemplateController.deleteTemplate() START ===");
        System.out.println("Template ID: " + id);
        try {
            customTemplateService.deleteTemplate(id);
            System.out.println("Template deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("=== CustomTemplateController.deleteTemplate() ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
