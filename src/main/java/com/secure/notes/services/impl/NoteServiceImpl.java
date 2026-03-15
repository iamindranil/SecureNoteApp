package com.secure.notes.services.impl;

import com.secure.notes.exceptions.ResourceNotFoundException;
import com.secure.notes.models.Note;
import com.secure.notes.repositories.NoteRepository;
import com.secure.notes.services.AuditLogService;
import com.secure.notes.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    @Override
    public Note createNoteForUser(String username, String content){
        Note note=new Note();
        note.setContent(content);
        note.setOwnerUsername(username);
        Note savedNote=noteRepository.save(note);
        auditLogService.logNoteCreation(username,savedNote);
        return savedNote;
    }

    @Transactional
    @Override
    public Note updateNoteForUser(Long noteId, String content, String username){
        Note note=noteRepository.findByIdAndOwnerUsername(noteId,username)
                        .orElseThrow(()->new ResourceNotFoundException("Note not found"));
        note.setContent(content);
        Note savedNote=noteRepository.save(note);
        auditLogService.logNoteUpdate(username,savedNote);
        return savedNote;
    }

    @Transactional
    @Override
    public void deleteNoteForUser(Long noteId, String username){
        Note note=noteRepository.findByIdAndOwnerUsername(noteId,username)
                .orElseThrow(()->new ResourceNotFoundException("Note not found"));
        auditLogService.logNoteDeletion(username,noteId);
        noteRepository.delete(note);
    }

    @Override
    public List<Note> getNotesForUser(String username){
        return noteRepository.findByOwnerUsername(username);
    }



}
