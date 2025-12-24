package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailPreviewRequest {
    private Long templateId;
    private Long invitationId;
}
