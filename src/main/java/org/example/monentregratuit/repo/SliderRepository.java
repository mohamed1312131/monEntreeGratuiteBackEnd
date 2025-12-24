package org.example.monentregratuit.repo;


import org.example.monentregratuit.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SliderRepository extends JpaRepository<Slider, Long> {
}