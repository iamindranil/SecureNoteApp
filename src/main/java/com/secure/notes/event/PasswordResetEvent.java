package com.secure.notes.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetEvent {
    private String email;
    private String token;
}
