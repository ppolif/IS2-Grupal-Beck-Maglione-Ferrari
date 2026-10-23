package com.irojas.demojwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irojas.demojwt.Auth.AuthResponse;
import com.irojas.demojwt.Auth.LoginRequest;
import com.irojas.demojwt.Auth.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoJwtApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void testAuthAndProtectedEndpointFlow() throws Exception {
		String testUser = "user_" + System.currentTimeMillis();
		String testPass = "password123";

		// 1. Registro exitoso
		RegisterRequest registerRequest = RegisterRequest.builder()
				.username(testUser)
				.password(testPass)
				.firstname("John")
				.lastname("Doe")
				.country("Argentina")
				.build();

		MvcResult regResult = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("User registered successfully"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andReturn();

		AuthResponse regResponse = objectMapper.readValue(regResult.getResponse().getContentAsString(), AuthResponse.class);
		assertNotNull(regResponse.getToken());

		// 2. Login exitoso
		LoginRequest loginRequest = LoginRequest.builder()
				.username(testUser)
				.password(testPass)
				.build();

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andReturn();

		AuthResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);
		assertNotNull(loginResponse.getToken());

		// 3. Intento de acceso a endpoint protegido sin token (debe fallar con 403 Forbidden)
		mockMvc.perform(get("/api/demo"))
				.andExpect(status().isForbidden());

		// 4. Acceso a endpoint protegido con token Bearer válido
		mockMvc.perform(get("/api/demo")
				.header("Authorization", "Bearer " + loginResponse.getToken()))
				.andExpect(status().isOk())
				.andExpect(content().string("You accessed our protected endpoint"));
	}

}
