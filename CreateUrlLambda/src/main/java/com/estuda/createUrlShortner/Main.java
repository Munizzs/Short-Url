package com.estuda.createUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = (String) input.get("body");

        Map<String, String> bodyMap;
        try{
            bodyMap = objectMapper.readValue(body, Map.class);
        }catch (Exception e){
            throw new RuntimeException("Erro parsing JSON body: "+ e.getMessage(), e);
        }

        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        Long expirationTimeInSecond = Long.parseLong(expirationTime);

        String shortUrlCode = UUID.randomUUID().toString().substring(0,8);

        UrlDate urlDate = new UrlDate(originalUrl, expirationTimeInSecond);

        try{
            String urlDataJson = objectMapper.writeValueAsString(urlDate);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket("url-shortener-storage-estudo")
                    .key(shortUrlCode)
                    .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        }catch (Exception e){
            throw new RuntimeException("Erro saving URL data to s3: "+ e.getMessage(), e);
        }

        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }

}