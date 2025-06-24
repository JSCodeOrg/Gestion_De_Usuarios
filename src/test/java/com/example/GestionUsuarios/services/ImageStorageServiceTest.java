package com.example.GestionUsuarios.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.JSCode.GestionUsuarios.service.ImageStorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageStorageServiceTest {

    private RestTemplate restTemplate;
    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        imageStorageService = new ImageStorageService(restTemplate);

        // Usar reflexión para setear los valores de las propiedades @Value
        setPrivateField(imageStorageService, "storageApiBase", "https://api.imgbb.com/1/upload");
        setPrivateField(imageStorageService, "apikey", "fake-api-key");
    }

    @Test
    void uploadImageToImgBB_returnsUrl_whenSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", "fake-image-data".getBytes());

        // Simular respuesta del API
        Map<String, Object> data = new HashMap<>();
        data.put("url", "https://imgbb.com/test-image.png");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("data", data);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(responseEntity);

        // Act
        String resultUrl = imageStorageService.uploadImageToImgBB(file);

        // Assert
        assertNotNull(resultUrl);
        assertEquals("https://imgbb.com/test-image.png", resultUrl);

        // Capturar request y validar base64 fue enviado
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));

        MultiValueMap<String, String> body = (MultiValueMap<String, String>) captor.getValue().getBody();
        assertNotNull(body.getFirst("image"));
    }

    @Test
    void uploadImageToImgBB_returnsNull_whenApiFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", "fake-image-data".getBytes());

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(responseEntity);

        String result = imageStorageService.uploadImageToImgBB(file);
        assertNull(result);
    }

    @Test
    void uploadImageToImgBB_returnsNull_whenExceptionThrown() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("I/O error")); // ✅ Corregido aquí

        String result = imageStorageService.uploadImageToImgBB(file);
        assertNull(result);
    }

    // Helper para setear campos privados con @Value
    private void setPrivateField(Object target, String fieldName, String value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
