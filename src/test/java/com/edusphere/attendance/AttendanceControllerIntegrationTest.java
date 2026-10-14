package com.edusphere.attendance;

import com.edusphere.security.SchoolUserDetailsService;
import com.edusphere.security.SecurityConfig;
import com.edusphere.security.TenantAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.jwt-secret=test-secret-for-integration-tests-32-chars-minimum",
        "app.security.issuer=http://localhost:8080"
})
class AttendanceControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean AttendanceService service;
    @MockitoBean TenantAccess tenantAccess;
    @MockitoBean SchoolUserDetailsService users;

    @Test @WithMockUser(roles = "SCHOOL_ADMIN")
    void acceptsIsoDateSentByBrowserDateInput() throws Exception {
        UUID school = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 14);
        when(service.dailySummary(school, date)).thenReturn(new AttendanceService.DailySummary(date, 0, 0, 0, 0, 0));
        mvc.perform(get("/api/v1/schools/{schoolId}/attendance/summary", school).param("date", "2026-10-14"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recorded").value(0));
    }
}
