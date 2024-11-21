package com.estuda.CreateUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        String pathParameters = input.get("rawPath").toString();
        String shortUrlCode = pathParameters.replace("/","");

        if(shortUrlCode == null || shortUrlCode.isEmpty()){
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-estudo")
                .key(shortUrlCode+".json")
                .build();

        InputStream s3ObjectStream;

        try{
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        }catch (Exception e){
            throw new RuntimeException("Erro fetching URL data from s3: "+ e.getMessage(), e);
        }

        UrlData urlDate;

        try{
            urlDate = objectMapper.readValue(s3ObjectStream, UrlData.class);
        }catch (Exception e){
            throw new RuntimeException("Erro deserializing URL data: "+ e.getMessage(), e);
        }

        long currentTimeInSecond = System.currentTimeMillis() / 1000;

        Map<String, Object> response = new HashMap<>();

        if( urlDate.getExpirationTime() < currentTimeInSecond){
            response.put("statusCode",410);
            response.put("body", "This URL has expired");

            return response;
        }

        response.put("statusCode",302);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlDate.getOriginalUrl());
        response.put("headers", headers);

        return response;
    }
}
