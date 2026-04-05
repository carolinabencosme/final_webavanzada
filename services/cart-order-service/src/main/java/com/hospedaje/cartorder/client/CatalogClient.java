package com.hospedaje.cartorder.client;

import com.hospedaje.cartorder.dto.ApiResponse;
import com.hospedaje.cartorder.dto.PropertyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @GetMapping("/properties/{id}")
    ApiResponse<PropertyDto> getProperty(@PathVariable String id);

    @PostMapping("/properties/batch")
    ApiResponse<List<PropertyDto>> getProperties(@RequestBody List<String> ids);
}
