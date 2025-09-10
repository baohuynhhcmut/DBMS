package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name="DICOM_INSTANCE",
        indexes = {@Index(name="IDX_INSTANCE_SERIES", columnList="SERIES_INSTANCE_UID")})
public class DicomInstance {
    @Id
    @Column(name="SOP_INSTANCE_UID", length=128)
    private String sopInstanceUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SERIES_INSTANCE_UID", nullable=false)
    private DicomSeries series;

    @Column(name="SOP_CLASS_UID", length=128) private String sopClassUid;
    @Column(name="INSTANCE_NUMBER") private Integer instanceNumber;

    @Column(name="INSTANCE_CREATION_DATE_RAW", length=16) private String instanceCreationDateRaw;
    @Column(name="INSTANCE_CREATION_TIME_RAW", length=32) private String instanceCreationTimeRaw;
    @Column(name="INSTANCE_CREATION_TS") private Timestamp instanceCreationTs;

    @Column(name="ACQUISITION_DATE_RAW", length=16) private String acquisitionDateRaw;
    @Column(name="ACQUISITION_TIME_RAW", length=32) private String acquisitionTimeRaw;
    @Column(name="CONTENT_DATE_RAW", length=16) private String contentDateRaw;
    @Column(name="CONTENT_TIME_RAW", length=32) private String contentTimeRaw;

    @Column(name="IMAGE_TYPE", length=128) private String imageType;
    @Column(name="PHOTOMETRIC_INTERPRETATION", length=32) private String photometricInterpretation;
    @Column(name="SAMPLES_PER_PIXEL") private Integer samplesPerPixel;

    @Column(name="PIXELS_ROWS") private Integer rows;
    @Column(name="PIXELS_COLS") private Integer cols;
    @Column(name="BITS_ALLOCATED") private Integer bitsAllocated;
    @Column(name="BITS_STORED") private Integer bitsStored;
    @Column(name="HIGH_BIT") private Integer highBit;
    @Column(name="PIXEL_REPRESENTATION") private Integer pixelRepresentation;

    @Column(name="SLICE_THICKNESS") private Double sliceThickness;
    @Column(name="SPACING_BETWEEN_SLICES") private Double spacingBetweenSlices;
    @Column(name="PIXEL_SPACING_ROW") private Double pixelSpacingRow;
    @Column(name="PIXEL_SPACING_COL") private Double pixelSpacingCol;

    @Column(name="IMAGE_POSITION_X") private Double imagePositionX;
    @Column(name="IMAGE_POSITION_Y") private Double imagePositionY;
    @Column(name="IMAGE_POSITION_Z") private Double imagePositionZ;

    @Column(name="IOP1") private Double iop1; @Column(name="IOP2") private Double iop2;
    @Column(name="IOP3") private Double iop3; @Column(name="IOP4") private Double iop4;
    @Column(name="IOP5") private Double iop5; @Column(name="IOP6") private Double iop6;

    @Column(name="WINDOW_CENTER") private Double windowCenter;
    @Column(name="WINDOW_WIDTH") private Double windowWidth;
    @Column(name="WINDOW_EXPLANATION", length=64) private String windowExplanation;

    @Column(name="ECHO_TIME") private Double echoTime;
    @Column(name="REPETITION_TIME") private Double repetitionTime;
    @Column(name="ECHO_TRAIN_LENGTH") private Integer echoTrainLength;
    @Column(name="NUMBER_OF_AVERAGES") private Double numberOfAverages;
    @Column(name="ECHO_NUMBER") private Integer echoNumber;
    @Column(name="SLICE_LOCATION") private Double sliceLocation;
}
