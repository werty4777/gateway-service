package com.sulluscocha.gateway.configuration.oauth;

import com.sulluscocha.gateway.configuration.oauth.dto.RecordRequest;
import com.sulluscocha.gateway.configuration.oauth.dto.User;
import com.sulluscocha.gateway.configuration.oauth.dto.UserRequest;
import com.sulluscocha.gateway.configuration.oauth.dto.responseAuthGoogle;
import com.sulluscocha.gateway.enchage.UserEnchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Map;

import static java.util.Collections.singleton;

@Service("googleService")
public class GoogleTokenServices implements ResourceServerTokenServices {

    private final AccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
    private final String userEmailUrl = "https://people.googleapis.com/v1/people/me?personFields=emailAddresses";


    @Autowired
    private UserEnchange userEnchange;

    private String userInfoUrl;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("google")
    private GoogleAccessTokenValidator googleAccessTokenValidator;


    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {

        System.out.println("entre aca en la autenticacion");
        responseAuthGoogle validate = this.googleAccessTokenValidator.validate(accessToken);
        if (!validate.isValidate()) {
            throw new UnapprovedClientAuthenticationException("Este token no sirve ga.");
        }
        Map<String, ?> tokenInfo = validate.getResponse();
        System.out.println(tokenInfo);
        OAuth2Authentication authentication = getAuthentication(tokenInfo, accessToken);

        return authentication;
    }

    private OAuth2Authentication getAuthentication(Map<String, ?> tokenInfo, String accessToken) {


        System.out.println("Esta es la informacion del token" + tokenInfo);
        OAuth2Request request = tokenConverter.extractAuthentication(tokenInfo).getOAuth2Request();
        Authentication authentication = getAuthenticationToken(accessToken);
        return new OAuth2Authentication(request, authentication);
    }

    private Authentication getAuthenticationToken(String accessToken) {

        try {
            String idStr = getIdUser(accessToken);


            String email = getEmail(accessToken);


            ResponseEntity<User> userSaved = this.userEnchange.getUser(accessToken);
            User body = userSaved.getBody();
            if (userSaved.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {


                UserRequest user = new UserRequest();
                System.out.println(email);
                System.out.println(idStr);
                user.setEmail(email);
                user.setId(idStr);
                ResponseEntity<User> responseEntity = this.userEnchange.guardarUsuario(user);
                User body1 = responseEntity.getBody();

                if (responseEntity.getStatusCode().value() != HttpStatus.OK.value()) {
                    throw new RuntimeException("error en la peticon");
                } else {
                    GooglePrincipal googlePrincipal = new GooglePrincipal(new BigInteger(idStr));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(googlePrincipal, "12", singleton(new SimpleGrantedAuthority("ROLE_" + body1.getRol().getRol())));
                    System.out.println(usernamePasswordAuthenticationToken.toString());

                    return usernamePasswordAuthenticationToken;
                }


            } else {
                GooglePrincipal googlePrincipal = new GooglePrincipal(new BigInteger(idStr));
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(googlePrincipal, null, singleton(new SimpleGrantedAuthority("ROLE_" + body.getRol().getRol())));
                System.out.println(usernamePasswordAuthenticationToken.toString());
                System.out.println(body.getRol().getRol());
                return usernamePasswordAuthenticationToken;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("error");
        }
    }

    public String getEmail(String accessToken) {


        return getUserInfo(accessToken).getEmail();

    }

    private String getIdUser(String accessToken) {

        String idStr = getUserInfo(accessToken).getSub();
        if (idStr == null) {
            throw new InternalAuthenticationServiceException("Cannot get id from user info");
        }
        return idStr;
    }

    private RecordRequest getUserInfo(String accessToken) {

        System.out.println(accessToken);
        ResponseEntity<RecordRequest> record = this.userEnchange.getRecord(accessToken);
        if (record.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {
            throw new UnapprovedClientAuthenticationException("Este token no sirve ga.");

        }


        return record.getBody();
    }

    private HttpHeaders getHttpHeaders(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }
}
