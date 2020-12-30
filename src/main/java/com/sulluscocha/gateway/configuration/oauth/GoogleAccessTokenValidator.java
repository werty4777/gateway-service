package com.sulluscocha.gateway.configuration.oauth;


import com.sulluscocha.gateway.configuration.oauth.dto.RecordRequest;
import com.sulluscocha.gateway.configuration.oauth.dto.responseAuthGoogle;
import com.sulluscocha.gateway.enchage.UserEnchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("google")
public class GoogleAccessTokenValidator {


    @Autowired
    private UserEnchange recordEnchange;

    @Value("${oauth.clientId}")
    private String clientId;

    @Value("${oauth.clientmovil}")
    private String clientIdMovil;


    @Value("${oauth.checkTokenUrl}")
    private String checkTokenUrl;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void defineHandler() {
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() == 400) {
                    throw new InvalidTokenException("El token es invalido");
                }
            }
        });
    }


    public responseAuthGoogle validate(String accessToken) {
        System.out.println("estoy validando :" + accessToken);


        Map<String, ?> response = getGoogleResponse(accessToken);


        boolean b = validateResponse(response);


        return new responseAuthGoogle(response, b);

    }

    private boolean validateResponse(Map<String, ?> response) throws AuthenticationException {

        String aud = (String) response.get("aud");

        if (aud.equalsIgnoreCase(this.getClientId())) {

            return true;
        } else return aud.equalsIgnoreCase(this.getClientIdMovil());


    }


    public Map<String, ?> getGoogleResponse(String accessToken) {

        try {

            ResponseEntity<RecordRequest> record = this.recordEnchange.getRecord(accessToken);


            if (record.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {
                Map<String, Object> map = (Map<String, Object>) restTemplate.exchange(checkTokenUrl + accessToken, HttpMethod.GET, null, Map.class).getBody();


                RecordRequest dd = new RecordRequest();

                dd.setToken(accessToken);
                dd.setAud(map.get("aud").toString());
                dd.setScope(map.get("scope").toString());
                dd.setExp(map.get("exp").toString());
                dd.setEmail(map.get("email").toString());
                dd.setAccess_type(map.get("access_type").toString());
                dd.setSub(map.get("sub").toString());

                this.recordEnchange.guardarRecord(accessToken, dd);

                return map;
            }
            RecordRequest access = record.getBody();
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("aud", access.getAud());
            mapa.put("scope", access.getScope());
            mapa.put("exp", access.getExp());
            mapa.put("email", access.getEmail());
            mapa.put("access_type", access.getAccess_type());
            mapa.put("sub", access.getSub());

            return mapa;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error al guardar el token");
        }
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCheckTokenUrl() {
        return checkTokenUrl;
    }

    public void setCheckTokenUrl(String checkTokenUrl) {
        this.checkTokenUrl = checkTokenUrl;
    }


    public String getClientIdMovil() {
        return clientIdMovil;
    }

    public void setClientIdMovil(String clientIdMovil) {
        this.clientIdMovil = clientIdMovil;
    }
}
