package kigali.clinic.rw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

@Repository 
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDate start, LocalDate end);

    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate appointmentDate, AppointmentStatus status);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(UUID patientId, UUID doctorId, LocalDate appointmentDate);

    boolean existsByDoctorIdAndAppointmentDateAndStatusNotAndIdNot(UUID doctorId, LocalDate appointmentDate, AppointmentStatus status, UUID id);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate appointmentDate, AppointmentStatus status);

    List<Appointment> findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(String ssNumber, LocalDate date, List<AppointmentStatus> statuses);

    long countByAppointmentDate(LocalDate appointmentDate);

    long countByStatus(AppointmentStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Appointment a SET a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED "
         + "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date "
         + "AND a.status <> kigali.clinic.rw.domain.AppointmentStatus.COMPLETED")
    int cancelDoctorDay(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Appointment a WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED "
         + "AND a.appointmentDate < :date")
    int deleteCancelledBefore(@Param("date") LocalDate date);
}
