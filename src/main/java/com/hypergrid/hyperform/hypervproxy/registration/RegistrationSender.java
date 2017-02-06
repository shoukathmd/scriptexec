package com.hypergrid.hyperform.hypervproxy.registration;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Intesar Mohammed
 */
@Component
public class RegistrationSender {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private RestTemplate template = null;
    private ParameterizedTypeReference<String> singleTypeReference = new ParameterizedTypeReference<String>() {
    };


    public RegistrationSender() {
        template = new RestTemplate(useApacheHttpClientWithSelfSignedSupport());
    }

    public String sendAndReceiveProxy(String endpoint, HyperCloudClusterRegistrationRequest dto) {

        //logger.debug("Executing: [{}]", map);

        String url = endpoint;
        URI uri = getUri(url);

        String username = "admin";
        String password = "";

        //password = DigestUtils.sha256Hex(password);

        //String authHeader = buildAuth(username, password);
        //set your entity to send
        //RequestEntity request = getBasicRequestEntity(authHeader, uri);

        MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        //params.add("cmdlet", "" + command);
        params.add("Content-Type", "application/json");

        org.springframework.web.util.UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();

        //RequestEntity request = getBasicRequestEntity(null, uriComponents.toUri());

        HttpEntity<HyperCloudClusterRegistrationRequest> entity = new HttpEntity(dto, params);

        ResponseEntity<String> res = null;
        try {
            res =
                    template.exchange(
                            uriComponents.toUriString(),
                            HttpMethod.POST,
                            entity,
                            singleTypeReference
                    );
        } catch (Exception e) {
            logger.warn("Make sure hypercloud.local is resolvable/reachable from this node.");
            logger.warn(e.getLocalizedMessage());
            return "Error";
        }

        logger.info("Response: [{}]", res.getBody());
        return res.getBody();
    }

    private HttpHeaders getHttpHeaders(String username, String password) {
        HttpHeaders map = new HttpHeaders();
        //map.add(HttpHeaders.AUTHORIZATION, buildAuth(username, password));
        map.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        map.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return map;
    }

    private RequestEntity getBasicRequestEntity(String authHeader, URI uri) {
        return RequestEntity.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                //.header("Authorization", authHeader)
                .build();
    }


    private URI getUri(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }


    private String buildAuth(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = org.apache.commons.codec.binary.Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")));
        return "Basic " + new String(encodedAuth);
    }

    /**
     * Trust manager that does not perform nay checks.
     */
    private static class NullX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            System.out.println();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            System.out.println();
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

    /**
     * Host name verifier that does not perform nay checks.
     */
    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    /**
     * Disable trust checks for SSL connections.
     */
    public static void disableChecks() throws NoSuchAlgorithmException,
            KeyManagementException {

        try {
            new URL("https://0.0.0.0/").getContent();
        } catch (IOException e) {
            // This invocation will always fail, but it will register the
            // default SSL provider to the URL class.
        }

        try {
            SSLContext sslc;

            sslc = SSLContext.getInstance("TLS");

            TrustManager[] trustManagerArray = {new NullX509TrustManager()};
            sslc.init(null, trustManagerArray, null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private static HttpComponentsClientHttpRequestFactory useApacheHttpClientWithSelfSignedSupport() {

        SSLContext sslc = null;
        try {

            sslc = SSLContext.getInstance("TLS");

            TrustManager[] trustManagerArray = {new NullX509TrustManager()};
            sslc.init(null, trustManagerArray, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setSSLContext(sslc)
                .build();
        HttpComponentsClientHttpRequestFactory useApacheHttpClient = new HttpComponentsClientHttpRequestFactory();
        useApacheHttpClient.setHttpClient(httpClient);
        return useApacheHttpClient;
    }
}
