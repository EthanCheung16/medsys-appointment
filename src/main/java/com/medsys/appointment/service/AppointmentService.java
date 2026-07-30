package com.medsys.appointment.service;

import com.medsys.appointment.entity.Appointment;
import com.medsys.appointment.entity.AppointmentStatus;
import com.medsys.appointment.entity.ResultRet;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core booking logic for the MedSys clinic appointment module.
 * Uses in-memory storage; a production system would substitute a persistent
 * repository behind the same service interface.
 */
@Service
public class AppointmentService {

    private final Map<Long, Appointment> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Books an appointment for a patient with a doctor at a given time.
     *
     * Business rules and the response each one produces:
     * 1. All fields are required
     *    code 0, "doctorId, patientName and time are required"
     * 2. The appointment time must be in the future
     *    code 0, "Cannot book an appointment in the past"
     * 3. A doctor cannot be double-booked for the same time slot
     *    code 0, "Doctor {id} already has an appointment at {time}"
     *
     * On success the method returns code 1 with the message
     * "Appointment booked successfully" and the created appointment.
     *
     * A cancelled appointment releases its slot, so the same doctor and
     * time can be booked again afterwards.
     */
    public synchronized ResultRet book(Long doctorId, String patientName, LocalDateTime time) {
        // Rule 1: all booking fields are mandatory.
        if (doctorId == null || patientName == null || patientName.isBlank() || time == null) {
            return ResultRet.errorResult("doctorId, patientName and time are required");
        }
        // Rule 2: the appointment must be in the future.
        if (time.isBefore(LocalDateTime.now())) {
            return ResultRet.errorResult("Cannot book an appointment in the past");
        }

        // Rule 3: the doctor must be free for this slot.
        boolean slotTaken = false;
        for (Appointment appointment : store.values()) {
            if (appointment.getStatus() == AppointmentStatus.BOOKED
                    && appointment.getDoctorId().equals(doctorId)
                    && appointment.getTime().equals(time)) {
                slotTaken = true;
                break;
            }
        }
        if (slotTaken) {
            return ResultRet.errorResult(
                    "Doctor " + doctorId + " already has an appointment at " + time);
        }

        Appointment appointment = new Appointment(
                idGenerator.getAndIncrement(), doctorId, patientName, time, AppointmentStatus.BOOKED);
        store.put(appointment.getId(), appointment);
        return ResultRet.okResult("Appointment booked successfully", appointment);
    }

    /**
     * Marks an existing appointment as cancelled. The record remains available
     * for queries, while the freed slot becomes bookable again.
     */
    public synchronized ResultRet cancel(Long id) {
        Appointment appointment = store.get(id);
        if (appointment == null) {
            return ResultRet.errorResult("Appointment not found: " + id);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return ResultRet.okResult("Appointment cancelled successfully", appointment);
    }

    public synchronized ResultRet findAll() {
        List<Appointment> appointments = List.copyOf(store.values());
        return ResultRet.okResult(appointments);
    }
}
