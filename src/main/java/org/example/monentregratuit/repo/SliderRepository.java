package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Slider entities.
 */
@Repository
public interface SliderRepository extends JpaRepository<Slider, Long> {
    /**
     * Finds all sliders associated with the given foire ID.
     *
     * @param foireId the ID of the foire
     * @return a list of sliders associated with the given foire ID
     */
    List<Slider> findByFoireId(Long foireId);

    /**
     * Finds all sliders associated with the given foire ID and having the specified active status.
     *
     * @param foireId  the ID of the foire
     * @param isActive the active status of the sliders to find
     * @return a list of sliders associated with the given foire ID and having the specified active status
     */
    List<Slider> findByFoireIdAndIsActive(Long foireId, Boolean isActive);

    /**
     * Finds all sliders having the specified active status.
     *
     * @param isActive the active status of the sliders to find
     * @return a list of sliders having the specified active status
     */
    List<Slider> findByIsActive(Boolean isActive);
}