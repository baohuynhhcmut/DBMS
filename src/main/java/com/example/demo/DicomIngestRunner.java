package com.example.demo;

import com.example.demo.service.DicomIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DicomIngestRunner implements CommandLineRunner {
    private final DicomIngestService service;

    @Value("${dicom.root}")
    private String dicomRoot;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting dicom ingest");
        service.ingest(Path.of(dicomRoot));
    }
}
