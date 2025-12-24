package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
