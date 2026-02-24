package com.ihsan.aidatflow.controller;

import com.ihsan.aidatflow.entity.Due;
import com.ihsan.aidatflow.entity.DueStatus;
import com.ihsan.aidatflow.repository.PaymentRepository;
import com.ihsan.aidatflow.service.DueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ihsan.aidatflow.config.JwtAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean DueService dueService;
    @MockBean PaymentRepository paymentRepository;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void summary_returnsAggregates() throws Exception {
        Due d1 = new Due();
        d1.setId(1L); d1.setApartmentId(1L); d1.setAmount(new BigDecimal("1000")); d1.setDueDate(LocalDate.now()); d1.setStatus(DueStatus.PAID);
        Due d2 = new Due();
        d2.setId(2L); d2.setApartmentId(1L); d2.setAmount(new BigDecimal("500")); d2.setDueDate(LocalDate.now()); d2.setStatus(DueStatus.UNPAID);

        when(dueService.listApartmentDues(1L)).thenReturn(List.of(d1, d2));
        when(paymentRepository.totalPaidForDue(1L)).thenReturn(new BigDecimal("1000"));
        when(paymentRepository.totalPaidForDue(2L)).thenReturn(new BigDecimal("100"));

        mockMvc.perform(get("/api/v1/dashboard/summary").param("apartmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDueAmount").value(1500))
                .andExpect(jsonPath("$.totalCollected").value(1100))
                .andExpect(jsonPath("$.totalOutstanding").value(400))
                .andExpect(jsonPath("$.paidCount").value(1))
                .andExpect(jsonPath("$.unpaidCount").value(1));
    }
}
