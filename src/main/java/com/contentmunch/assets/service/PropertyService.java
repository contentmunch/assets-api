package com.contentmunch.assets.service;

import org.springframework.stereotype.Service;

@Service
public class PropertyService {
    public String propertyIdToDomain(String propertyId) {
        return switch (propertyId) {
            case "verona-park" -> "https://verona-park.herokuapp.com/";
            case "cov-affordable", "covenanter-hill" -> "https://covenanter-hill.herokuapp.com/";
            case "high-grove", "meadow-creek" -> "https://high-grove.herokuapp.com/";
            case "scholars-quad", "huntington-gardens" -> "https://scholars-quad.herokuapp.com/";
            case "scholars-rock", "scholars-rooftop" -> "https://scholars-rooftop.herokuapp.com/";
            case "sh-garages", "summer-house" -> "https://summer-house.herokuapp.com/";
            case "localhost:8080" -> "http://localhost:8080/";
            case "localhost:8082" -> "http://localhost:8082/";
            default -> "https://renaissancerentals.herokuapp.com/";
        };
    }
}
