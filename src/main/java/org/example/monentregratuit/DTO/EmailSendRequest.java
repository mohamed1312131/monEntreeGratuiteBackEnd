package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendRequest {
    private Long templateId;
    private List<Long> invitationIds;
}
