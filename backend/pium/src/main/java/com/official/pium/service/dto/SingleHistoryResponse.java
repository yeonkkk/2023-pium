package com.official.pium.service.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SingleHistoryResponse {

    private String type;
    private LocalDate date;
    private Content content;

    @Getter
    @NoArgsConstructor
    public static class Content {

        private String previous;
        private String current;

        @Builder
        private Content(String previous, String current) {
            this.previous = previous;
            this.current = current;
        }
    }
}
