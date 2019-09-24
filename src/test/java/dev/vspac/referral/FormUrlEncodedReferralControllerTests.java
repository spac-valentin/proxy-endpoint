package dev.vspac.referral;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.vspac.referral.v1.ReferralController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static dev.vspac.referral.v1.ReferralController.SUBMIT_FORM;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FormUrlEncodedReferralControllerTests {

    public static final String EMAIL = "newUser@valentinspac.dev";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer thirdPartyMock;


    @Before
    public void before() {
        thirdPartyMock = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldAddReferralAndForward() throws Exception
    {

        thirdPartyMock.expect(ExpectedCount.once(),
                requestTo(SUBMIT_FORM))
                .andExpect(method(POST))
                .andExpect((request) -> matchFormParam(request, "email", Collections.singletonList(EMAIL)))
                .andExpect((request) -> matchFormParam(request, "foo", Collections.singletonList("bar")))
                .andExpect((request) -> matchFormParam(request, "lorem", Arrays.asList("ipsum", "dolor")))
                .andExpect((request) -> matchFormParam(request, "fromEmail", null))
                .andRespond(withSuccess()
                        .contentType(APPLICATION_JSON)
                        .body(String.format("{\"email\":\"%s\"}", EMAIL))
                );

        doRequest()
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"email\":\"%s\"}", EMAIL)));
    }

    // Open PR: https://github.com/spring-projects/spring-framework/pull/23671
    private void matchFormParam(ClientHttpRequest request, String field, List<String> value) throws IOException {
        FormHttpMessageConverter form = new FormHttpMessageConverter();
        final byte[] reqBody = ((MockClientHttpRequest) request).getBodyAsBytes();

        MultiValueMap<String, String> formParams = form.read(null, new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(reqBody);
            }

            @Override
            public HttpHeaders getHeaders() {
                return request.getHeaders();
            }
        });

        assertEquals("Unexpected value for form param " + field, value, formParams.get(field));
    }


    private ResultActions doRequest() throws Exception {
        return mockMvc
                .perform(post(ReferralController.PATH)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                        .param("fromEmail", "existingUser@valentinspac.dev")
                        .param("email", EMAIL)
                        .param("foo", "bar")
                        .param("baz", "fizzbuzz")
                        .param("lorem", "ipsum", "dolor")
                ).andDo(print());
    }

}
