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
        try {
            CustomTemplateDTO template = customTemplateService.createTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(template);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CustomTemplateDTO>> getAllTemplates() {
        List<CustomTemplateDTO> templates = customTemplateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomTemplateDTO> getTemplateById(@PathVariable Long id) {
        try {
            CustomTemplateDTO template = customTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CustomTemplateDTO> getTemplateBySlug(@PathVariable String slug) {
        try {
            CustomTemplateDTO template = customTemplateService.getTemplateBySlug(slug);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody CreateCustomTemplateRequest request) {
        try {
            CustomTemplateDTO template = customTemplateService.updateTemplate(id, request);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            customTemplateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
