package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.AboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AboutUsRepository extends JpaRepository<AboutUs, Long> {
    List<AboutUs> findByIsActiveTrue();
    List<AboutUs> findAllByOrderByCreatedAtDesc();
}
