package com.example.demo.repo;

import com.example.demo.entity.DicomStudy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DicomStudyRepo extends JpaRepository<DicomStudy, String> { }