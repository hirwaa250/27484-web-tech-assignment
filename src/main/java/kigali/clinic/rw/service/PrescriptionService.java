package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String savePrescription(UUID appointmentId, Prescription prescription){
        Optional<Appointment> appointment = appointmentRepo.findById(appointmentId);
        if(appointment.isEmpty()){
            return "The appointment with that id does not exist";
        }
        if(appointment.get().getStatus() != AppointmentStatus.COMPLETED){
            return "Prescriptions are only for COMPLETED appointments";
        }
        prescription.setId(null);
        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment.get());
        prescriptionRepo.save(prescription);
        return "Prescription saved successfully";
    }

    public Optional<List<Prescription>> getPrescriptionsOfAppointment(UUID appointmentId){
        Optional<Appointment> appointment = appointmentRepo.findById(appointmentId);
        if(appointment.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(prescriptionRepo.findByAppointment(appointment.get()));
    }

    public List<Prescription> getPrescriptionsOfPatient(String ssNumber){
        return prescriptionRepo.findPrescriptionsOfPatient(ssNumber);
    }

    public String updatePrescription(UUID id, Prescription prescription){
        Optional<Prescription> existing = prescriptionRepo.findById(id);
        if(existing.isEmpty()){
            return "The prescription with that id does not exist";
        }
        Prescription toUpdate = existing.get();
        toUpdate.setMedicineName(prescription.getMedicineName());
        toUpdate.setDosage(prescription.getDosage());
        toUpdate.setDurationDays(prescription.getDurationDays());
        prescriptionRepo.save(toUpdate);
        return "Prescription updated successfully";
    }
    public List<Object[]> getTopMedicines() {
    return prescriptionRepo.findTopMedicines();
    }   
    public String deletePrescription(UUID id){
        if(!prescriptionRepo.existsById(id)){
            return "The prescription with that id does not exist";
        }
        prescriptionRepo.deleteById(id);
        return "Prescription deleted successfully";
        
    }
}
