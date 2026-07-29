package com.medsys.appointment.controller;

import com.medsys.appointment.entity.ResultRet;
import com.medsys.appointment.entity.Appointment;
import com.medsys.appointment.service.AppointmentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResultRet book(@RequestBody Appointment appointment) {
        return service.book(appointment.getDoctorId(), appointment.getPatientName(), appointment.getTime());
    }

    @DeleteMapping("/{id}")
    public ResultRet cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping
    public ResultRet findAll() {
        return service.findAll();
    }
}
