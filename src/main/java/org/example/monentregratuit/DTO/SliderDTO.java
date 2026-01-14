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
    private Long foireId;
    private String foireName;

    public SliderDTO(Slider slider) {
        this.id = slider.getId();
        this.reference = slider.getReference();
        this.imageUrl = slider.getImageUrl();
        this.order = slider.getOrder();
        this.isActive = slider.getIsActive() != null ? slider.getIsActive() : false;
        if (slider.getFoire() != null) {
            this.foireId = slider.getFoire().getId();
            this.foireName = slider.getFoire().getName();
        }
    }
}