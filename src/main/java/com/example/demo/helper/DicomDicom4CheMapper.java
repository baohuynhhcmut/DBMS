package com.example.demo.helper;


import com.example.demo.entity.DicomInstance;
import com.example.demo.entity.DicomSeries;
import com.example.demo.entity.DicomStudy;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class DicomDicom4CheMapper {
    public Timestamp toTimestamp(String da, String tm) {
        if (da == null || da.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(da, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalTime t = (tm == null || tm.isBlank()) ? LocalTime.MIDNIGHT
                    : LocalTime.parse(tm.replace(":", "").replace(" ", ""),
                    DateTimeFormatter.ofPattern("HHmmss[.SSSSSS]"));
            return Timestamp.valueOf(LocalDateTime.of(d, t));
        } catch (Exception e) { return null; }
    }
    private Double getDouble(Attributes ds, int tag){ try{
        String s=ds.getString(tag); return (s==null||s.isBlank())?null:Double.valueOf(s);}catch(Exception e){return null;}}
    private Integer getInt(Attributes ds, int tag){ try{
        String s=ds.getString(tag); return (s==null||s.isBlank())?null:Integer.valueOf(s);}catch(Exception e){return null;}}

    public void fillStudy(Attributes ds, DicomStudy st){
        st.setStudyInstanceUid(ds.getString(Tag.StudyInstanceUID));
        st.setStudyId(ds.getString(Tag.StudyID));
        st.setAccessionNumber(ds.getString(Tag.AccessionNumber));
        st.setStudyDescription(ds.getString(Tag.StudyDescription));
        st.setStudyDateRaw(ds.getString(Tag.StudyDate));
        st.setStudyTimeRaw(ds.getString(Tag.StudyTime));
        st.setStudyDateTime(toTimestamp(st.getStudyDateRaw(), st.getStudyTimeRaw()));
        st.setPatientId(ds.getString(Tag.PatientID));
        st.setPatientName(ds.getString(Tag.PatientName));
        st.setPatientSex(ds.getString(Tag.PatientSex));
        st.setPatientBirthDateRaw(ds.getString(Tag.PatientBirthDate));
        st.setPatientAge(ds.getString(Tag.PatientAge));
        st.setPatientSizeM(getDouble(ds, Tag.PatientSize));
        st.setPatientWeightKg(getDouble(ds, Tag.PatientWeight));
        st.setPatientIdentityRemoved(ds.getString(Tag.PatientIdentityRemoved));
        st.setDeidentification(ds.getString(Tag.DeidentificationMethod));
    }

    public void fillSeries(Attributes ds, DicomSeries se, DicomStudy st){
        se.setSeriesInstanceUid(ds.getString(Tag.SeriesInstanceUID));
        se.setStudy(st);
        se.setSeriesNumber(getInt(ds, Tag.SeriesNumber));
        se.setSeriesDescription(ds.getString(Tag.SeriesDescription));
        se.setModality(ds.getString(Tag.Modality));
        se.setManufacturer(ds.getString(Tag.Manufacturer));
        se.setManufacturerModelName(ds.getString(Tag.ManufacturerModelName));
        se.setSoftwareVersions(ds.getString(Tag.SoftwareVersions));
        se.setBodyPartExamined(ds.getString(Tag.BodyPartExamined));
        se.setScanningSequence(ds.getString(Tag.ScanningSequence));
        se.setSequenceVariant(ds.getString(Tag.SequenceVariant));
        se.setScanOptions(ds.getString(Tag.ScanOptions));
        se.setMrAcquisitionType(ds.getString(Tag.MRAcquisitionType));
        se.setSequenceName(ds.getString(Tag.SequenceName));
        se.setAngioFlag(ds.getString(Tag.AngioFlag));
        se.setTransmitCoilName(ds.getString(Tag.TransmitCoilName));
        se.setInPlanePhaseEncDirection(ds.getString(Tag.InPlanePhaseEncodingDirection));
        se.setVariableFlipAngleFlag(ds.getString(Tag.VariableFlipAngleFlag));
        se.setPatientPosition(ds.getString(Tag.PatientPosition));
        se.setSeriesDateRaw(ds.getString(Tag.SeriesDate));
        se.setSeriesTimeRaw(ds.getString(Tag.SeriesTime));
        se.setSeriesDateTime(toTimestamp(se.getSeriesDateRaw(), se.getSeriesTimeRaw()));
        se.setMagneticFieldStrength(getDouble(ds, Tag.MagneticFieldStrength));
        se.setImagingFrequency(getDouble(ds, Tag.ImagingFrequency));
        se.setPercentSampling(getDouble(ds, Tag.PercentSampling));
        se.setPercentPhaseFov(getDouble(ds, Tag.PercentPhaseFieldOfView));
        se.setPixelBandwidth(getDouble(ds, Tag.PixelBandwidth));
        se.setFlipAngle(getDouble(ds, Tag.FlipAngle));
        se.setFrameOfReferenceUid(ds.getString(Tag.FrameOfReferenceUID));
    }

    public void fillInstance(Attributes ds, DicomInstance in, DicomSeries se){
        in.setSopInstanceUid(ds.getString(Tag.SOPInstanceUID));
        in.setSeries(se);
        in.setSopClassUid(ds.getString(Tag.SOPClassUID));
        in.setInstanceNumber(getInt(ds, Tag.InstanceNumber));
        in.setInstanceCreationDateRaw(ds.getString(Tag.InstanceCreationDate));
        in.setInstanceCreationTimeRaw(ds.getString(Tag.InstanceCreationTime));
        in.setInstanceCreationTs(toTimestamp(in.getInstanceCreationDateRaw(), in.getInstanceCreationTimeRaw()));
        in.setAcquisitionDateRaw(ds.getString(Tag.AcquisitionDate));
        in.setAcquisitionTimeRaw(ds.getString(Tag.AcquisitionTime));
        in.setContentDateRaw(ds.getString(Tag.ContentDate));
        in.setContentTimeRaw(ds.getString(Tag.ContentTime));

        in.setImageType(String.join("\\", ds.getStrings(Tag.ImageType)==null? new String[0]: ds.getStrings(Tag.ImageType)));
        in.setPhotometricInterpretation(ds.getString(Tag.PhotometricInterpretation));
        in.setSamplesPerPixel(getInt(ds, Tag.SamplesPerPixel));
        in.setRows(getInt(ds, Tag.Rows));
        in.setCols(getInt(ds, Tag.Columns));
        in.setBitsAllocated(getInt(ds, Tag.BitsAllocated));
        in.setBitsStored(getInt(ds, Tag.BitsStored));
        in.setHighBit(getInt(ds, Tag.HighBit));
        in.setPixelRepresentation(getInt(ds, Tag.PixelRepresentation));

        in.setSliceThickness(getDouble(ds, Tag.SliceThickness));
        in.setSpacingBetweenSlices(getDouble(ds, Tag.SpacingBetweenSlices));
        double[] px = ds.getDoubles(Tag.PixelSpacing);
        if(px!=null && px.length>0) in.setPixelSpacingRow(px[0]);
        if(px!=null && px.length>1) in.setPixelSpacingCol(px[1]);

        double[] ipp = ds.getDoubles(Tag.ImagePositionPatient);
        if(ipp!=null && ipp.length>2){
            in.setImagePositionX(ipp[0]); in.setImagePositionY(ipp[1]); in.setImagePositionZ(ipp[2]);
        }
        double[] iop = ds.getDoubles(Tag.ImageOrientationPatient);
        if(iop!=null && iop.length>5){
            in.setIop1(iop[0]); in.setIop2(iop[1]); in.setIop3(iop[2]);
            in.setIop4(iop[3]); in.setIop5(iop[4]); in.setIop6(iop[5]);
        }

        in.setWindowCenter(getDouble(ds, Tag.WindowCenter));
        in.setWindowWidth(getDouble(ds, Tag.WindowWidth));
        in.setWindowExplanation(ds.getString(Tag.WindowCenterWidthExplanation));
        in.setEchoTime(getDouble(ds, Tag.EchoTime));
        in.setRepetitionTime(getDouble(ds, Tag.RepetitionTime));
        in.setEchoTrainLength(getInt(ds, Tag.EchoTrainLength));
        in.setNumberOfAverages(getDouble(ds, Tag.NumberOfAverages));
        in.setEchoNumber(getInt(ds, Tag.EchoNumbers));
        in.setSliceLocation(getDouble(ds, Tag.SliceLocation));
    }
}
