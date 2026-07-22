package com.ritnesh.careerpilot.repository;

import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser(User user);

}