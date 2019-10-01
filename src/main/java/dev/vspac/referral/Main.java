package dev.vspac.referral;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import dev.vspac.referral.v1.ReferralController;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class Main {

    public interface ReferralService {
        void addReferral(String from, String to);
    }

    @Configuration
    class Config {

        @Bean
        public RestTemplate restTemplate(ReferralConverter referralConverter) {
            RestTemplate res = new RestTemplate();
            res.getMessageConverters().add(referralConverter);

            return res;
        }

        @Bean
        public ReferralService referralService() {
            return (from, to) -> System.out.println(to + " was added by " + from);
        }

        @Bean
        public ReferralConverter buildReferralConverter() {
            ReferralConverter referralConverter = new ReferralConverter();
            MediaType mediaType = new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);
            referralConverter.setSupportedMediaTypes(Arrays.asList(mediaType, MediaType.APPLICATION_FORM_URLENCODED));

            return referralConverter;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ReferralController.class, args);
    }

}
