package com.hallado.api.service;

import com.hallado.api.entity.CodigoOtp;
import com.hallado.api.enums.PropositoOtp;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.CodigoOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final CodigoOtpRepository codigoOtpRepository;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration-minutes}")
    private int expiracionMinutos;

    @Value("${app.otp.length}")
    private int longitud;

    public void generarYEnviar(String email) {
        generar(email, PropositoOtp.REGISTRO, mailService::enviarOtp);
    }

    public void reenviar(String email) {
        generarYEnviar(email);
    }

    public void verificar(String email, String codigo) {
        verificar(email, codigo, PropositoOtp.REGISTRO);
    }

    public void generarYEnviarRecuperacion(String email) {
        generar(email, PropositoOtp.RECUPERACION, mailService::enviarCodigoRecuperacion);
    }

    public void reenviarRecuperacion(String email) {
        generarYEnviarRecuperacion(email);
    }

    public void verificarRecuperacion(String email, String codigo) {
        verificar(email, codigo, PropositoOtp.RECUPERACION);
    }

    private void generar(String email, PropositoOtp proposito, java.util.function.BiConsumer<String, String> envio) {
        String codigo = generarCodigo();
        CodigoOtp otp = CodigoOtp.builder()
                .email(email)
                .codigo(codigo)
                .expiracion(LocalDateTime.now().plusMinutes(expiracionMinutos))
                .usado(false)
                .proposito(proposito)
                .build();
        codigoOtpRepository.save(otp);

        // Se mantiene el log para debugging (por si el mail tarda o falla),
        // y ahora además se manda el mail real.
        log.info("OTP ({}) generado para {}: {} (valido {} minutos)", proposito, email, codigo, expiracionMinutos);
        envio.accept(email, codigo);
    }

    private void verificar(String email, String codigo, PropositoOtp proposito) {
        CodigoOtp otp = codigoOtpRepository.findFirstByEmailAndPropositoAndUsadoFalseOrderByIdDesc(email, proposito)
                .orElseThrow(() -> ApiException.solicitudInvalida("No hay un código pendiente para este email"));

        if (otp.getExpiracion().isBefore(LocalDateTime.now())) {
            throw ApiException.solicitudInvalida("El código expiró, solicitá uno nuevo");
        }
        if (!otp.getCodigo().equals(codigo)) {
            throw ApiException.solicitudInvalida("El código ingresado es incorrecto");
        }

        otp.setUsado(true);
        codigoOtpRepository.save(otp);
    }

    private String generarCodigo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
