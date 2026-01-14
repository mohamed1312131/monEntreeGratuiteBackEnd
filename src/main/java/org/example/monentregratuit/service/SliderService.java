package org.example.monentregratuit.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.entity.Image;
import org.example.monentregratuit.entity.Slider;
import org.example.monentregratuit.repo.FoireRepository;
import org.example.monentregratuit.repo.SliderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class SliderService {
    private final SliderRepository sliderRepository;
    private final FoireRepository   foireRepository;
    @Autowired
    private Cloudinary cloudinary;

    public List<Slider> getAllSliders() {
        return sliderRepository.findAll();
    }

    public Slider updateSlider(Long id, Slider updatedSlider) {
        return sliderRepository.findById(id)
                .map(slider -> {
                    if (updatedSlider.getImageUrl() != null) {
                        slider.setImageUrl(updatedSlider.getImageUrl());
                    }
                    if (updatedSlider.getReference() != null) {
                        slider.setReference(updatedSlider.getReference());
                    }
                    if (updatedSlider.getOrder() != null) {
                        slider.setOrder(updatedSlider.getOrder());
                    }
                    if (updatedSlider.getIsActive() != null) {
                        slider.setIsActive(updatedSlider.getIsActive());
                    }
                    if (updatedSlider.getFoire() != null) {
                        slider.setFoire(updatedSlider.getFoire());
                    }
                    return sliderRepository.save(slider);
                })
                .orElseThrow(() -> new RuntimeException("Slider not found"));
    }

    public Slider toggleActive(Long id) {
        return sliderRepository.findById(id)
                .map(slider -> {
                    slider.setIsActive(!slider.getIsActive());
                    return sliderRepository.save(slider);
                })
                .orElseThrow(() -> new RuntimeException("Slider not found"));
    }

    public Slider updateSliderWithImage(Long id, MultipartFile file) throws IOException {
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider not found"));

        if (file != null && !file.isEmpty()) {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();
            slider.setImageUrl(imageUrl);
        }

        return sliderRepository.save(slider);
    }

    public void deleteSlider(Long id) {
        sliderRepository.deleteById(id);
    }

    public Slider createSlider(MultipartFile file, Long foireId) throws IOException {
        // Upload the image to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("secure_url").toString();

        String reference = "IMG" + new Random().nextInt(10000000);
        
        // Get the next order number
        int order = sliderRepository.findAll().size() + 1;
        
        // Get the foire if foireId is provided
        Foire foire = null;
        if (foireId != null) {
            foire = foireRepository.findById(foireId)
                    .orElseThrow(() -> new RuntimeException("Foire not found with id: " + foireId));
        }
        
        // Create and save the Slider entity
        Slider slider = Slider.builder()
                .imageUrl(imageUrl)
                .reference(reference)
                .order(order)
                .isActive(false)
                .foire(foire)
                .build();

        return sliderRepository.save(slider);
    }

    public List<Slider> getAllSlidersWithOrder() {
        return sliderRepository.findAll().stream()
                .filter(slider -> slider.getOrder() != null)
                .sorted(Comparator.comparingInt(Slider::getOrder))
                .collect(Collectors.toList());
    }

    public List<Slider> getActiveSliders() {
        return sliderRepository.findAll().stream()
                .filter(slider -> slider.getIsActive() != null && slider.getIsActive())
                .sorted(Comparator.comparingInt(slider -> slider.getOrder() != null ? slider.getOrder() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    public Slider updateSliderOrder(Long id, Integer order) {
        return sliderRepository.findById(id)
                .map(slider -> {
                    slider.setOrder(order);
                    return sliderRepository.save(slider);
                })
                .orElseThrow(() -> new RuntimeException("Slider not found"));
    }

    public List<Slider> getSlidersByFoire(Long foireId) {
        return sliderRepository.findByFoireId(foireId).stream()
                .sorted(Comparator.comparingInt(slider -> slider.getOrder() != null ? slider.getOrder() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    public List<Slider> getActiveSlidersByFoire(Long foireId) {
        return sliderRepository.findByFoireIdAndIsActive(foireId, true).stream()
                .sorted(Comparator.comparingInt(slider -> slider.getOrder() != null ? slider.getOrder() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    public Slider updateSliderFoire(Long id, Long foireId) {
        return sliderRepository.findById(id)
                .map(slider -> {
                    if (foireId != null) {
                        Foire foire = foireRepository.findById(foireId)
                                .orElseThrow(() -> new RuntimeException("Foire not found with id: " + foireId));
                        slider.setFoire(foire);
                    } else {
                        slider.setFoire(null);
                    }
                    return sliderRepository.save(slider);
                })
                .orElseThrow(() -> new RuntimeException("Slider not found"));
    }
}