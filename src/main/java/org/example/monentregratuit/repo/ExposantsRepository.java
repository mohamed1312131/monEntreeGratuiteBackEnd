package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Exposant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExposantsRepository extends JpaRepository<Exposant,Long> {

}
