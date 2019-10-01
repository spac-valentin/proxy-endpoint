package dev.vspac.referral.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ReferralRegister {

    @Email
    @NotNull
    private String fromEmail;

    @Email
    @NotNull
    private String email;

    private Map<String, Object> extra = new HashMap<>();

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return Collections.unmodifiableMap(this.extra);
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @JsonAnySetter
    private void addExtra(final String key, final Object value) {
        this.extra.put(key, value);
    }

    public ReferralRegister sanitized() {
        ReferralRegister sanitized = new ReferralRegister();
        sanitized.setEmail(this.email);
        sanitized.extra =  new HashMap<>(this.extra);

        return sanitized;
    }
}
