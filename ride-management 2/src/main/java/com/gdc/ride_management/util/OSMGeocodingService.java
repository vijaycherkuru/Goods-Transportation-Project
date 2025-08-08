package com.gdc.ride_management.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class OSMGeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public double[] getCoordinatesFromLocation(String location) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                    .queryParam("q", URLEncoder.encode(location, StandardCharsets.UTF_8))
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .build(true)
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            JSONArray results = new JSONArray(response);

            if (results.length() > 0) {
                JSONObject place = results.getJSONObject(0);
                double lat = Double.parseDouble(place.getString("lat"));
                double lon = Double.parseDouble(place.getString("lon"));
                return new double[]{lat, lon};
            } else {
                throw new RuntimeException("No results found for location: " + location);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch coordinates for location: " + location, e);
        }
    }
}
