package org.example.monentregratuit.controller;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.DTO.SliderDTO;
import org.example.monentregratuit.entity.Slider;
import org.example.monentregratuit.service.SliderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sliders")
@AllArgsConstructor
public class SliderController {
    private final SliderService sliderService;

    @GetMapping("/ordered")
    public List<SliderDTO> getSlidersWithOrder() {
        return sliderService.getAllSlidersWithOrder().stream()
                .map(SliderDTO::new)
                .collect(Collectors.toList());
    }
    @GetMapping
    public List<SliderDTO> getSliders() {
        return sliderService.getAllSliders().stream()
                .map(SliderDTO::new)
                .collect(Collectors.toList());
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateSlider(@PathVariable Long id, @RequestBody Slider slider) {
        try {
            Slider updated = sliderService.updateSlider(id, slider);
            return ResponseEntity.ok(new SliderDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update slider: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            Slider updated = sliderService.toggleActive(id);
            return ResponseEntity.ok(new SliderDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to toggle slider: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/update-with-image")
    public ResponseEntity<?> updateSliderWithImage(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Slider updated = sliderService.updateSliderWithImage(id, file);
            return ResponseEntity.ok(new SliderDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update slider: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSlider(@PathVariable Long id) {
        try {
            sliderService.deleteSlider(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete slider: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    public List<SliderDTO> getActiveSliders() {
        return sliderService.getActiveSliders().stream()
                .map(SliderDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSlider(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "foireId", required = false) Long foireId) {
        try {
            Slider slider = sliderService.createSlider(file, foireId);
            return ResponseEntity.ok(new SliderDTO(slider));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create slider: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/update-order")
    public ResponseEntity<?> updateSliderOrder(@PathVariable Long id, @RequestParam("order") Integer order) {
        try {
            Slider updated = sliderService.updateSliderOrder(id, order);
            return ResponseEntity.ok(new SliderDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update slider order: " + e.getMessage());
        }
    }

    @GetMapping("/foire/{foireId}")
    public List<SliderDTO> getSlidersByFoire(@PathVariable Long foireId) {
        return sliderService.getSlidersByFoire(foireId).stream()
                .map(SliderDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/foire/{foireId}/active")
    public List<SliderDTO> getActiveSlidersByFoire(@PathVariable Long foireId) {
        return sliderService.getActiveSlidersByFoire(foireId).stream()
                .map(SliderDTO::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/update-foire")
    public ResponseEntity<?> updateSliderFoire(@PathVariable Long id, @RequestParam(value = "foireId", required = false) Long foireId) {
        try {
            Slider updated = sliderService.updateSliderFoire(id, foireId);
            return ResponseEntity.ok(new SliderDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update slider foire: " + e.getMessage());
        }
    }
}
