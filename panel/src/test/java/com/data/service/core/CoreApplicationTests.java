package com.data.service.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoreApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testRestfulServiceLoadsAndReturnsOk() throws Exception {
		// Testing if the Trade user-scoped endpoints respond correctly.
		mockMvc.perform(get("/api/user/trades").with(oauth2Login()))
				.andExpect(status().isOk());
	}
}
