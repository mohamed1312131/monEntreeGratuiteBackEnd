package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.EmailTemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailTemplateImageRepository extends JpaRepository<EmailTemplateImage, Long> {
    List<EmailTemplateImage> findByEmailTemplateIdOrderByImageOrderAsc(Long templateId);
    List<EmailTemplateImage> findByEmailTemplateId(Long templateId);
    void deleteByEmailTemplateId(Long templateId);
}
