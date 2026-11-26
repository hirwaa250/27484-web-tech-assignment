package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String savePatient(Patient patient){
        if(patientRepo.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirth(
                patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth())){
            return "Patient already exists";
        }
        patientRepo.save(patient);
        return "Patient saved successfully";
    }

    public List<Patient> getAllPatients(){
        return patientRepo.findAll();
    }

    public Optional<Patient> getPatientById(UUID id){
        return patientRepo.findById(id);
    }

    public String updatePatient(UUID id, Patient patient){
        Optional<Patient> existing = patientRepo.findById(id);
        if(existing.isEmpty()){
            return "Patient not found";
        }
        Patient toUpdate = existing.get();
        toUpdate.setFirstName(patient.getFirstName());
        toUpdate.setLastName(patient.getLastName());
        toUpdate.setDateOfBirth(patient.getDateOfBirth());
        patientRepo.save(toUpdate);
        return "Patient updated successfully";
    }

    public String deletePatient(UUID id){
        Optional<Patient> existing = patientRepo.findById(id);
        if(existing.isEmpty()){
            return "Patient not found";
        }
        if(!existing.get().getAppointments().isEmpty()){
            return "Patient has appointments";
        }
        patientRepo.deleteById(id);
        return "Patient deleted successfully";
    }

    public List<Patient> getPatientsByLastName(String lastName){
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public Optional<List<Patient>> getPatientsOfDoctor(UUID doctorId){
        if(!doctorRepo.existsById(doctorId)){
            return Optional.empty();
        }
        return Optional.of(patientRepo.findPatientsOfDoctor(doctorId));
    }

    public List<Patient> getFrequentPatients(long min){
        return patientRepo.findFrequentPatients(min);
    }

    public Optional<List<Appointment>> getUpcomingAppointments(String ssNumber){
        if(!patientRepo.existsBySsNumber(ssNumber)){
            return Optional.empty();
        }
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        return Optional.of(appointmentRepo.findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                ssNumber, LocalDate.now(), statuses));
    }
}
