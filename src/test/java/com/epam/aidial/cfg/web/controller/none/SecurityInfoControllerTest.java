package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.web.controller.SecurityInfoController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.json.JsonCompareMode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityInfoController.class)
@Import({
        JsonMapperConfiguration.class,
})
class SecurityInfoControllerTest extends AbstractControllerNoneSecureTest {

    private static final String SECURITY_INFO_BASE_API_PATH = "/api/v1/security-info";

    @Test
    void testGetSecurityInfo() throws Exception {
        var responseJson = "{\"userInfo\":{\"id\":\"anonymousUser\",\"roles\":[\"ROLE_ANONYMOUS\"]}}";

        mockMvc.perform(get(SECURITY_INFO_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson, JsonCompareMode.STRICT));
    }
}