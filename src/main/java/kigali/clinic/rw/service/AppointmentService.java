package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    public String saveAppointment(Appointment appointment){
        if(appointment.getAppointmentDate().isBefore(LocalDate.now())){
            return "Appointment date cannot be in the past";
        }
        String problem = loadPatientAndDoctor(appointment);
        if(problem != null){
            return problem;
        }
        boolean duplicate = appointmentRepo.existsByPatientIdAndDoctorIdAndAppointmentDate(
                appointment.getPatient().getId(), appointment.getDoctor().getId(), appointment.getAppointmentDate());
        if(duplicate){
            return "Appointment already exists";
        }
        boolean alreadyBooked = appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(
                appointment.getDoctor().getId(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED);
        if(alreadyBooked){
            return "Doctor is already booked on that date";
        }
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(appointment);
        return "Appointment saved successfully";
    }

    public List<Appointment> getAllAppointments(){
        return appointmentRepo.findAll();
    }

    public Optional<Appointment> getAppointmentById(UUID id){
        return appointmentRepo.findById(id);
    }

    public String updateAppointment(UUID id, Appointment appointment){
        Optional<Appointment> existing = appointmentRepo.findById(id);
        if(existing.isEmpty()){
            return "Appointment not found";
        }
        String problem = loadPatientAndDoctor(appointment);
        if(problem != null){
            return problem;
        }
        boolean alreadyBooked = appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNotAndIdNot(
                appointment.getDoctor().getId(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED, id);
        if(alreadyBooked){
            return "Doctor is already booked on that date";
        }
        Appointment toUpdate = existing.get();
        toUpdate.setAppointmentDate(appointment.getAppointmentDate());
        toUpdate.setReason(appointment.getReason());
        toUpdate.setPatient(appointment.getPatient());
        toUpdate.setDoctor(appointment.getDoctor());
        appointmentRepo.save(toUpdate);
        return "Appointment updated successfully";
    }

    public String deleteAppointment(UUID id){
        if(!appointmentRepo.existsById(id)){
            return "Appointment not found";
        }
        if(prescriptionRepo.existsByAppointmentId(id)){
            return "That appointment has prescriptions, delete them first";
        }
        appointmentRepo.deleteById(id);
        return "Appointment deleted successfully";
    }

    public String updateStatus(UUID id, AppointmentStatus status){
        Optional<Appointment> existing = appointmentRepo.findById(id);
        if(existing.isEmpty()){
            return "The appointment with that id does not exist";
        }
        Appointment toUpdate = existing.get();
        AppointmentStatus current = toUpdate.getStatus();
        boolean allowed = false;
        if(current == AppointmentStatus.SCHEDULED){
            allowed = status == AppointmentStatus.CONFIRMED || status == AppointmentStatus.CANCELLED;
        }else if(current == AppointmentStatus.CONFIRMED){
            allowed = status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED;
        }
        if(!allowed){
            return "Cannot change status from " + current + " to " + status;
        }
        toUpdate.setStatus(status);
        appointmentRepo.save(toUpdate);
        return "Appointment status updated successfully";
    }

    public String rescheduleAppointment(UUID id, LocalDate newDate){
        Optional<Appointment> existing = appointmentRepo.findById(id);
        if(existing.isEmpty()){
            return "The appointment with that id does not exist";
        }
        Appointment toUpdate = existing.get();
        if(toUpdate.getStatus() == AppointmentStatus.COMPLETED){
            return "A COMPLETED appointment cannot be rescheduled";
        }
        if(toUpdate.getStatus() == AppointmentStatus.CANCELLED){
            return "A CANCELLED appointment cannot be rescheduled";
        }
        if(newDate.isBefore(LocalDate.now())){
            return "Appointment date cannot be in the past";
        }
        boolean alreadyBooked = appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNotAndIdNot(
                toUpdate.getDoctor().getId(), newDate, AppointmentStatus.CANCELLED, id);
        if(alreadyBooked){
            return "Doctor is already booked on that date";
        }
        toUpdate.setAppointmentDate(newDate);
        toUpdate.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(toUpdate);
        return "Appointment rescheduled successfully";
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status){
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end){
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    public List<Object[]> getAppointmentCountsByStatus(){
        return appointmentRepo.countAppointmentsByStatus();
    }

    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date){
        int cancelled = appointmentRepo.cancelDoctorDay(doctorId, date);
        return cancelled + " appointments cancelled";
    }

    public Page<Appointment> getAppointmentsPage(Pageable pageable){
        return appointmentRepo.findAll(pageable);
    }

    @Transactional
    public String deleteCancelledBefore(LocalDate date){
        int deleted = appointmentRepo.deleteCancelledBefore(date);
        return deleted + " appointments deleted";
    }

    private String loadPatientAndDoctor(Appointment appointment){
        if(appointment.getPatient() == null || appointment.getPatient().getId() == null){
            return "Patient not found";
        }
        if(appointment.getDoctor() == null || appointment.getDoctor().getId() == null){
            return "Doctor not found";
        }
        Optional<Patient> patient = patientRepo.findById(appointment.getPatient().getId());
        if(patient.isEmpty()){
            return "Patient not found";
        }
        Optional<Doctor> doctor = doctorRepo.findById(appointment.getDoctor().getId());
        if(doctor.isEmpty()){
            return "Doctor not found";
        }
        appointment.setPatient(patient.get());
        appointment.setDoctor(doctor.get());
        return null;
    }
}
