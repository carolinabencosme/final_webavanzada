package com.hospedaje.cartorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {
    /** Sandbox or live API base (no trailing slash) */
    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private String clientId = "";
    private String clientSecret = "";
    /** When false, PayPal REST calls are disabled; use mock checkout instead */
    private boolean enabled = false;
    /** Currency exposed to frontend SDK initialization */
    private String currency = "USD";
    /**
     * Enables diagnostic PayPal health endpoint in any environment.
     * Keep false by default and use local/dev profiles for non-production access.
     */
    private boolean healthEnabled = false;
}
