package com.example.demo.repo;

import com.example.demo.entity.DicomFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DicomFileRepo extends JpaRepository<DicomFile, Long> {
    Optional<DicomFile> findByFilePath(String filePath);
}