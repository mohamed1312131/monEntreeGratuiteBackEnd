package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomTemplateDTO {
    private Long id;
    private String name;
    private String slug;
    private String backgroundColor;
    private List<TemplateImageDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private String publicUrl;
}
