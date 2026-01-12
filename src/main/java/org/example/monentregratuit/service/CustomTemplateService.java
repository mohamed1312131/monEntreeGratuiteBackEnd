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
        // Generate unique slug from name
        String slug = generateSlug(request.getName());
        
        // Ensure slug is unique
        String uniqueSlug = ensureUniqueSlug(slug);

        // Create template
        CustomTemplate template = CustomTemplate.builder()
                .name(request.getName())
                .slug(uniqueSlug)
                .backgroundColor(request.getBackgroundColor())
                .active(true)
                .build();

        template = customTemplateRepository.save(template);

        // Add images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (ImageOrderDTO imageDto : request.getImages()) {
                TemplateImage image = TemplateImage.builder()
                        .template(template)
                        .imageUrl(imageDto.getImageUrl())
                        .displayOrder(imageDto.getDisplayOrder())
                        .altText(imageDto.getAltText())
                        .build();
                templateImageRepository.save(image);
            }
        }

        // Reload to get images
        template = customTemplateRepository.findById(template.getId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        return convertToDTO(template);
    }

    public List<CustomTemplateDTO> getAllTemplates() {
        return customTemplateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
