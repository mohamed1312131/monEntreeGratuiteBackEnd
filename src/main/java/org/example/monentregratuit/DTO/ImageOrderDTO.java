package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageOrderDTO {
    private String imageUrl;
    private Integer displayOrder;
    private String altText;
}
