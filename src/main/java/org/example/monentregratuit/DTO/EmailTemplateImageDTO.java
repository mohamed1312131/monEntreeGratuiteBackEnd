package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateImageDTO {
    private Long id;
    private Long templateId;
    private String imageUrl;
    private String imageName;
    private Integer imageOrder;
    private LocalDateTime createdAt;
}
