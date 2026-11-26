package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping (value = "/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorServ;

    @PostMapping(value="/save")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor){
        String returnedMessage = doctorServ.saveDoctor(doctor);
        if(returnedMessage.equals("Doctor saved successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.endsWith("not found")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value="/all")
    public ResponseEntity<?> getAllDoctors(){
        List<Doctor> doctors = doctorServ.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value="/by-specialization")
    public ResponseEntity<?> getDoctorsBySpecialization(@RequestParam String name){
        List<Doctor> doctors = doctorServ.getDoctorsBySpecialization(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value="/without-office")
    public ResponseEntity<?> getDoctorsWithoutOffice(){
        List<Doctor> doctors = doctorServ.getDoctorsWithoutOffice();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value="/search")
    public ResponseEntity<?> searchDoctorsByName(@RequestParam String name){
        List<Doctor> doctors = doctorServ.searchDoctorsByName(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    

    @GetMapping(value = "/workload", name = "getDoctorWorkload")
    public ResponseEntity<?> getDoctorWorkload(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

    if (start.isAfter(end)) {
        return new ResponseEntity<>(
                "Start date cannot be after end date",
                HttpStatus.BAD_REQUEST);
    }

    List<Object[]> workload = doctorServ.getDoctorWorkload(start, end);

    return new ResponseEntity<>(workload, HttpStatus.OK);
}

    @GetMapping(value="/{doctorId}/agenda")
    public ResponseEntity<?> getAgenda(@PathVariable UUID doctorId, @RequestParam String date){
        LocalDate day = LocalDate.parse(date);
        Optional<List<Appointment>> appointments = doctorServ.getAgenda(doctorId, day);
        if(appointments.isPresent()){
            return new ResponseEntity<>(appointments.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value="/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> addSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId){
        String returnedMessage = doctorServ.addSpecialization(doctorId, specializationId);
        if(returnedMessage.equals("Specialization added to the doctor")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.endsWith("does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value="/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> removeSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId){
        String returnedMessage = doctorServ.removeSpecialization(doctorId, specializationId);
        if(returnedMessage.equals("Specialization removed from the doctor")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.endsWith("does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @PatchMapping(value="/{doctorId}/office")
    public ResponseEntity<?> changeOffice(@PathVariable UUID doctorId, @RequestParam int officeNumber){
        String returnedMessage = doctorServ.changeOffice(doctorId, officeNumber);
        if(returnedMessage.equals("Office changed successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.endsWith("does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id){
        Optional<Doctor> doctor = doctorServ.getDoctorById(id);
        if(doctor.isPresent()){
            return new ResponseEntity<>(doctor.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value="/update/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor){
        String returnedMessage = doctorServ.updateDoctor(id, doctor);
        if(returnedMessage.equals("Doctor updated successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.endsWith("not found")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value="/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id){
        String returnedMessage = doctorServ.deleteDoctor(id);
        if(returnedMessage.equals("Doctor deleted successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("Doctor not found")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }
}
