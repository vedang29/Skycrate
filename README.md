# 📁 Skycrate – Web-Based File Management System

Skycrate is a web based file management system that uses Hadoop as filesystem.

- Dockerized a Hadoop cluster to resolve dependency issues, hosting it internally and exposing it to the team via Tailscale.  
- Integrated internationalization (i18n) for multilingual support across the frontend.  
- Implemented hybrid RSA-AES encryption for each file, utilizing user-specific key pairs.  
- Established strong password policies with breach checks via Have I Been Pwned.  
- Utilized JWT-based authentication with refresh token support and a blacklist feature upon logout.  
- Implemented brute-force login protection through rate limiting.  
- Enforced HTTPS with automatic redirection from HTTP to HTTPS.  
- Enabled encrypted file upload and download to/from HDFS, with metadata stored in a database.  
- Developed audit logging and structured Data Transfer Objects (DTOs) with validation.  
- Created a token refresh endpoint and ensured secure session handling.  
- Automated user directory creation in HDFS upon user registration.  
- Enhanced Spring Security, streamlined configuration, and established a modular service structure.
