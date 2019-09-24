package dev.vspac.referral;

import dev.vspac.referral.v1.ReferralController;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class Main {

    public interface ReferralService {
        void addReferral(String from, String to);
    }

    @Configuration
    class Config {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public ReferralService referralService() {
            return (from, to) -> System.out.println(to + " was added by " + from);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ReferralController.class, args);
    }

}
