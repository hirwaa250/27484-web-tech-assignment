package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping (value = "/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentServ;

    @PostMapping(value="/save")
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment){
        String returnedMessage = appointmentServ.saveAppointment(appointment);
        if(returnedMessage.equals("Appointment saved successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("Doctor is already booked on that date")
                || returnedMessage.equals("Appointment already exists")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }else if(returnedMessage.equals("Appointment date cannot be in the past")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value="/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestParam AppointmentStatus status){
        String returnedMessage = appointmentServ.updateStatus(id, status);
        if(returnedMessage.equals("Appointment status updated successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("The appointment with that id does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping(value="/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable UUID id, @RequestParam String newDate){
        LocalDate date = LocalDate.parse(newDate);
        String returnedMessage = appointmentServ.rescheduleAppointment(id, date);
        if(returnedMessage.equals("Appointment rescheduled successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("The appointment with that id does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else if(returnedMessage.equals("Doctor is already booked on that date")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/by-status")
    public ResponseEntity<?> getAppointmentsByStatus(@RequestParam AppointmentStatus status){
        List<Appointment> appointments = appointmentServ.getAppointmentsByStatus(status);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value="/between")
    public ResponseEntity<?> getAppointmentsBetween(@RequestParam String start, @RequestParam String end){
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<Appointment> appointments = appointmentServ.getAppointmentsBetween(startDate, endDate);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value="/all")
    public ResponseEntity<?> getAllAppointments(){
        List<Appointment> appointments = appointmentServ.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value="/stats/by-status")
    public ResponseEntity<?> getAppointmentCountsByStatus(){
        List<Object[]> stats = appointmentServ.getAppointmentCountsByStatus();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @PatchMapping(value="/cancel-day")
    public ResponseEntity<?> cancelDoctorDay(@RequestParam UUID doctorId, @RequestParam String date){
        LocalDate day = LocalDate.parse(date);
        String returnedMessage = appointmentServ.cancelDoctorDay(doctorId, day);
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @GetMapping(value="/page")
    public ResponseEntity<?> getAppointmentsPage(@RequestParam int page, @RequestParam int size, @RequestParam String sort){
        String[] sortParts = sort.split(",");
        Sort.Direction direction = Sort.Direction.ASC;
        if(sortParts.length > 1){
            direction = Sort.Direction.fromString(sortParts[1].trim());
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0].trim()));
        Page<Appointment> appointments = appointmentServ.getAppointmentsPage(pageable);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @DeleteMapping(value="/cancelled-before")
    public ResponseEntity<?> deleteCancelledBefore(@RequestParam String date){
        LocalDate day = LocalDate.parse(date);
        String returnedMessage = appointmentServ.deleteCancelledBefore(day);
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id){
        Optional<Appointment> appointment = appointmentServ.getAppointmentById(id);
        if(appointment.isPresent()){
            return new ResponseEntity<>(appointment.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value="/update/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointment){
        String returnedMessage = appointmentServ.updateAppointment(id, appointment);
        if(returnedMessage.equals("Appointment updated successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("Doctor is already booked on that date")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value="/delete/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id){
        String returnedMessage = appointmentServ.deleteAppointment(id);
        if(returnedMessage.equals("Appointment deleted successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("That appointment has prescriptions, delete them first")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }
}
