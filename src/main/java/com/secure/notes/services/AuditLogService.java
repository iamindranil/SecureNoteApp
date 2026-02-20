package com.secure.notes.services;

import com.secure.notes.models.AuditLog;
import com.secure.notes.models.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {
    void logNoteCreation(String username, Note note);

    void logNoteUpdate(String username, Note note);

    void logNoteDeletion(String username, Long noteId);

    Page<AuditLog> getAllAuditLogs(Pageable pageable);

    List<AuditLog> getAuditLogsForNoteId(Long id);
}
