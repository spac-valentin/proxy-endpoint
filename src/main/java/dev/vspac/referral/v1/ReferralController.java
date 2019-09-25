package dev.vspac.referral.v1;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import dev.vspac.referral.Main;
import dev.vspac.referral.domain.ReferralRegister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController(ReferralController.PATH)
@Validated
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


    @RequestMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handler(@Valid @RequestBody ReferralRegister request,
                                          @RequestHeader MultiValueMap<String, String> headers) {
        String fromEmail = request.getFromEmail();
        String referralEmail = request.getEmail();
        referralService.addReferral(fromEmail, referralEmail);

        return restTemplate.exchange(SUBMIT_FORM, HttpMethod.POST, new HttpEntity<>(request.sanitized(), headers), String.class);
    }

    @RequestMapping(consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> formUrlEncodedHandler(@RequestParam("fromEmail") @Email @NotNull String fromEmail,
                                                        @RequestParam("email") @Email @NotNull String referralEmail,
                                                        @RequestParam MultiValueMap<String, String> params,
                                                        @RequestHeader MultiValueMap<String, String> headers) {
        referralService.addReferral(fromEmail, referralEmail);
        params.remove("fromEmail");

        return restTemplate.exchange(SUBMIT_FORM, HttpMethod.POST, new HttpEntity<>(params, headers), String.class);
    }
}
