package com.formos.huub.service.resttemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.framework.base.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RestTemplateService extends BaseService {
    private final RestTemplate restTemplate;

    /**
     * use to send Post Request
     *
     * @param url String
     * @param requestBody Object
     * @param headers HttpHeaders
     * @param responseType Class<T>
     * @param <T> <T>
     * @return ResponseEntity<T>
     */
    public <T> ResponseEntity<T> sendPostRequest(String url, Object requestBody, HttpHeaders headers, Class<T> responseType) {
        String json = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        } catch (HttpClientErrorException e) {
            log.error("Error when send post request {}", e.getMessage());
            log.error("URL: {}", url);
            log.error("RequestBody: {}", json);
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * use to send Get Request
     *
     * @param url          String
     * @param headers      HttpHeaders
     * @param responseType Class<T>
     * @param <T>          <T>
     * @return ResponseEntity<T>
     */
    public <T> ResponseEntity<T> sendGetRequest(String url, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        } catch (HttpClientErrorException e) {
            log.error("Error when send get request {}", e.getMessage());
            log.error("URL: {}", url);
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * use to send Put Request
     *
     * @param url          String
     * @param requestBody  Object
     * @param headers      HttpHeaders
     * @param responseType Class<T>
     * @param <T>          <T>
     * @return ResponseEntity<T>
     */
    public <T> ResponseEntity<T> sendPutRequest(String url, Object requestBody, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        try {
            return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
        } catch (HttpClientErrorException e) {
            log.error("Error when send put request {}", e.getMessage());
            log.error("URL: {}", url);
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    /**
     * use to send Delete Request
     *
     * @param url          String
     * @param headers      HttpHeaders
     * @param responseType Class<T>
     * @param <T>
     * @return ResponseEntity<T>
     */
    public <T> ResponseEntity<T> sendDeleteRequest(String url, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
        } catch (HttpClientErrorException e) {
            log.error("Error when send delete request {}", e.getMessage());
            log.error("URL: {}", url);
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    public <T> ResponseEntity<T> sendPatchRequest(String url, Object requestBody, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        try {
            return restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

}
