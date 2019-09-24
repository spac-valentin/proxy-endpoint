package dev.vspac.referral;

import dev.vspac.referral.v1.ReferralController;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import static dev.vspac.referral.v1.ReferralController.SUBMIT_FORM;
import static org.springframework.http.HttpMethod.POST;
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
public class ReferralControllerTests {

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
                .andExpect(MockRestRequestMatchers.jsonPath("email", Matchers.is(EMAIL)))
                .andExpect(MockRestRequestMatchers.jsonPath("foo", Matchers.is("bar")))
                .andExpect(MockRestRequestMatchers.jsonPath("fromEmail").doesNotExist())
                .andExpect(MockRestRequestMatchers.jsonPath("lorem.ipsum").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("dolor").isArray())
                .andRespond(withSuccess()
                        .contentType(APPLICATION_JSON)
                        .body(String.format("{\"email\":\"%s\"}", EMAIL))
                );

        doRequest()
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"email\":\"%s\"}", EMAIL)));
    }

    private ResultActions doRequest() throws Exception {
        return mockMvc
                .perform(post(ReferralController.PATH)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "\"fromEmail\" : \"existingUser@valentinspac.dev\"," +
                                "\"email\" : \"" + EMAIL +"\"," +
                                "\"foo\" : \"bar\"," +
                                "\"baz\" : \"fizzbuzz\"," +
                                "\"lorem\" : {\"ipsum\":true}," +
                                "\"dolor\" : [{\"sit\":false}, {\"amet\":\"consectetur\"}]" +
                                "}")
                ).andDo(print());
    }

}
