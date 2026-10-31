package com.hallado.api.service;

import com.hallado.api.dto.request.AgradecimientoRequestDto;
import com.hallado.api.dto.response.AgradecimientoResponseDto;
import com.hallado.api.entity.Agradecimiento;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.RolHistorial;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.AgradecimientoRepository;
import com.hallado.api.repository.PublicacionRepository;
import com.hallado.api.repository.ReclamoRepository;
import com.hallado.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agradecimientos públicos: sólo quien recuperó su objeto puede dejarle uno a quien
 * se lo devolvió, una vez por devolución. Se muestran en el perfil de quien lo recibe.
 */
@Service
@RequiredArgsConstructor
public class AgradecimientoService {

    private static final int LARGO_MAXIMO = 300;

    private final AgradecimientoRepository agradecimientoRepository;
    private final PublicacionRepository publicacionRepository;
    private final ReclamoRepository reclamoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AgradecimientoResponseDto crear(Usuario autor, AgradecimientoRequestDto dto) {
        if (dto.publicacionId() == null) {
            throw ApiException.solicitudInvalida("Debe indicar la publicación");
        }
        Publicacion p = publicacionRepository.findById(dto.publicacionId())
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));

        if (p.getEstado() != EstadoPublicacion.RESUELTA) {
            throw ApiException.solicitudInvalida("Sólo se puede agradecer una devolución ya resuelta");
        }
        Reclamo aceptado = reclamoRepository.findFirstByPublicacionAndEstado(p, EstadoReclamo.ACEPTADO)
                .orElseThrow(() -> ApiException.solicitudInvalida("Esta publicación no tiene un reclamo aceptado"));

        if (!HistorialService.participa(aceptado, autor)) {
            throw ApiException.noAutorizado("No participaste en la devolución de esta publicación");
        }
        if (HistorialService.rolDe(aceptado, autor) != RolHistorial.RECUPERE) {
            throw ApiException.noAutorizado("Solo quien recuperó su objeto puede agradecer");
        }
        if (agradecimientoRepository.existsByAutorAndPublicacion(autor, p)) {
            throw ApiException.conflicto("Ya dejaste un agradecimiento para esta devolución");
        }

        String mensaje = dto.mensaje() == null ? "" : dto.mensaje().trim();
        if (mensaje.isEmpty()) {
            throw ApiException.solicitudInvalida("Escribí un mensaje de agradecimiento");
        }
        if (mensaje.length() > LARGO_MAXIMO) {
            throw ApiException.solicitudInvalida("El mensaje no puede superar los " + LARGO_MAXIMO + " caracteres");
        }

        Agradecimiento agradecimiento = Agradecimiento.builder()
                .autor(autor)
                .receptor(HistorialService.contraparte(aceptado, autor))
                .publicacion(p)
                .mensaje(mensaje)
                .build();
        agradecimientoRepository.save(agradecimiento);
        return aDto(agradecimiento);
    }

    @Transactional(readOnly = true)
    public List<AgradecimientoResponseDto> listarRecibidos(Long usuarioId) {
        Usuario receptor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        return agradecimientoRepository.findByReceptorOrderByFechaCreacionDesc(receptor).stream()
                .map(this::aDto)
                .toList();
    }

    private AgradecimientoResponseDto aDto(Agradecimiento a) {
        return new AgradecimientoResponseDto(
                a.getId(), a.getAutor().getNombre(), a.getMensaje(), a.getPublicacion().getTitulo(), a.getFechaCreacion()
        );
    }
}
