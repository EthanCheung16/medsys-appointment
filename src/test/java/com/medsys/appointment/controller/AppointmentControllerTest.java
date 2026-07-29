package com.medsys.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the REST API (Spring Boot Test + MockMvc).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String iso(LocalDateTime time) {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Test
    @DisplayName("POST /api/appointments books an appointment successfully")
    void bookEndpointCreatesAppointment() throws Exception {
        String body = """
                {"doctorId": 10, "patientName": "Alice Tan", "time": "%s"}
                """.formatted(iso(LocalDateTime.now().plusDays(2)));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("Appointment booked successfully"))
                .andExpect(jsonPath("$.resultData.status").value("BOOKED"))
                .andExpect(jsonPath("$.resultData.patientName").value("Alice Tan"));
    }

    @Test
    @DisplayName("POST with a past time returns a failure message")
    void bookPastTimeReturnsFailure() throws Exception {
        String body = """
                {"doctorId": 10, "patientName": "Alice Tan", "time": "%s"}
                """.formatted(iso(LocalDateTime.now().minusDays(1)));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Cannot book an appointment in the past"));
    }

    @Test
    @DisplayName("DELETE marks an appointment as cancelled in subsequent GET results")
    void cancelEndpointMarksAppointmentCancelled() throws Exception {
        long doctorId = 1000L + System.nanoTime();
        String body = """
                {"doctorId": %d, "patientName": "Delete Me", "time": "%s"}
                """.formatted(doctorId, iso(LocalDateTime.now().plusDays(3)));

        String response = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = new ObjectMapper().readTree(response)
                .path("resultData")
                .path("id")
                .asText();

        mockMvc.perform(delete("/api/appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.resultData.status").value("CANCELLED"));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultData[?(@.id == " + id + ")].status")
                        .value(hasItem("CANCELLED")));
    }
}
