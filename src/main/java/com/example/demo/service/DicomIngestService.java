package com.example.demo.service;


import com.example.demo.entity.DicomFile;
import com.example.demo.entity.DicomInstance;
import com.example.demo.entity.DicomSeries;
import com.example.demo.entity.DicomStudy;
import com.example.demo.helper.DicomDicom4CheMapper;
import com.example.demo.repo.DicomFileRepo;
import com.example.demo.repo.DicomInstanceRepo;
import com.example.demo.repo.DicomSeriesRepo;
import com.example.demo.repo.DicomStudyRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DicomIngestService {
    private final DicomStudyRepo studyRepo;
    private final DicomSeriesRepo seriesRepo;
    private final DicomInstanceRepo instanceRepo;
    private final DicomFileRepo fileRepo;
    private final DicomDicom4CheMapper mapper;

    @Transactional
    public void ingest(Path root) throws Exception {
        try (var stream = Files.walk(root)) {
            List<Path> files = stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".ima"))
                    .collect(Collectors.toList());

//            if (!files.isEmpty()) {
//                Path p = files.get(0);
//                System.out.println(p);
//                processOne(p);
//            }

            for (Path p : files) {
                System.out.println(p.toString());
                processOne(p);
            }
        }
        System.out.println("Done");
    }

    private void processOne(Path p) throws Exception {
        File f = p.toFile();
        BasicFileAttributes fat = Files.readAttributes(p, BasicFileAttributes.class);

        try (DicomInputStream din = new DicomInputStream(f)) {
            Attributes ds = din.readDataset(-1, -1);

            // STUDY
            String studyUID = ds.getString(org.dcm4che3.data.Tag.StudyInstanceUID);
            DicomStudy study = studyRepo.findById(studyUID).orElseGet(DicomStudy::new);
            mapper.fillStudy(ds, study);
            studyRepo.save(study);

            // SERIES
            String seriesUID = ds.getString(org.dcm4che3.data.Tag.SeriesInstanceUID);
            DicomSeries series = seriesRepo.findById(seriesUID).orElseGet(DicomSeries::new);
            mapper.fillSeries(ds, series, study);
            seriesRepo.save(series);

            // INSTANCE
            String sopUID = ds.getString(org.dcm4che3.data.Tag.SOPInstanceUID);
            DicomInstance inst = instanceRepo.findById(sopUID).orElseGet(DicomInstance::new);
            mapper.fillInstance(ds, inst, series);
            instanceRepo.save(inst);

            // FILE (upsert theo path)
            DicomFile file = fileRepo.findByFilePath(p.toString()).orElseGet(DicomFile::new);
            file.setInstance(inst);
            file.setFileName(f.getName());
            String name = f.getName();
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')+1) : "";
            file.setFileExt(ext);
            String mime = Files.probeContentType(p);
            file.setMimeType(mime==null? "application/dicom" : mime);
            file.setFileType("DICOM");
            file.setFileSizeBytes(Files.size(p));
            file.setFileDate(new Timestamp(fat.lastModifiedTime().toMillis()));
            file.setFilePath(p.toString());


            byte[] raw = Files.readAllBytes(p);
            file.setContent(raw);

            byte[] png = tryRenderPng(raw);
            if (png != null && png.length > 0) {
                file.setPreviewPng(png);
                file.setPreviewMime("image/png");
            }

            fileRepo.save(file);
        }
    }


    private byte[] tryRenderPng(byte[] dicomBytes) {
        try {
            // Tìm ImageReader cho DICOM
            java.util.Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
            if (!readers.hasNext()) return null;
            ImageReader reader = readers.next();

            try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(dicomBytes))) {
                reader.setInput(iis, false, false);
                // đọc frame 0 (nếu multi-frame)
                BufferedImage frame0 = reader.read(0);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(frame0, "png", baos);
                return baos.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
