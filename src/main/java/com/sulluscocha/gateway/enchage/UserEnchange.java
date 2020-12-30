package com.sulluscocha.gateway.enchage;

import com.sulluscocha.gateway.configuration.oauth.dto.RecordRequest;
import com.sulluscocha.gateway.configuration.oauth.dto.User;
import com.sulluscocha.gateway.configuration.oauth.dto.UserRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("user-service")
public interface UserEnchange {


    @GetMapping("/user")
    ResponseEntity<User> getUser(@RequestHeader("Authorization") String token);


    @PostMapping("/user")
    ResponseEntity<User> guardarUsuario(@RequestBody UserRequest userRequest);


    @GetMapping("/record")
    ResponseEntity<RecordRequest> getRecord(@RequestHeader("Authorization") String token);



    @PostMapping("/record")
    ResponseEntity<?> guardarRecord(@RequestHeader("Authorization") String token, @RequestBody RecordRequest recordRequest);
}
