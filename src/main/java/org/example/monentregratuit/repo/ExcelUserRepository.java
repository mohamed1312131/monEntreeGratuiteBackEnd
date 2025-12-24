package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.ExcelUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelUserRepository extends JpaRepository<ExcelUser, Long> {
    List<ExcelUser> findByFoireId(Long foireId);
    boolean existsByEmailAndFoireId(String email, Long foireId);
    void deleteByFoireId(Long foireId);
}
