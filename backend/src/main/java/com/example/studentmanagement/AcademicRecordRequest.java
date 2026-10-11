package com.example.studentmanagement;

public class AcademicRecordRequest {

    private Long studentId;
    private Double attendance;
    private Double marks;
    private String interventionNote;

    public AcademicRecordRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getAttendance() {
        return attendance;
    }

    public void setAttendance(Double attendance) {
        this.attendance = attendance;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getInterventionNote() {
        return interventionNote;
    }

    public void setInterventionNote(String interventionNote) {
        this.interventionNote = interventionNote;
    }
}