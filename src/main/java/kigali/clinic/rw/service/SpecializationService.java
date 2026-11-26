package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepo;

    public String saveSpecialization(Specialization specialization){
        if(specializationRepo.existsByNameIgnoreCase(specialization.getName())){
            return "Specialization already exists";
        }
        specializationRepo.save(specialization);
        return "Specialization saved successfully";
    }

    public List<Specialization> getAllSpecializations(){
        return specializationRepo.findAll();
    }

    public Optional<Specialization> getSpecializationById(UUID id){
        return specializationRepo.findById(id);
    }

    public String updateSpecialization(UUID id, Specialization specialization){
        Optional<Specialization> existing = specializationRepo.findById(id);
        if(existing.isEmpty()){
            return "Specialization not found";
        }
        Specialization toUpdate = existing.get();
        toUpdate.setName(specialization.getName());
        specializationRepo.save(toUpdate);
        return "Specialization updated successfully";
    }

    public String deleteSpecialization(UUID id){
        Optional<Specialization> existing = specializationRepo.findById(id);
        if(existing.isEmpty()){
            return "Specialization not found";
        }
        if(!existing.get().getDoctors().isEmpty()){
            return "Specialization is held by a doctor";
        }
        specializationRepo.deleteById(id);
        return "Specialization deleted successfully";
    }

    public List<Specialization> getUnusedSpecializations(){
        return specializationRepo.findUnusedSpecializations();
    }
}
