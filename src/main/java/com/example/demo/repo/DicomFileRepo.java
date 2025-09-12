package com.example.demo.repo;

import com.example.demo.entity.DicomFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DicomFileRepo extends JpaRepository<DicomFile, Long> {
    Optional<DicomFile> findByFilePath(String filePath);
    List<DicomFile> findAllByFilePathIn(Set<String> filePaths);
}