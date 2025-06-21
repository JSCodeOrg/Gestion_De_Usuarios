package com.JSCode.GestionUsuarios.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.JSCode.GestionUsuarios.dto.users.DeliveryEntregasData;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class RepartidorClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendDeliveryInfo(DeliveryEntregasData deliveryData, String authToken) {
        String entregasUrl = "http://api-gateway:8080/entregas/repartidor";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<DeliveryEntregasData> request = new HttpEntity<>(deliveryData, headers);

        restTemplate.postForEntity(entregasUrl, request, Void.class);
    }
}