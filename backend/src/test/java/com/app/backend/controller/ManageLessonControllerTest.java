package com.app.backend.controller;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.CorsProperties;
import com.app.backend.config.SecurityConfig;
import com.app.backend.dto.response.LessonResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.User;
import com.app.backend.model.enums.ContentFormat;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.model.enums.UserRole;
import com.app.backend.security.JwtAuthFilter;
import com.app.backend.service.ManageLessonService;
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

@WebMvcTest(ManageLessonController.class)
@Import({SecurityConfig.class, CorsProperties.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:4200")
class ManageLessonControllerTest {

    private static final UUID MODULE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID LESSON_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageLessonService manageLessonService;

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
    void testGivenContentManagerWhenCreateLessonThenReturn201() throws Exception {
        LessonResponse response = new LessonResponse(
                LESSON_ID, MODULE_ID, "New Lesson", "# Content", ContentStatus.DRAFT,
                ContentFormat.MARKDOWN, null, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageLessonService.create(eq(MODULE_ID), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/manage/modules/{moduleId}/lessons", MODULE_ID)
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New Lesson", "content": "# Content"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Lesson"))
                .andExpect(jsonPath("$.moduleId").value(MODULE_ID.toString()));
    }

    @Test
    void testGivenAdminWhenCreateLessonThenReturn201() throws Exception {
        LessonResponse response = new LessonResponse(
                LESSON_ID, MODULE_ID, "Admin Lesson", null, ContentStatus.DRAFT,
                ContentFormat.MARKDOWN, null, false, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageLessonService.create(eq(MODULE_ID), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/manage/modules/{moduleId}/lessons", MODULE_ID)
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Admin Lesson"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void testGivenRegularUserWhenCreateLessonThenReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/manage/modules/{moduleId}/lessons", MODULE_ID)
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Forbidden"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGivenBlankTitleWhenCreateLessonThenReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/manage/modules/{moduleId}/lessons", MODULE_ID)
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGivenMissingModuleWhenCreateLessonThenReturn404() throws Exception {
        when(manageLessonService.create(eq(MODULE_ID), any()))
                .thenThrow(new ResourceNotFoundException("Module", MODULE_ID));

        mockMvc.perform(post("/api/v1/manage/modules/{moduleId}/lessons", MODULE_ID)
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Test"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGivenContentManagerWhenGetLessonThenReturnFullLesson() throws Exception {
        LessonResponse response = new LessonResponse(
                LESSON_ID, MODULE_ID, "Lesson", "Full **markdown** content", ContentStatus.DRAFT,
                ContentFormat.MARKDOWN, "https://video.url", false,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(manageLessonService.getById(LESSON_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/manage/lessons/{id}", LESSON_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lesson"))
                .andExpect(jsonPath("$.content").value("Full **markdown** content"));
    }

    @Test
    void testGivenMissingIdWhenGetLessonThenReturn404() throws Exception {
        when(manageLessonService.getById(LESSON_ID))
                .thenThrow(new ResourceNotFoundException("Lesson", LESSON_ID));

        mockMvc.perform(get("/api/v1/manage/lessons/{id}", LESSON_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGivenContentManagerWhenUpdateLessonThenReturnUpdated() throws Exception {
        LessonResponse response = new LessonResponse(
                LESSON_ID, MODULE_ID, "Updated", "new content", ContentStatus.DRAFT,
                ContentFormat.HTML, null, true, OffsetDateTime.now(), OffsetDateTime.now());
        when(manageLessonService.update(eq(LESSON_ID), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/manage/lessons/{id}", LESSON_ID)
                        .with(authentication(contentManagerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated", "content": "new content", "contentFormat": "HTML", "ifFreePreview": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.contentFormat").value("HTML"));
    }

    @Test
    void testGivenContentManagerWhenDeleteLessonThenReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/manage/lessons/{id}", LESSON_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGivenMissingIdWhenDeleteLessonThenReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Lesson", LESSON_ID))
                .when(manageLessonService).delete(LESSON_ID);

        mockMvc.perform(delete("/api/v1/manage/lessons/{id}", LESSON_ID)
                        .with(authentication(contentManagerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGivenNoAuthWhenGetLessonThenReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/manage/lessons/{id}", LESSON_ID))
                .andExpect(status().isForbidden());
    }
}
