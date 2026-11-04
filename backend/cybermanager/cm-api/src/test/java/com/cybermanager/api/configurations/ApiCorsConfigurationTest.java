package com.cybermanager.api.configurations;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCorsConfigurationTest {

    @Test
    void shouldAllowAuthenticatedCommentPatchFromFrontend() throws Exception {
        var request = new MockHttpServletRequest("OPTIONS", "/api/customers/test/comment");
        request.addHeader("Origin", "http://localhost:4200");
        request.addHeader("Access-Control-Request-Method", "PATCH");
        request.addHeader("Access-Control-Request-Headers", "Authorization,Content-Type");
        var response = new MockHttpServletResponse();
        new ApiCorsConfiguration().corsFilter().doFilter(request, response, (incoming, outgoing) -> {
            throw new AssertionError("Preflight should be handled by CorsFilter");
        });
        assertEquals(200, response.getStatus());
        assertTrue(response.getHeader("Access-Control-Allow-Methods").contains("PATCH"));
    }

}
