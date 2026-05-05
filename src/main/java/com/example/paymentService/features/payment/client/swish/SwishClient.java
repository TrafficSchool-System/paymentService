package com.example.paymentService.features.payment.client.swish;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentRequest;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentResponse;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Service
public class SwishClient {

    private final RestTemplate restTemplate;
    private final SwishProperties properties;

    public SwishClient(SwishProperties properties) throws Exception {
        this.properties = properties;
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() throws Exception {
        // Ladda keystore (certifikat) från classpath
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        String keyStoreResourcePath = properties.getKeyStorePath().replace("classpath:", "");

        try (var keyStoreStream = getClass().getClassLoader().getResourceAsStream(keyStoreResourcePath)) {
            if (keyStoreStream == null) {
                throw new IllegalArgumentException("Keystore hittades inte: " + properties.getKeyStorePath());
            }
            keyStore.load(keyStoreStream, properties.getKeyStorePassword().toCharArray());
        }

        // Ladda CA-certifikat från PEM-fil och lägg till i trustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null); // Initiera tom truststore

        String caCertResourcePath = properties.getCaCertPath().replace("classpath:", "");
        try (InputStream caCertStream = getClass().getClassLoader().getResourceAsStream(caCertResourcePath)) {
            if (caCertStream == null) {
                throw new IllegalArgumentException("CA-certifikat hittades inte: " + properties.getCaCertPath());
            }

            // Ladda PEM-certifikat
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(caCertStream);
            trustStore.setCertificateEntry("swish-ca", caCert);
        }

        // Skapa SSL context med HttpClient 5.x syntax
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, properties.getKeyStorePassword().toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();

        // Skapa TLS strategy med moderna API:n
        DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);

        // Skapa connection manager med TLS
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .build();

        // Skapa HttpClient 5.x
        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    /**
     * Skapar en ny betalning i Swish (PUT
     * /api/v2/paymentrequests/{instructionUUID})
     * 
     * @param instructionUUID Unikt UUID för betalningen (32 hex-tecken utan
     *                        bindestreck)
     * @param request         Swish payment request med alla nödvändiga fält
     * @return ResponseEntity med Location header om success (201 Created)
     */
    public ResponseEntity<String> createPayment(String instructionUUID, SwishPaymentRequest request) {
        String url = properties.getBaseUrl() + "/swish-cpcapi/api/v2/paymentrequests/" + instructionUUID;

        // Swish kräver Content-Type: application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SwishPaymentRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    /**
     * Hämtar status för en betalning (GET
     * /api/v1/paymentrequests/{instructionUUID})
     * 
     * @param instructionUUID Payment request ID
     * @return SwishPaymentResponse med status, paymentReference, etc.
     */
    public ResponseEntity<SwishPaymentResponse> getPaymentStatus(String instructionUUID) {
        String url = properties.getBaseUrl() + "/swish-cpcapi/api/v1/paymentrequests/" + instructionUUID;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, SwishPaymentResponse.class);
    }

    /**
     * Avbryter en betalning (PATCH /api/v1/paymentrequests/{instructionUUID})
     * 
     * Kan endast avbryta betalningar som inte är signerade/genomförda.
     * 
     * @param instructionUUID Payment request ID
     * @return SwishPaymentResponse med status "CANCELLED"
     */
    public ResponseEntity<SwishPaymentResponse> cancelPayment(String instructionUUID) {
        String url = properties.getBaseUrl() + "/swish-cpcapi/api/v1/paymentrequests/" + instructionUUID;

        // PATCH body för att sätta status till cancelled
        List<Map<String, String>> patch = List.of(Map.of(
                "op", "replace",
                "path", "/status",
                "value", "cancelled"));

        // Swish kräver Content-Type: application/json-patch+json för PATCH
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));

        HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(patch, headers);
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, SwishPaymentResponse.class);
    }
}
