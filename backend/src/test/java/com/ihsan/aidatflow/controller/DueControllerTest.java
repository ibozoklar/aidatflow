package com.ihsan.aidatflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsan.aidatflow.dto.CreateDueRequest;
import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.DueStatus;
import com.ihsan.aidatflow.service.DueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DueController.class)
@AutoConfigureMockMvc(addFilters = false)
class DueControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean DueService dueService;

    @Test
    void createDue_returnsCreatedDue() throws Exception {
        Due due = new Due();
        due.setId(1L);
        due.setApartmentId(2L);
        due.setPeriod("2026-03");
        due.setAmount(new BigDecimal("750.00"));
        due.setDueDate(LocalDate.of(2026,3,10));
        due.setStatus(DueStatus.UNPAID);

        when(dueService.createDue(any())).thenReturn(due);

        CreateDueRequest req = new CreateDueRequest(2L, "2026-03", new BigDecimal("750.00"), LocalDate.of(2026,3,10));

        mockMvc.perform(post("/api/v1/dues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("UNPAID"));
    }
}
