package com.medsys.appointment.service;

import com.medsys.appointment.entity.ResultRet;
import com.medsys.appointment.entity.Appointment;
import com.medsys.appointment.entity.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the appointment booking business rules (JUnit 5).
 */
class AppointmentServiceTest {

    private AppointmentService service;
    private LocalDateTime tomorrow;

    @BeforeEach
    void setUp() {
        service = new AppointmentService();
        tomorrow = LocalDateTime.now().plusDays(1).withNano(0);
    }

    @Test
    @DisplayName("Booking a valid future appointment succeeds")
    void bookValidAppointment() {
        ResultRet result = service.book(1L, "Alice Tan", tomorrow);
        Appointment appointment = (Appointment) result.getResultData();

        assertEquals(1, result.getCode());
        assertNotNull(appointment.getId());
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
        assertEquals(1, ((List<?>) service.findAll().getResultData()).size());
    }

    @Test
    @DisplayName("Booking an appointment in the past is rejected")
    void bookInThePastFails() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        ResultRet result = service.book(1L, "Alice Tan", yesterday);

        assertEquals(1, result.getCode());
        assertEquals("Cannot book an appointment in the past", result.getMessage());
    }

    @Test
    @DisplayName("Double-booking the same doctor for the same slot is rejected")
    void doubleBookingSameDoctorFails() {
        service.book(1L, "Alice Tan", tomorrow);

        ResultRet result = service.book(1L, "Bob Lee", tomorrow);

        assertEquals(0, result.getCode());
        assertEquals("Doctor 1 already has an appointment at " + tomorrow, result.getMessage());
    }

    @Test
    @DisplayName("Different doctors can be booked for the same slot")
    void differentDoctorsSameSlotSucceeds() {
        service.book(1L, "Alice Tan", tomorrow);
        ResultRet result = service.book(2L, "Bob Lee", tomorrow);
        Appointment second = (Appointment) result.getResultData();

        assertEquals(1, result.getCode());
        assertEquals(AppointmentStatus.BOOKED, second.getStatus());
        assertEquals(2, ((List<?>) service.findAll().getResultData()).size());
    }

    @Test
    @DisplayName("A blank patient name is rejected")
    void blankPatientNameFails() {
        ResultRet result = service.book(1L, "   ", tomorrow);

        assertEquals(0, result.getCode());
        assertEquals("doctorId, patientName and time are required", result.getMessage());
    }

    @Test
    @DisplayName("Cancelling an existing appointment succeeds")
    void cancelExistingAppointment() {
        Appointment appointment = (Appointment) service.book(1L, "Alice Tan", tomorrow).getResultData();

        ResultRet result = service.cancel(appointment.getId());
        Appointment cancelled = (Appointment) result.getResultData();

        assertEquals(1, result.getCode());
        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());
        List<?> appointments = (List<?>) service.findAll().getResultData();
        assertEquals(1, appointments.size());
        assertEquals(AppointmentStatus.CANCELLED, ((Appointment) appointments.get(0)).getStatus());
    }

    @Test
    @DisplayName("Cancelling an unknown appointment fails")
    void cancelUnknownAppointmentFails() {
        ResultRet result = service.cancel(999L);

        assertEquals(0, result.getCode());
        assertEquals("Appointment not found: 999", result.getMessage());
    }

    @Test
    @DisplayName("A cancelled slot can be booked again")
    void cancelledSlotCanBeRebooked() {
        Appointment first = (Appointment) service.book(1L, "Alice Tan", tomorrow).getResultData();
        service.cancel(first.getId());

        Appointment second = (Appointment) service.book(1L, "Bob Lee", tomorrow).getResultData();

        assertEquals(AppointmentStatus.BOOKED, second.getStatus());
    }

}
