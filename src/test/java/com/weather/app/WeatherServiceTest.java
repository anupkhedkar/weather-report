package com.weather.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.repo.WeatherRepository;
import com.weather.app.repo.entities.WeatherEntity;
import com.weather.app.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetWeather_CacheHit() {
        String city = "London";
        String country = "uk";
        String apiKey = "test-api-key";
        String description = "Clear sky";

        WeatherEntity mockWeatherEntity = new WeatherEntity();
        mockWeatherEntity.setCity(city);
        mockWeatherEntity.setCountry(country);
        mockWeatherEntity.setDescription(description);

        Mockito.when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Optional.of(mockWeatherEntity));

        String result = weatherService.getWeather(city, country, apiKey);

        assertEquals(description, result);
        Mockito.verify(weatherRepository, Mockito.times(1)).findByCityAndCountry(city, country);
        Mockito.verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    public void testGetWeather_CacheMiss() throws Exception {
        String city = "Sydney";
        String country = "Australia";
        String apiKey = "78a036562f78f67955fa56f6a12442ff";
        String description = "Rainy";

        String jsonResponse = "{\"weather\":[{\"description\":\"Rainy\"}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        Mockito.when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Optional.empty());
        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        String result = weatherService.getWeather(city, country, apiKey);

        assertEquals(description, result);
        Mockito.verify(weatherRepository, Mockito.times(1)).findByCityAndCountry(city, country);
        Mockito.verify(weatherRepository, Mockito.times(1)).save(Mockito.any(WeatherEntity.class));
    }

    @Test
    public void testFetchWeatherFromApi_Success() throws Exception {
        String city = "Sydney";
        String country = "Australia";
        String apiKey = "78a036562f78f67955fa56f6a12442ff";
        String description = "Sunny";

        String jsonResponse = "{\"weather\":[{\"description\":\"Sunny\"}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        String result = weatherService.getWeather(city, country, apiKey);

        assertEquals(description, result);
    }

    @Test
    public void testFetchWeatherFromApi_Failure() {
        String city = "InvalidCity";
        String country = "InvalidCountry";
        String apiKey = "test-api-key";

        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class)))
                .thenThrow(new RuntimeException("Error occurred while calling OpenWeatherMap API"));

        assertThrows(RuntimeException.class, () -> {
            weatherService.getWeather(city, country, apiKey);
        });
    }
}
