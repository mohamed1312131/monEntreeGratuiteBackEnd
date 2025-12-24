package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByIsActiveTrue();
    List<Video> findAllByOrderByCreatedAtDesc();
}
