package org.example.monentregratuit.service;

import org.example.monentregratuit.DTO.*;
import org.example.monentregratuit.entity.CustomTemplate;
import org.example.monentregratuit.entity.TemplateImage;
import org.example.monentregratuit.repo.CustomTemplateRepository;
import org.example.monentregratuit.repo.TemplateImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomTemplateService {

    private final CustomTemplateRepository customTemplateRepository;
    private final TemplateImageRepository templateImageRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public CustomTemplateService(CustomTemplateRepository customTemplateRepository,
                                TemplateImageRepository templateImageRepository) {
        this.customTemplateRepository = customTemplateRepository;
        this.templateImageRepository = templateImageRepository;
    }

    @Transactional
    public CustomTemplateDTO createTemplate(CreateCustomTemplateRequest request) {
        System.out.println("=== CustomTemplateService.createTemplate() START ===");
        System.out.println("Request name: " + request.getName());
        System.out.println("Request backgroundColor: " + request.getBackgroundColor());
        System.out.println("Request images count: " + (request.getImages() != null ? request.getImages().size() : 0));
        
        // Generate unique slug from name
        String slug = generateSlug(request.getName());
        System.out.println("Generated slug: " + slug);
        
        // Ensure slug is unique
        String uniqueSlug = ensureUniqueSlug(slug);
        System.out.println("Unique slug: " + uniqueSlug);

        // Create template with initialized images list
        CustomTemplate template = CustomTemplate.builder()
                .name(request.getName())
                .slug(uniqueSlug)
                .backgroundColor(request.getBackgroundColor())
                .active(true)
                .images(new java.util.ArrayList<>())
                .build();

        System.out.println("Saving template to database...");
        template = customTemplateRepository.save(template);
        System.out.println("Template saved with ID: " + template.getId());

        // Add images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            System.out.println("Adding " + request.getImages().size() + " images...");
            for (ImageOrderDTO imageDto : request.getImages()) {
                System.out.println("Adding image - URL: " + imageDto.getImageUrl() + ", Order: " + imageDto.getDisplayOrder());
                TemplateImage image = TemplateImage.builder()
                        .template(template)
                        .imageUrl(imageDto.getImageUrl())
                        .displayOrder(imageDto.getDisplayOrder())
                        .altText(imageDto.getAltText())
                        .build();
                templateImageRepository.save(image);
                System.out.println("Image saved with ID: " + image.getId());
            }
            System.out.println("All images saved successfully");
        }

        // Reload to get images
        System.out.println("Reloading template with images...");
        template = customTemplateRepository.findById(template.getId())
                .orElseThrow(() -> new RuntimeException("Template not found"));
        System.out.println("Template reloaded with " + template.getImages().size() + " images");

        System.out.println("Converting to DTO...");
        CustomTemplateDTO dto = convertToDTO(template);
        System.out.println("=== CustomTemplateService.createTemplate() SUCCESS ===");
        return dto;
    }

    public List<CustomTemplateDTO> getAllTemplates() {
        System.out.println("=== CustomTemplateService.getAllTemplates() START ===");
        try {
            System.out.println("Calling customTemplateRepository.findAll()...");
            List<CustomTemplate> templates = customTemplateRepository.findAll();
            System.out.println("Found " + templates.size() + " templates in database");
            
            System.out.println("Converting to DTOs...");
            List<CustomTemplateDTO> dtos = templates.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            System.out.println("Converted " + dtos.size() + " templates to DTOs");
            System.out.println("=== CustomTemplateService.getAllTemplates() SUCCESS ===");
            return dtos;
        } catch (Exception e) {
            System.err.println("=== CustomTemplateService.getAllTemplates() ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public CustomTemplateDTO getTemplateById(Long id) {
        CustomTemplate template = customTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + id));
        return convertToDTO(template);
    }

    public CustomTemplateDTO getTemplateBySlug(String slug) {
        CustomTemplate template = customTemplateRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Template not found with slug: " + slug));
        return convertToDTO(template);
    }

    @Transactional
    public CustomTemplateDTO updateTemplate(Long id, CreateCustomTemplateRequest request) {
        CustomTemplate template = customTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + id));

        template.setName(request.getName());
        template.setBackgroundColor(request.getBackgroundColor());

        // Update slug if name changed
        String newSlug = generateSlug(request.getName());
        if (!template.getSlug().equals(newSlug)) {
            template.setSlug(ensureUniqueSlug(newSlug, id));
        }

        // Remove old images
        template.getImages().clear();
        templateImageRepository.flush();

        // Add new images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (ImageOrderDTO imageDto : request.getImages()) {
                TemplateImage image = TemplateImage.builder()
                        .template(template)
                        .imageUrl(imageDto.getImageUrl())
                        .displayOrder(imageDto.getDisplayOrder())
                        .altText(imageDto.getAltText())
                        .build();
                template.getImages().add(image);
            }
        }

        template = customTemplateRepository.save(template);
        return convertToDTO(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!customTemplateRepository.existsById(id)) {
            throw new RuntimeException("Template not found with ID: " + id);
        }
        customTemplateRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    private String ensureUniqueSlug(String slug) {
        return ensureUniqueSlug(slug, null);
    }

    private String ensureUniqueSlug(String slug, Long excludeId) {
        String uniqueSlug = slug;
        int counter = 1;

        while (true) {
            String finalSlug = uniqueSlug;
            boolean exists = customTemplateRepository.findBySlug(finalSlug)
                    .filter(t -> excludeId == null || !t.getId().equals(excludeId))
                    .isPresent();

            if (!exists) {
                break;
            }

            uniqueSlug = slug + "-" + counter;
            counter++;
        }

        return uniqueSlug;
    }

    private CustomTemplateDTO convertToDTO(CustomTemplate template) {
        List<TemplateImageDTO> imageDTOs = template.getImages().stream()
                .map(img -> TemplateImageDTO.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .altText(img.getAltText())
                        .build())
                .collect(Collectors.toList());

        String publicUrl = frontendUrl + "/" + template.getSlug();

        return CustomTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .slug(template.getSlug())
                .backgroundColor(template.getBackgroundColor())
                .images(imageDTOs)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .active(template.isActive())
                .publicUrl(publicUrl)
                .build();
    }
}
