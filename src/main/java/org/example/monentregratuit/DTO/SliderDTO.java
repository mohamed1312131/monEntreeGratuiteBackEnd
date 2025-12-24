package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.Slider;

@Data
public class SliderDTO {
    private Long id;
    private String reference;
    private String imageUrl;
    private Integer order;
    private Boolean isActive;

    public SliderDTO(Slider slider) {
        this.id = slider.getId();
        this.reference = slider.getReference();
        this.imageUrl = slider.getImageUrl();
        this.order = slider.getOrder();
        this.isActive = slider.getIsActive() != null ? slider.getIsActive() : false;
    }
}