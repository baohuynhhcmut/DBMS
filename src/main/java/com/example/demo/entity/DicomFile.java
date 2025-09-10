package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;

@Data
@Entity
@Table(name="DICOM_FILE",
        uniqueConstraints = @UniqueConstraint(name="UQ_FILE_BY_PATH", columnNames="FILE_PATH"))
public class DicomFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="FILE_ID")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SOP_INSTANCE_UID", nullable=false)
    private DicomInstance instance;

    @Column(name="FILE_NAME", length=255) private String fileName;
    @Column(name="FILE_EXT", length=16) private String fileExt;
    @Column(name="MIME_TYPE", length=128) private String mimeType;
    @Column(name="FILE_TYPE", length=64) private String fileType;
    @Column(name="FILE_SIZE_BYTES") private Long fileSizeBytes;
    @Column(name="FILE_DATE") private Timestamp fileDate;
    @Column(name="FILE_PATH", length=1024) private String filePath;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.BLOB)
    @Column(name="CONTENT")
    private byte[] content;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.BLOB)
    @Column(name="PREVIEW_PNG")
    private byte[] previewPng;

    @Column(name="PREVIEW_MIME", length=64)
    private String previewMime;
}
