package com.app.backend.controller;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.CorsProperties;
import com.app.backend.config.SecurityConfig;
import com.app.backend.dto.response.ModuleDetailResponse;
import com.app.backend.dto.response.ModuleDetailResponse.LessonSummary;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.User;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.model.enums.UserRole;
import com.app.backend.security.JwtAuthFilter;
import com.app.backend.service.ManageModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManageModuleController.class)
@Import({SecurityConfig.class, CorsProperties.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:4200")
class ManageModuleControllerTest {

    private static final UUID MODULE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageModuleService manageModuleService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private UsernamePasswordAuthenticationToken contentManagerAuth;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        User cmUser = TestEntityFactory.createUser(USER_ID, "g-1", "cm@test.com", "CM");
        cmUser.setRole(UserRole.CONTENT_MANAGER);
        contentManagerAuth = new UsernamePasswordAuthenticationToken(
                cmUser, null, List.of(new SimpleGrantedAuthority("ROLE_CONTENT_MANAGER")));

        User adminUser = TestEntityFactory.createUser(USER_ID, "g-2", "admin@test.com", "Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        User regularUser = TestEntityFactory.createUser(USER_ID, "g-3", "user@test.com", "User");
        userAuth = new UsernamePasswordAuthenticationToken(
                regularUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGivenContentManagerWhenCreateModuleThenReturn201() throws Exception {
        ModuleResponse response = new ModuleResponse(
                MODULE_ID, "New Module", "desc", null, ContentStatus.DRAFT,
                0, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageModuleService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/manage/modules")
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New Module", "description": "desc"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Module"));
    }

    @Test
    void testGivenAdminWhenCreateModuleThenReturn201() throws Exception {
        ModuleResponse response = new ModuleResponse(
                MODULE_ID, "Admin Module", null, null, ContentStatus.DRAFT,
                0, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageModuleService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/manage/modules")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Admin Module"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void testGivenRegularUserWhenCreateModuleThenReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/manage/modules")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Forbidden Module"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGivenNoAuthWhenListModulesThenReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/manage/modules"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGivenBlankTitleWhenCreateModuleThenReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/manage/modules")
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGivenContentManagerWhenGetAllThenReturnModules() throws Exception {
        ModuleResponse r = new ModuleResponse(
                MODULE_ID, "M1", null, null, ContentStatus.DRAFT,
                0, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageModuleService.getAll()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/manage/modules")
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("M1"));
    }

    @Test
    void testGivenContentManagerWhenGetByIdThenReturnModuleWithLessons() throws Exception {
        UUID lessonId = UUID.randomUUID();
        ModuleDetailResponse response = new ModuleDetailResponse(
                MODULE_ID, "Module", "desc", null, ContentStatus.DRAFT,
                0, false, OffsetDateTime.now(), OffsetDateTime.now(),
                List.of(new LessonSummary(lessonId, "Lesson 1")));
        when(manageModuleService.getById(MODULE_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/manage/modules/{id}", MODULE_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Module"))
                .andExpect(jsonPath("$.lessons[0].title").value("Lesson 1"));
    }

    @Test
    void testGivenMissingIdWhenGetByIdThenReturn404() throws Exception {
        when(manageModuleService.getById(MODULE_ID))
                .thenThrow(new ResourceNotFoundException("Module", MODULE_ID));

        mockMvc.perform(get("/api/v1/manage/modules/{id}", MODULE_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGivenContentManagerWhenUpdateThenReturnUpdatedModule() throws Exception {
        ModuleResponse response = new ModuleResponse(
                MODULE_ID, "Updated", null, null, ContentStatus.PUBLISHED,
                0, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageModuleService.update(eq(MODULE_ID), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/manage/modules/{id}", MODULE_ID)
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated", "status": "PUBLISHED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void testGivenContentManagerWhenDeleteThenReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/manage/modules/{id}", MODULE_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGivenMissingIdWhenDeleteThenReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Module", MODULE_ID))
                .when(manageModuleService).delete(MODULE_ID);

        mockMvc.perform(delete("/api/v1/manage/modules/{id}", MODULE_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNotFound());
    }
}
