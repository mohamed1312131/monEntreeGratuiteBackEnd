package org.example.monentregratuit.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.monentregratuit.entity.Foire;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FoireDTO {
    private Long id;
    private String name;
    private String url;
    private String image;
    private String description;
    private String location;
    private List<Foire.DateRange> dateRanges;
    private List<Foire.DayTimeSlot> dayTimeSlots;

    @Deprecated
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime date;

    private Boolean isActive;

    private Boolean disponible;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime updatedAt;
}