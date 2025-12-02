package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.VerificationCodeRepository;
import com.Perfume_e_commerce.models.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class VerificationCodeService {
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    public VerificationCode createVerificationCode(String email) {
        String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        verificationCodeRepository.findByEmail(email)
                .ifPresent(verificationCodeRepository::delete);

        VerificationCode vc = new VerificationCode(email, code);
        return verificationCodeRepository.save(vc);
    }

    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> vcOpt = verificationCodeRepository.findByEmail(email);

        if (vcOpt.isEmpty()) return false;

        VerificationCode vc = vcOpt.get();
        return vc.getCode().equals(code) && !vc.isExpired();
    }

    public void deleteByEmail(String email) {
        verificationCodeRepository.findByEmail(email)
                .ifPresent(verificationCodeRepository::delete);
    }

    public Optional<VerificationCode> findByEmail(String email) {
        return verificationCodeRepository.findByEmail(email);
    }

}
