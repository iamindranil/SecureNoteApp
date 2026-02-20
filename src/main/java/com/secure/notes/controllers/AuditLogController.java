package com.secure.notes.controllers;


import com.secure.notes.models.AuditLog;
import com.secure.notes.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {
    @Autowired
    AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timeStamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ){
        Sort sort=direction.equalsIgnoreCase("desc")?
                Sort.by(sortBy).descending():
                Sort.by(sortBy).ascending();

        Pageable pageable= PageRequest.of(page,size,sort);
        return auditLogService.getAllAuditLogs(pageable);
    }

    @GetMapping("/note/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AuditLog> getNoteAuditLogs(@PathVariable Long id){
        return auditLogService.getAuditLogsForNoteId(id);
    }
}
