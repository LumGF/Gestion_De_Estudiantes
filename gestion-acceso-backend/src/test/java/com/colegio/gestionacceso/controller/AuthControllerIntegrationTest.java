package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.LoginRequestDTO;
import com.colegio.gestionacceso.dto.LoginResponseDTO;
import com.colegio.gestionacceso.model.Rol;
import com.colegio.gestionacceso.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void login_retornaToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("admin@colegio.edu");
        request.setPassword("admin123");

        when(authService.login(any())).thenReturn(
                new LoginResponseDTO("token-jwt", "admin@colegio.edu", "Admin", Rol.ADMIN)
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }
}
