package com.bookstore.cartorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalPublicConfigDto {
    private boolean enabled;
    private String clientId;
    private String currency;
    /** Optional. Values: sandbox/live when inferable from PayPal base URL. */
    private String baseUrlMode;
    private String provider;
}
