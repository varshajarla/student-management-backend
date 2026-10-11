package com.example.studentmanagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.studentmanagement.entity.AcademicRecord;
import com.example.studentmanagement.entity.Student;

@RestController
@RequestMapping("/academic-records")
@CrossOrigin(origins = "http://localhost:5173")
public class AcademicRecordController {

    private final AcademicRecordRepository recordRepository;
    private final StudentRepository studentRepository;

    public AcademicRecordController(
            AcademicRecordRepository recordRepository,
            StudentRepository studentRepository) {
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<AcademicRecord> getAllRecords() {
        return recordRepository.findAll();
    }

    @PostMapping
    public AcademicRecord saveRecord(
            @RequestBody AcademicRecordRequest request) {

        if (request.getStudentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Student ID is required");
        }

        validateRecord(request);

        Student student = studentRepository
                .findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"));

        AcademicRecord record = recordRepository
                .findByStudentId(request.getStudentId())
                .orElseGet(AcademicRecord::new);

        record.setStudent(student);
        record.setAttendance(request.getAttendance());
        record.setMarks(request.getMarks());
        record.setInterventionNote(request.getInterventionNote());

        return recordRepository.save(record);
    }

    @GetMapping("/{studentId}")
    public AcademicRecord getRecord(
            @PathVariable Long studentId) {

        return recordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Academic record not found"));
    }

    private void validateRecord(AcademicRecordRequest request) {
        if (request.getAttendance() == null
                || request.getAttendance() < 0
                || request.getAttendance() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance must be between 0 and 100");
        }

        if (request.getMarks() == null
                || request.getMarks() < 0
                || request.getMarks() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marks must be between 0 and 100");
        }
    }
}