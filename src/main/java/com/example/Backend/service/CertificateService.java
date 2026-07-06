package com.example.Backend.service;

import com.example.Backend.model.Certificate;
import com.example.Backend.model.User;

public interface CertificateService {

    /** Generates (if not already generated) a PDF certificate for an ATTENDED registration. */
    Certificate generateForRegistration(Long registrationId, User currentUser);

    Certificate findByRegistration(Long registrationId);
}
