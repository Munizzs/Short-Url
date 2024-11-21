package com.estuda.createUrlShortner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UrlDate {
    private String originalUrl;
    private Long expirationTime;

    public UrlDate(String originalUrl, Long expirationTime) {
        this.originalUrl = originalUrl;
        this.expirationTime = expirationTime;
    }
}
