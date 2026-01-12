package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.TemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateImageRepository extends JpaRepository<TemplateImage, Long> {
}
