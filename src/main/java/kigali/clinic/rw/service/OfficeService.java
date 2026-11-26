package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepo;


    public String saveOffice(Office office){

          if(officeRepo.findByOfficeNumber(office.getOfficeNumber()) != null){
            return "Office is already exist";
          }
          officeRepo.save(office);

          return "Office saved successfully";
    }

    public List<Office> getAllOffices(){
        return officeRepo.findAll();
    }

    public Optional<Office> getOfficeById(UUID id){
        return officeRepo.findById(id);
    }

    public String updateOffice(UUID id, Office office){
        Optional<Office> existing = officeRepo.findById(id);
        if(existing.isEmpty()){
            return "Office not found";
        }
        Office sameNumber = officeRepo.findByOfficeNumber(office.getOfficeNumber());
        if(sameNumber != null && !sameNumber.getId().equals(id)){
            return "Office is already exist";
        }
        Office toUpdate = existing.get();
        toUpdate.setName(office.getName());
        toUpdate.setOfficeNumber(office.getOfficeNumber());
        officeRepo.save(toUpdate);
        return "Office updated successfully";
    }

    public String deleteOffice(UUID id){
        Optional<Office> existing = officeRepo.findById(id);
        if(existing.isEmpty()){
            return "Office not found";
        }
        if(existing.get().getDoctor() != null){
            return "Office is assigned to a doctor";
        }
        officeRepo.deleteById(id);
        return "Office deleted successfully";
    }

    public List<Office> getFreeOffices(){
        return officeRepo.findFreeOffices();
    }

    public Optional<Object[]> getBusiestOffice(){
        List<Object[]> rows = officeRepo.findBusiestOffice(PageRequest.of(0, 1));
        if(rows.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

}
