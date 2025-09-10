package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name="DICOM_SERIES",
        indexes = {@Index(name="IDX_SERIES_STUDY", columnList="STUDY_INSTANCE_UID")})
public class DicomSeries {
    @Id
    @Column(name="SERIES_INSTANCE_UID", length=128)
    private String seriesInstanceUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="STUDY_INSTANCE_UID", nullable=false)
    private DicomStudy study;

    @Column(name="SERIES_NUMBER") private Integer seriesNumber;
    @Column(name="SERIES_DESCRIPTION", length=256) private String seriesDescription;
    @Column(name="MODALITY", length=16) private String modality;
    @Column(name="MANUFACTURER", length=64) private String manufacturer;
    @Column(name="MANUFACTURER_MODEL_NAME", length=128) private String manufacturerModelName;
    @Column(name="SOFTWARE_VERSIONS", length=128) private String softwareVersions;
    @Column(name="BODY_PART_EXAMINED", length=64) private String bodyPartExamined;
    @Column(name="SCANNING_SEQUENCE", length=64) private String scanningSequence;
    @Column(name="SEQUENCE_VARIANT", length=64) private String sequenceVariant;
    @Column(name="SCAN_OPTIONS", length=64) private String scanOptions;
    @Column(name="MR_ACQUISITION_TYPE", length=16) private String mrAcquisitionType;
    @Column(name="SEQUENCE_NAME", length=64) private String sequenceName;
    @Column(name="ANGIO_FLAG", length=8) private String angioFlag;
    @Column(name="TRANSMIT_COIL_NAME", length=64) private String transmitCoilName;
    @Column(name="IN_PLANE_PHASE_ENC_DIRECTION", length=16) private String inPlanePhaseEncDirection;
    @Column(name="VARIABLE_FLIP_ANGLE_FLAG", length=8) private String variableFlipAngleFlag;
    @Column(name="PATIENT_POSITION", length=8) private String patientPosition;

    @Column(name="SERIES_DATE_RAW", length=16) private String seriesDateRaw;
    @Column(name="SERIES_TIME_RAW", length=32) private String seriesTimeRaw;
    @Column(name="SERIES_DATETIME") private Timestamp seriesDateTime;

    @Column(name="MAGNETIC_FIELD_STRENGTH") private Double magneticFieldStrength;
    @Column(name="IMAGING_FREQUENCY") private Double imagingFrequency;
    @Column(name="PERCENT_SAMPLING") private Double percentSampling;
    @Column(name="PERCENT_PHASE_FOV") private Double percentPhaseFov;
    @Column(name="PIXEL_BANDWIDTH") private Double pixelBandwidth;
    @Column(name="FLIP_ANGLE") private Double flipAngle;

    @Column(name="FRAME_OF_REFERENCE_UID", length=128) private String frameOfReferenceUid;
}
