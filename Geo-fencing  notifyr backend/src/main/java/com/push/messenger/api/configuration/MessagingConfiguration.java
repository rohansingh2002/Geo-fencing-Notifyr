package com.push.messenger.api.configuration;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static com.push.messenger.api.constants.QueryConstants.STR_QUERY_APN_KEY;
import static com.push.messenger.api.constants.QueryConstants.STR_QUERY_FCM_KEY;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MessagingConfiguration {
    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;
    private final static String STR_PROXY_SERVER_HOST = "proxy.server.host";
    private final static String STR_PROXY_SERVER_PORT = "proxy.server.port";

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        List<Tuple> tuple = createTuples(STR_QUERY_FCM_KEY);
        if (environment.getActiveProfiles()[0].equals("local")) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(tuple.get(0).getFileData())).createScoped("https://www.googleapis.com/auth/firebase.messaging");
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).setConnectTimeout(3000).setReadTimeout(3000).build();
            return FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options, "push-api-app"));
        } else  {
            String host = environment.getProperty(STR_PROXY_SERVER_HOST);
            String port = environment.getProperty(STR_PROXY_SERVER_PORT);

            if (StringUtils.hasText(host) && StringUtils.hasText(port)) {
                log.info("proxy details obtained for profile [ " + environment.getActiveProfiles()[0] + " ] are [ " + host + " ] port [ " + port + " ]");
                final HttpTransport transport =  new NetHttpTransport.Builder().setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)))).build();
                GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(tuple.get(0).getFileData()), () -> transport);
                FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).setConnectTimeout(3000).setReadTimeout(3000).setHttpTransport(transport).build();
                return FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options, "push-api-app"));
            } else {
                throw new RuntimeException("please configure host and port either in properties file or as env variables");
            }
        }
    }

    @Bean
    @SneakyThrows
    public ApnsClient apnsClient() {

        List<Tuple> tuple = createTuples(STR_QUERY_APN_KEY);
        if (environment.getActiveProfiles()[0].equals("local")) {
           return new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST).
                    setClientCredentials(new ByteArrayInputStream(tuple.get(0).getFileData()), tuple.get(0).getPassword()).
                   build();
        } else {
            String host = environment.getProperty(STR_PROXY_SERVER_HOST);
            String port = environment.getProperty(STR_PROXY_SERVER_PORT);
            if (StringUtils.hasText(host) && StringUtils.hasText(port)) {
                log.info("proxy details obtained for profile [ " + environment.getActiveProfiles()[0] + " ] are [ " + host + " ] port [ " + port + " ]");
                return new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST).
                            setClientCredentials(new ByteArrayInputStream(tuple.get(0).getFileData()), tuple.get(0).getPassword()).
                            setProxyHandlerFactory(new HttpProxyHandlerFactory(new InetSocketAddress(host, Integer.parseInt(port)))).
                        build();
            } else {
                throw new RuntimeException("please configure host and port either in properties file or as env variables in the following format [ -D" + STR_PROXY_SERVER_HOST + " -D" + STR_PROXY_SERVER_PORT + " ]");
            }
        }
    }


    private List<Tuple> createTuples(String query) {
        return  jdbcTemplate.query(query, (rs, rowNum) ->
                Tuple.builder().fileData(rs.getBytes(1)).password(rs.getString(2)).build());
    }
 }

 @Builder
 @AllArgsConstructor
 @NoArgsConstructor
 @Getter
 class Tuple {
    private byte[] fileData;
    private String password;
}


