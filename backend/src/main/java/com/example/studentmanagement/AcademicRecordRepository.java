package com.example.studentmanagement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.studentmanagement.entity.AcademicRecord;

public interface AcademicRecordRepository
        extends JpaRepository<AcademicRecord, Long> {

    Optional<AcademicRecord> findByStudentId(Long studentId);
}
