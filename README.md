# 🛡️ SecureNotes API 

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0--M1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)

## 📖 Executive Summary
SecureNotes is a production-grade, security-hardened RESTful API built with Spring Boot. It provides a robust backend for managing encrypted notes, featuring advanced enterprise-level security paradigms including **OAuth2**, **TOTP Two-Factor Authentication (2FA)**, and **Redis-backed JWT Blacklisting**.

This project was built with a "Security-First" mindset, successfully mitigating critical web vulnerabilities (IDOR, TOCTOU, Timing Attacks) while maintaining high performance and clean architectural boundaries.

## ✨ Core Enterprise Features

### 🔐 Advanced Authentication & Security
* **Multi-Factor Authentication:** Standard username/password flow fortified with Time-based One-Time Password (TOTP) 2FA.
* **Social SSO:** OAuth2 integration for seamless Google/GitHub single sign-on.
* **Role-Based Access Control (RBAC):** Strict hierarchical authorization (`ROLE_USER`, `ROLE_ADMIN`) preventing privilege escalation.
* **JWT Management:** Stateless session policy with a Redis-backed token blacklist for immediate session revocation and logout handling.
* **CSRF Protection:** Integrated Cross-Site Request Forgery defenses.

### 🛡️ Vulnerability Mitigation
* **IDOR Prevention:** Collapsed resource lookup and authorization into single, atomic database queries to prevent Insecure Direct Object Reference probing.
* **Timing-Attack Defense:** Preserved Spring Security's exception contract (`UsernameNotFoundException`) to prevent username enumeration during login attempts.
* **TOCTOU Race Condition Handling:** Secured role-update and user-management endpoints against Time-of-Check to Time-of-Use vulnerabilities using strict `@Transactional` boundaries.

### ⚡ Architecture & Performance
* **Event-Driven Email Dispatch:** Decoupled password-reset email logic (via JavaMailSender) using Spring's `AFTER_COMMIT` event listeners, ensuring async execution and preventing silent failures or database transaction rollbacks.
* **Caching & Rate Limiting:** Implemented Redis to cache user data and enforce strict API rate limiting, protecting against brute-force attacks.
* **Global Exception Handling:** Standardized API contracts using `@ControllerAdvice` and a custom `AccessDeniedHandler` to ensure predictable, sanitized JSON error responses without leaking internal server details.

## 🛠️ Technology Stack
* **Core:** Java, Spring Boot 4, Spring Web
* **Security:** Spring Security, OAuth2 Client, JWT (io.jsonwebtoken)
* **Data Persistence:** Spring Data JPA (Hibernate), MySQL
* **Caching & Performance:** Redis, Spring Cache
* **Messaging/Mail:** JavaMailSender
* **Build Tool:** Maven
