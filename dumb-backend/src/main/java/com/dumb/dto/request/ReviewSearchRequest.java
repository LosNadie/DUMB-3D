package com.dumb.dto.request;

import lombok.Data;

@Data
public class ReviewSearchRequest {
    private String keyword;
    private String genre;
    private Double minScore;
    private Double maxScore;
}
