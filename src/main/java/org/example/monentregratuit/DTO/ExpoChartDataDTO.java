package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpoChartDataDTO {
    private List<String> countries;
    private List<SeriesData> series;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesData {
        private String name;
        private List<Integer> data;
    }
}
