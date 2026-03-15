package com.secure.notes.models;

public enum AppRole {
    ROLE_USER,
    ROLE_ADMIN;

    public static AppRole fromString(String roleName) {
        try {
            return AppRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
}
