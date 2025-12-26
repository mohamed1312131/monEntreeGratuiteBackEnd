package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.UserVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserVisitRepository extends JpaRepository<UserVisit, Long> {
    @Query("SELECT COUNT(u) FROM UserVisit u WHERE u.visitDate BETWEEN :start AND :end")
    long countByVisitDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.country, COUNT(u) FROM UserVisit u GROUP BY u.country ORDER BY COUNT(u) DESC")
    List<Object[]> countByCountry();

    @Query("SELECT u.country, COUNT(u) FROM UserVisit u WHERE YEAR(u.visitDate) = :year GROUP BY u.country ORDER BY COUNT(u) DESC")
    List<Object[]> countByCountryAndYear(@Param("year") int year);
}
