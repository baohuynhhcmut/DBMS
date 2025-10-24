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
import org.springframework.scheduling.annotation.Async;
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
import java.util.*;
import java.util.concurrent.*;
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

            int batchSize = 50; // Reduced batch size to lower memory footprint
            ExecutorService executor = Executors.newFixedThreadPool(
                    Math.min(Runtime.getRuntime().availableProcessors(), 4)
            );

            try {
                for (int i = 0; i < 2; i += batchSize) {
                    List<Path> batch = files.subList(i, Math.min(i + batchSize, files.size()));
                    processBatch(batch, executor);
                    System.gc(); // Suggest GC to reclaim memory between batches
                }
            } finally {
                executor.shutdown();
                executor.awaitTermination(30, TimeUnit.MINUTES);
            }
            System.out.println("Ingestion completed");
        }
    }

    @Transactional
    protected void processBatch(List<Path> files, ExecutorService executor) throws Exception {
        // Preload only necessary entities
        Map<String, DicomStudy> studyCache = new ConcurrentHashMap<>();
        Map<String, DicomSeries> seriesCache = new ConcurrentHashMap<>();
        Map<String, DicomInstance> instanceCache = new ConcurrentHashMap<>();
        Map<String, DicomFile> fileCache = new ConcurrentHashMap<>();

        // Preload entities in smaller chunks
        preloadEntities(files, studyCache, seriesCache, instanceCache, fileCache);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Path p : files) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    processOne(p, studyCache, seriesCache, instanceCache, fileCache);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all tasks in the batch to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Save entities incrementally
        studyRepo.saveAll(studyCache.values());
        seriesRepo.saveAll(seriesCache.values());
        instanceRepo.saveAll(instanceCache.values());
        fileRepo.saveAll(fileCache.values());

        // Clear caches to free memory
        studyCache.clear();
        seriesCache.clear();
        instanceCache.clear();
        fileCache.clear();
    }

    private void preloadEntities(List<Path> files, Map<String, DicomStudy> studyCache,
                                 Map<String, DicomSeries> seriesCache, Map<String, DicomInstance> instanceCache,
                                 Map<String, DicomFile> fileCache) throws Exception {
        Set<String> studyUIDs = new HashSet<>();
        Set<String> seriesUIDs = new HashSet<>();
        Set<String> sopUIDs = new HashSet<>();
        Set<String> filePaths = new HashSet<>();

        // Collect UIDs and file paths
        for (Path p : files) {
            try (DicomInputStream din = new DicomInputStream(p.toFile())) {
                Attributes ds = din.readDataset(-1, -1);
                studyUIDs.add(ds.getString(org.dcm4che3.data.Tag.StudyInstanceUID));
                seriesUIDs.add(ds.getString(org.dcm4che3.data.Tag.SeriesInstanceUID));
                sopUIDs.add(ds.getString(org.dcm4che3.data.Tag.SOPInstanceUID));
                filePaths.add(p.toString());
            }
        }

        // Preload entities
        studyCache.putAll(studyRepo.findAllById(studyUIDs).stream()
                .collect(Collectors.toMap(DicomStudy::getStudyInstanceUid, s -> s)));
        seriesCache.putAll(seriesRepo.findAllById(seriesUIDs).stream()
                .collect(Collectors.toMap(DicomSeries::getSeriesInstanceUid, s -> s)));
        instanceCache.putAll(instanceRepo.findAllById(sopUIDs).stream()
                .collect(Collectors.toMap(DicomInstance::getSopInstanceUid, i -> i)));
        fileCache.putAll(fileRepo.findAllByFilePathIn(filePaths).stream()
                .collect(Collectors.toMap(DicomFile::getFilePath, f -> f)));
    }

    private void processOne(Path p, Map<String, DicomStudy> studyCache, Map<String, DicomSeries> seriesCache,
                            Map<String, DicomInstance> instanceCache, Map<String, DicomFile> fileCache) throws Exception {
        File f = p.toFile();
        System.out.println("Processing file: " + p.toString());
        BasicFileAttributes fat = Files.readAttributes(p, BasicFileAttributes.class);

        Attributes ds;
        try (DicomInputStream din = new DicomInputStream(f)) {
            ds = din.readDataset(-1, -1);
        }

        // STUDY
        String studyUID = ds.getString(org.dcm4che3.data.Tag.StudyInstanceUID);
        DicomStudy study = studyCache.computeIfAbsent(studyUID, k -> new DicomStudy());
        mapper.fillStudy(ds, study);

        // SERIES
        String seriesUID = ds.getString(org.dcm4che3.data.Tag.SeriesInstanceUID);
        DicomSeries series = seriesCache.computeIfAbsent(seriesUID, k -> new DicomSeries());
        mapper.fillSeries(ds, series, study);

        // INSTANCE
        String sopUID = ds.getString(org.dcm4che3.data.Tag.SOPInstanceUID);
        DicomInstance inst = instanceCache.computeIfAbsent(sopUID, k -> new DicomInstance());
        mapper.fillInstance(ds, inst, series);

        // FILE
        String filePath = p.toString();
        DicomFile file = fileCache.computeIfAbsent(filePath, k -> new DicomFile());
        file.setInstance(inst);
        file.setFileName(f.getName());
        String name = f.getName();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        file.setFileExt(ext);
        String mime = Files.probeContentType(p);
        file.setMimeType(mime == null ? "application/dicom" : mime);
        file.setFileType("DICOM");
        file.setFileSizeBytes(Files.size(p));
        file.setFileDate(new Timestamp(fat.lastModifiedTime().toMillis()));
        file.setFilePath(filePath);

        // Avoid loading entire file into memory
        file.setContent(null); // Store file path instead of content, or use a streaming approach if needed

        byte[] png = tryRenderPngAsync(filePath).join(); // Use async rendering
        if (png != null && png.length > 0) {
            file.setPreviewPng(png);
            file.setPreviewMime("image/png");
        }
    }

    @Async
    public CompletableFuture<byte[]> tryRenderPngAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return tryRenderPng(Files.readAllBytes(Path.of(filePath)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private byte[] tryRenderPng(byte[] dicomBytes) {
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
            if (!readers.hasNext()) return null;
            ImageReader reader = readers.next();

            try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(dicomBytes))) {
                reader.setInput(iis, false, false);
                BufferedImage frame0 = reader.read(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(frame0, "png", baos);
                return baos.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            return null;
        }
    }
}