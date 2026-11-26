package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private OfficeRepository officeRepo;

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String saveDoctor(Doctor doctor){
        if(doctorRepo.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirth(
                doctor.getFirstName(), doctor.getLastName(), doctor.getDateOfBirth())){
            return "Doctor already exists";
        }
        String problem = loadOfficeAndSpecializations(doctor, null);
        if(problem != null){
            return problem;
        }
        doctorRepo.save(doctor);
        return "Doctor saved successfully";
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(UUID id){
        return doctorRepo.findById(id);
    }

    public String updateDoctor(UUID id, Doctor doctor){
        Optional<Doctor> existing = doctorRepo.findById(id);
        if(existing.isEmpty()){
            return "Doctor not found";
        }
        String problem = loadOfficeAndSpecializations(doctor, id);
        if(problem != null){
            return problem;
        }
        Doctor toUpdate = existing.get();
        toUpdate.setFirstName(doctor.getFirstName());
        toUpdate.setLastName(doctor.getLastName());
        toUpdate.setDateOfBirth(doctor.getDateOfBirth());
        toUpdate.setOffice(doctor.getOffice());
        toUpdate.setSpecializations(doctor.getSpecializations());
        doctorRepo.save(toUpdate);
        return "Doctor updated successfully";
    }

    public String deleteDoctor(UUID id){
        Optional<Doctor> existing = doctorRepo.findById(id);
        if(existing.isEmpty()){
            return "Doctor not found";
        }
        if(!existing.get().getAppointments().isEmpty()){
            return "Doctor has appointments";
        }
        doctorRepo.deleteById(id);
        return "Doctor deleted successfully";
    }

    public List<Doctor> getDoctorsBySpecialization(String name){
        return doctorRepo.findDoctorsBySpecializationName(name);
    }

    public List<Doctor> getDoctorsWithoutOffice(){
        return doctorRepo.findDoctorsWithoutOffice();
    }

    public String addSpecialization(UUID doctorId, UUID specializationId){
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specialization = specializationRepo.findById(specializationId);
        if(specialization.isEmpty()){
            return "That specialization does not exist";
        }
        Doctor toUpdate = doctor.get();
        if(toUpdate.getSpecializations().contains(specialization.get())){
            return "The doctor already has that specialization";
        }
        toUpdate.getSpecializations().add(specialization.get());
        doctorRepo.save(toUpdate);
        return "Specialization added to the doctor";
    }

    public String removeSpecialization(UUID doctorId, UUID specializationId){
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specialization = specializationRepo.findById(specializationId);
        if(specialization.isEmpty()){
            return "That specialization does not exist";
        }
        Doctor toUpdate = doctor.get();
        if(!toUpdate.getSpecializations().contains(specialization.get())){
            return "The doctor does not have that specialization";
        }
        toUpdate.getSpecializations().remove(specialization.get());
        doctorRepo.save(toUpdate);
        return "Specialization removed from the doctor";
    }

    public String changeOffice(UUID doctorId, int officeNumber){
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }
        Office office = officeRepo.findByOfficeNumber(officeNumber);
        if(office == null){
            return "The office with that office number does not exist";
        }
        if(doctorRepo.existsByOfficeOfficeNumberAndIdNot(officeNumber, doctorId)){
            return "That office is taken by other doctor";
        }
        Doctor toUpdate = doctor.get();
        toUpdate.setOffice(office);
        doctorRepo.save(toUpdate);
        return "Office changed successfully";
    }

    public Optional<List<Appointment>> getAgenda(UUID doctorId, LocalDate date){
        if(!doctorRepo.existsById(doctorId)){
            return Optional.empty();
        }
        return Optional.of(appointmentRepo.findByDoctorIdAndAppointmentDateAndStatusNot(doctorId, date, AppointmentStatus.CANCELLED));
    }

    public List<Doctor> searchDoctorsByName(String name){
        return doctorRepo.searchDoctorsByName(name);
    }
    public List<Object[]> getDoctorWorkload(LocalDate start, LocalDate end) {
    return doctorRepo.findDoctorWorkload(start, end);
    } 
    private String loadOfficeAndSpecializations(Doctor doctor, UUID doctorId){
        if(doctor.getOffice() != null){
            if(doctor.getOffice().getId() == null){
                return "Office not found";
            }
            Optional<Office> office = officeRepo.findById(doctor.getOffice().getId());
            if(office.isEmpty()){
                return "Office not found";
            }
            Doctor holder = office.get().getDoctor();
            if(holder != null && !holder.getId().equals(doctorId)){
                return "Office is already assigned to another doctor";
            }
            doctor.setOffice(office.get());
        }

        List<Specialization> specializations = new ArrayList<>();
        if(doctor.getSpecializations() != null){
            for(Specialization s : doctor.getSpecializations()){
                if(s == null || s.getId() == null){
                    return "Specialization not found";
                }
                Optional<Specialization> found = specializationRepo.findById(s.getId());
                if(found.isEmpty()){
                    return "Specialization not found";
                }
                specializations.add(found.get());
            }
        }
        doctor.setSpecializations(specializations);
        return null;
    }
}
