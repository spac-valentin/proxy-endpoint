package dev.vspac.referral.v1;

import javax.validation.Valid;

import dev.vspac.referral.Main;
import dev.vspac.referral.domain.ReferralRegister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController(ReferralController.PATH)
public class ReferralController {
    public static final String SUBMIT_FORM = "/submit_form";
    public static final String PATH = "/referral";

    private final RestTemplate restTemplate;
    private final Main.ReferralService referralService;

    @Autowired
    public ReferralController(RestTemplate restTemplate, Main.ReferralService referralService) {
        this.restTemplate = restTemplate;
        this.referralService = referralService;
    }


    @RequestMapping
    public ResponseEntity<String> handler(@Valid @RequestBody ReferralRegister request,
                                          @RequestHeader MultiValueMap<String, String> headers) {
        String fromEmail = request.getFromEmail();
        String referralEmail = request.getEmail();
        referralService.addReferral(fromEmail, referralEmail);

        return restTemplate.exchange(SUBMIT_FORM, HttpMethod.POST, new HttpEntity<>(request.sanitized(), headers), String.class);
    }
}
