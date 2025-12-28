package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.AboutUsQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AboutUsQARepository extends JpaRepository<AboutUsQA, Long> {
    List<AboutUsQA> findByAboutUsIdOrderByDisplayOrderAsc(Long aboutUsId);
}
