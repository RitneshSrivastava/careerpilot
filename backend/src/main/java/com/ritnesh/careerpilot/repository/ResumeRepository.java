package com.ritnesh.careerpilot.repository;

import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser(User user);

    Page<Resume> findByUserAndDeletedFalse(User user, Pageable pageable);

    Optional<Resume> findByIdAndDeletedFalse(Long id);

}
