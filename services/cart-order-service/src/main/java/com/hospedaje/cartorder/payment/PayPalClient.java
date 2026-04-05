package com.hospedaje.cartorder.payment;

import com.hospedaje.cartorder.config.PayPalProperties;
import com.hospedaje.cartorder.exception.PayPalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayPalClient {
    private final PayPalProperties props;
    private final RestTemplate restTemplate;

    public boolean isConfigured() {
        return props.isEnabled()
            && props.getClientId() != null && !props.getClientId().isBlank()
            && props.getClientSecret() != null && !props.getClientSecret().isBlank();
    }

    public String getAccessToken() {
        try {
            String auth = props.getClientId() + ":" + props.getClientSecret();
            String basic = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + basic);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> res = restTemplate.exchange(
                props.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                req,
                JsonNode.class
            );
            return res.getBody().get("access_token").asText();
        } catch (HttpStatusCodeException ex) {
            throw buildPayPalError("PAYPAL_TOKEN_FAILED", "No se pudo autenticar con PayPal.", ex);
        } catch (RestClientException ex) {
            throw new PayPalApiException("PAYPAL_TOKEN_FAILED", "No se pudo autenticar con PayPal.", null, snippet(ex.getMessage()));
        }
    }

    /**
     * @return map with keys: paypalOrderId, approvalUrl
     */
    public Map<String, String> createOrder(BigDecimal amountUsd, String returnUrl, String cancelUrl) {
        try {
            String token = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String value = amountUsd.setScale(2, RoundingMode.HALF_UP).toPlainString();
            Map<String, Object> amount = Map.of("currency_code", "USD", "value", value);
            Map<String, Object> unit = Map.of("amount", amount);
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("return_url", returnUrl);
            ctx.put("cancel_url", cancelUrl);
            ctx.put("brand_name", "BookStore");
            ctx.put("user_action", "PAY_NOW");

            Map<String, Object> payload = new HashMap<>();
            payload.put("intent", "CAPTURE");
            payload.put("purchase_units", new Object[] { unit });
            payload.put("application_context", ctx);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<JsonNode> res = restTemplate.exchange(
                props.getBaseUrl() + "/v2/checkout/orders",
                HttpMethod.POST,
                entity,
                JsonNode.class
            );
            JsonNode root = res.getBody();
            String orderId = root.get("id").asText();
            String approve = null;
            for (JsonNode link : root.get("links")) {
                if ("approve".equals(link.get("rel").asText())) {
                    approve = link.get("href").asText();
                    break;
                }
            }
            if (approve == null) {
                throw new IllegalStateException("PayPal response missing approve link");
            }
            return Map.of("paypalOrderId", orderId, "approvalUrl", approve);
        } catch (HttpStatusCodeException ex) {
            throw buildPayPalError("PAYPAL_CREATE_ORDER_FAILED", "No se pudo iniciar el pago con PayPal.", ex);
        } catch (RestClientException ex) {
            throw new PayPalApiException("PAYPAL_CREATE_ORDER_FAILED", "No se pudo iniciar el pago con PayPal.", null, snippet(ex.getMessage()));
        }
    }

    /** @return PayPal capture / transaction id */
    public String captureOrder(String paypalOrderId) {
        try {
            String token = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> res = restTemplate.exchange(
                props.getBaseUrl() + "/v2/checkout/orders/" + paypalOrderId + "/capture",
                HttpMethod.POST,
                entity,
                JsonNode.class
            );
            JsonNode root = res.getBody();
            JsonNode cap = root.path("purchase_units").path(0).path("payments").path("captures").path(0);
            if (cap.hasNonNull("id")) {
                return cap.get("id").asText();
            }
            return paypalOrderId;
        } catch (HttpStatusCodeException ex) {
            throw buildPayPalError("PAYPAL_CAPTURE_FAILED", "No se pudo confirmar el pago con PayPal.", ex);
        } catch (RestClientException ex) {
            throw new PayPalApiException("PAYPAL_CAPTURE_FAILED", "No se pudo confirmar el pago con PayPal.", null, snippet(ex.getMessage()));
        }
    }

    private PayPalApiException buildPayPalError(String code, String message, HttpStatusCodeException ex) {
        return new PayPalApiException(code, message, HttpStatus.resolve(ex.getStatusCode().value()), snippet(ex.getResponseBodyAsString()));
    }

    private String snippet(String text) {
        if (text == null || text.isBlank()) return null;
        return text.length() <= 300 ? text : text.substring(0, 300);
    }
}
