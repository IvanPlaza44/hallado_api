package com.hallado.api.repository;

import com.hallado.api.entity.CodigoOtp;
import com.hallado.api.enums.PropositoOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoOtpRepository extends JpaRepository<CodigoOtp, Long> {
    Optional<CodigoOtp> findFirstByEmailAndPropositoAndUsadoFalseOrderByIdDesc(String email, PropositoOtp proposito);
}
