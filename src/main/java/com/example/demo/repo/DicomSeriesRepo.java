package com.example.demo.repo;

import com.example.demo.entity.DicomSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DicomSeriesRepo extends JpaRepository<DicomSeries, String> { }
