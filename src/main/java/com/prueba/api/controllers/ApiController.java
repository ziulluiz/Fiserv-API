package com.prueba.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prueba.api.models.Data;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class ApiController {
    @Autowired
    private HttpServletRequest request;
    private Data storedData;
    @PostMapping("/data/payments")
    public ResponseEntity <String> save(@Validated @RequestBody Data data){
        storedData = data;
        //set UUID
        UUID clientRequestId = UUID.randomUUID();
        data.setUuid(clientRequestId.toString());

        //set timestamp
        long timestamp = System.currentTimeMillis();
        data.setTimestamp(String.valueOf(timestamp));

        //get apikey ang apisecret
        String apiKey = "hsbtDhDJc6o9NLxwZ2c5ir4dXUaqoQju";
        data.setApiKey(apiKey);

        String apiSecret = "RS5raqlrHZmp8f0yWgb1S4AEfga9egyR1veFgYc2aUq";

        //payload
        String staticPayload = "{ \"transactionAmount\": { \"total\": \"5.00\", \"currency\": \"MXN\" }, \"requestType\": \"PaymentCardSaleTransaction\", \"paymentMethod\": { \"paymentCard\": { \"number\": \"4931580001642617\", \"securityCode\": \"123\", \"expiryDate\": { \"month\": \"12\", \"year\": \"25\" } } } }";

        //build msgSignatureString
        String msgSignatureString = apiKey + clientRequestId + timestamp + staticPayload;

        //build message signature with Hmac SHA256
        String msgSignature = calculateHmacSHA256(msgSignatureString, apiSecret);
        data.setMsgSignature(msgSignature);

        //setting headers
        //HttpHeaders headers = construirHeaders(storedData);

        String cuerpoSolicitud = construirCuerpoSolicitud(storedData);

        // Configurar la solicitud POST
        //HttpEntity<String> httpEntity = new HttpEntity<>(cuerpoSolicitud, headers);

        // Realizar la solicitud POST a la API externa
        RestTemplate restTemplate = new RestTemplate();
        //ResponseEntity<String> respuestaAPI = restTemplate.postForEntity("https://webhook.site/ad1c2e20-1c94-4c91-8981-ef2423eb4787", httpEntity, String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Convert Data object to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al convertir los datos a JSON");
        }

        // Create the request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(cuerpoSolicitud, headers);

        // Make the POST request
        //ResponseEntity<String> response = restTemplate.exchange("https://cert.api.firstdata.com/gateway/v2", HttpMethod.POST, requestEntity, String.class);

        // Return response
        //return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        //System.out.println("Total: " + data.getMsgSignature());

        //return ResponseEntity.ok().body(respuestaAPI.getBody());
        return ResponseEntity.status(HttpStatus.OK).body("datos recibidos");
    }

    @GetMapping("/requests")
    public ResponseEntity<Data> getData(){
        if(storedData != null){
            return ResponseEntity.ok(storedData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private String calculateHmacSHA256(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hmacData = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Manejar errores de algoritmo o clave
            e.printStackTrace();
            return null;
        }
    }

    private HttpHeaders construirHeaders(Data data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Client-Request-Id", data.getUuid());
        headers.set("Api-Key", data.getApiKey());
        headers.set("Timestamp", data.getTimestamp());
        headers.set("Message-Signature", data.getMsgSignature());
        return headers;
    }

    private String construirCuerpoSolicitud(Data data) {
        // Construir el cuerpo de la solicitud según la estructura proporcionada
        return "{\n" +
                "    \"transactionAmount\": {\n" +
                "        \"total\": \"" + data.getTotal() + "\",\n" +
                "        \"currency\": \"MXN\"\n" +
                "    },\n" +
                "    \"requestType\": \"PaymentCardSaleTransaction\",\n" +
                "    \"paymentMethod\": {\n" +
                "        \"paymentCard\": {\n" +
                "            \"number\": \"" + data.getNumber() + "\",\n" +
                "            \"securityCode\": \"" + data.getSecurityCode() + "\",\n" +
                "            \"expiryDate\": {\n" +
                "                \"month\": \"" + data.getMonth() + "\",\n" +
                "                \"year\": \"" + data.getYear() + "\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}
