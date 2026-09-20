package com.edusphere.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SmsNotificationProvider {
    private final RestClient client;
    private final String accountSid;
    private final String authToken;
    private final String from;
    private final boolean enabled;

    public SmsNotificationProvider(
            RestClient.Builder builder,
            @Value("${notifications.sms.twilio.base-url:https://api.twilio.com}") String baseUrl,
            @Value("${notifications.sms.twilio.account-sid:}") String accountSid,
            @Value("${notifications.sms.twilio.auth-token:}") String authToken,
            @Value("${notifications.sms.twilio.from:}") String from,
            @Value("${notifications.sms.enabled:false}") boolean enabled) {
        this.client = builder.baseUrl(baseUrl).build();
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.from = from;
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }

    public void send(String recipient, String body) {
        if (!enabled) throw new IllegalStateException("SMS notification provider is disabled");
        if (accountSid.isBlank() || authToken.isBlank() || from.isBlank()) {
            throw new IllegalStateException("Twilio SMS provider is not fully configured");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("SMS recipient phone number is missing");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("SMS body is required");
        }

        client.post()
                .uri("/2010-04-01/Accounts/{accountSid}/Messages.json", accountSid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                .body("To=" + encode(recipient.trim()) + "&From=" + encode(from.trim()) + "&Body=" + encode(body.trim()))
                .retrieve()
                .toBodilessEntity();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
