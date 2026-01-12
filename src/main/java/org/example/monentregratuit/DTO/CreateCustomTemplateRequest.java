package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomTemplateRequest {
    private String name;
    private String backgroundColor;
    private List<ImageOrderDTO> images;
}
