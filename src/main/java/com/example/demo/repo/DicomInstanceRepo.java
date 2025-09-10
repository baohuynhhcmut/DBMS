package com.example.demo.repo;

import com.example.demo.entity.DicomInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DicomInstanceRepo extends JpaRepository<DicomInstance, String> { }