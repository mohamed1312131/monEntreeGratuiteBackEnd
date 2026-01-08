package org.example.monentregratuit.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailLogUserDTO {
    private Long id;
    private String recipientEmail;
    private String recipientName;
    private String status;
    private boolean opened;
    private LocalDateTime openedAt;
    private boolean clicked;
    private LocalDateTime clickedAt;
    private int clickCount;
    private String errorMessage;
}
