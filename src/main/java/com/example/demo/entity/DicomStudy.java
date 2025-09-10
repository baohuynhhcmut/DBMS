package com.example.demo.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name="DICOM_STUDY")
public class DicomStudy {
    @Id
    @Column(name="STUDY_INSTANCE_UID", length=128)
    private String studyInstanceUid;

    @Column(name="STUDY_ID", length=64) private String studyId;
    @Column(name="ACCESSION_NUMBER", length=64) private String accessionNumber;
    @Column(name="STUDY_DESCRIPTION", length=256) private String studyDescription;
    @Column(name="STUDY_DATE_RAW", length=16) private String studyDateRaw;
    @Column(name="STUDY_TIME_RAW", length=32) private String studyTimeRaw;
    @Column(name="STUDY_DATETIME") private Timestamp studyDateTime;

    @Column(name="PATIENT_ID", length=64) private String patientId;
    @Column(name="PATIENT_NAME", length=256) private String patientName;
    @Column(name="PATIENT_SEX", length=8) private String patientSex;
    @Column(name="PATIENT_BIRTH_DATE_RAW", length=16) private String patientBirthDateRaw;
    @Column(name="PATIENT_AGE", length=8) private String patientAge;
    @Column(name="PATIENT_SIZE_M") private Double patientSizeM;
    @Column(name="PATIENT_WEIGHT_KG") private Double patientWeightKg;
    @Column(name="PATIENT_IDENTITY_REMOVED", length=8) private String patientIdentityRemoved;
    @Column(name="DEIDENTIFICATION_METHOD", length=256) private String deidentification;
}

