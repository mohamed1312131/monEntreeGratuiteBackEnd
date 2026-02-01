package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.entity.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoireRepository extends JpaRepository<Foire,Long> {
    List<Foire> findByCountryCode(CountryCode countryCode);
    List<Foire> findByCountryCodeAndIsActiveTrue(CountryCode countryCode);
    List<Foire> findByDateBetween(LocalDateTime start, LocalDateTime end);
    Optional<Foire> findByName(String name);
}
