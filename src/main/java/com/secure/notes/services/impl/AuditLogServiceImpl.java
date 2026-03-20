package com.secure.notes.services.impl;

import com.secure.notes.models.AuditLog;
import com.secure.notes.models.Note;
import com.secure.notes.repositories.AuditLogRepository;
import com.secure.notes.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    AuditLogRepository auditLogRepository;

    @Override
    public void logNoteCreation(String username, Note note){
        AuditLog log=new AuditLog();
        log.setAction("CREATED");
        log.setUsername(username);
        log.setNoteId(note.getId());
        log.setNoteContent(note.getContent());
        log.setTimeStamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
    @Override
    public void logNoteUpdate(String username, Note note){
        AuditLog log=new AuditLog();
        log.setAction("UPDATED");
        log.setUsername(username);
        log.setNoteId(note.getId());
        log.setNoteContent(note.getContent());
        log.setTimeStamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
    @Override
    public void logNoteDeletion(String username, Long noteId){
        AuditLog log=new AuditLog();
        log.setAction("DELETED");
        log.setUsername(username);
        log.setNoteId(noteId);
        log.setTimeStamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public List<AuditLog> getAuditLogsForNoteId(Long id) {
        return auditLogRepository.findByNoteId(id);
    }
}
