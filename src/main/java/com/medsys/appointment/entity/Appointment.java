package com.medsys.appointment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single clinic appointment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    private Long id;
    private Long doctorId;
    private String patientName;
    private LocalDateTime time;
    private AppointmentStatus status;
}
