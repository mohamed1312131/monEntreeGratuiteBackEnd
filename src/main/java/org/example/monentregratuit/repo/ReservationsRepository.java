package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.CountryCode;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationsRepository extends JpaRepository<Reservations, Long> {
    @Query("""
        SELECT f.countryCode, COUNT(r)
        FROM Reservations r
        JOIN r.foire f
        WHERE YEAR(r.reservationDate) = :year
        GROUP BY f.countryCode
    """)
    List<Object[]> countReservationsByCountryAndYear(@Param("year") int year);

    @Query("SELECT r FROM Reservations r WHERE YEAR(r.reservationDate) = :year AND r.foire.countryCode = :country")
    List<Reservations> findByYearAndCountryCode(@Param("year") int year, @Param("country") CountryCode country);

    long countByFoireId(Long foireId);

    @Query("SELECT COUNT(r) FROM Reservations r WHERE r.reservationDate BETWEEN :start AND :end")
    long countByReservationDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Reservations> findByFoireId(Long foireId);

    @Query("SELECT r.ageCategory, COUNT(r) FROM Reservations r WHERE r.foire.id = :foireId GROUP BY r.ageCategory")
    List<Object[]> countByFoireIdGroupByAgeCategory(Long foireId);

    @Query("SELECT r.status, COUNT(r) FROM Reservations r WHERE r.foire.id = :foireId GROUP BY r.status")
    List<Object[]> countByFoireIdGroupByStatus(Long foireId);

    // Check if reservation exists with same email for a specific foire
    boolean existsByFoireIdAndEmail(Long foireId, String email);
    
    // Check if reservation exists with same phone for a specific foire
    boolean existsByFoireIdAndPhone(Long foireId, String phone);

    // Count reservations by status
    long countByStatus(ReservationStatus status);

}
