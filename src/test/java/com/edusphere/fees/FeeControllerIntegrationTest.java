package com.edusphere.fees;

import com.edusphere.security.SchoolUserDetailsService;
import com.edusphere.security.SecurityConfig;
import com.edusphere.security.TenantAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeeController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.jwt-secret=test-secret-for-integration-tests-32-chars-minimum",
        "app.security.issuer=http://localhost:8080"
})
class FeeControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean FeeService feeService;
    @MockitoBean TenantAccess tenantAccess;
    @MockitoBean SchoolUserDetailsService schoolUserDetailsService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/v1/schools/{schoolId}/fees/students/{studentId}/invoices",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void tenantBoundaryDenialPropagatesAsForbidden() throws Exception {
        UUID school = UUID.randomUUID();
        UUID student = UUID.randomUUID();
        doThrow(new AccessDeniedException("Cross-tenant access denied"))
                .when(tenantAccess).requireSchool(any(), eq(school));

        mvc.perform(get("/api/v1/schools/{schoolId}/fees/students/{studentId}/invoices", school, student))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void unauthorizedRoleCannotReadFinancialEndpoint() throws Exception {
        mvc.perform(get("/api/v1/schools/{schoolId}/fees/students/{studentId}/invoices",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
