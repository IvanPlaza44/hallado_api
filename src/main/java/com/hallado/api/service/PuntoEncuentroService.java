package com.hallado.api.service;

import com.hallado.api.dto.request.PuntoEncuentroRequestDto;
import com.hallado.api.dto.response.EncuentroDto;
import com.hallado.api.dto.response.PuntoEncuentroDto;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.PuntoEncuentro;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.EstadoPuntoEncuentro;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.PuntoEncuentroRepository;
import com.hallado.api.repository.ReclamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Devolución y punto de encuentro (Punto 8).
 * - Solo participan el publicador y el reclamante de un reclamo ACEPTADO.
 * - La primera propuesta la hace quien tiene el objeto.
 * - La otra parte acepta o propone otro punto (el anterior queda REEMPLAZADO), y así
 *   hasta que alguien acepta la propuesta del otro.
 * - El teléfono de la otra parte se muestra solo si lo eligió en su perfil (Punto 2).
 */
@Service
@RequiredArgsConstructor
public class PuntoEncuentroService {

    private final ReclamoRepository reclamoRepository;
    private final PuntoEncuentroRepository puntoEncuentroRepository;

    @Transactional(readOnly = true)
    public EncuentroDto obtener(Usuario usuario, Long reclamoId) {
        Reclamo reclamo = obtenerReclamoAceptado(usuario, reclamoId);
        return aDto(reclamo, usuario);
    }

    @Transactional
    public EncuentroDto proponer(Usuario usuario, Long reclamoId, PuntoEncuentroRequestDto dto) {
        Reclamo reclamo = obtenerReclamoAceptado(usuario, reclamoId);
        PuntoEncuentro vigente = vigente(reclamo);

        if (vigente == null) {
            if (!tieneElObjeto(reclamo, usuario)) {
                throw ApiException.solicitudInvalida("El punto de encuentro lo propone quien tiene el objeto");
            }
        } else {
            if (vigente.getEstado() == EstadoPuntoEncuentro.ACEPTADO) {
                throw ApiException.conflicto("Ya acordaron un punto de encuentro");
            }
            if (esDe(vigente, usuario)) {
                throw ApiException.solicitudInvalida("Esperá a que la otra persona responda tu propuesta");
            }
            // Contrapropuesta: la propuesta anterior deja de estar vigente
            vigente.setEstado(EstadoPuntoEncuentro.REEMPLAZADO);
            vigente.setFechaRespuesta(LocalDateTime.now());
            puntoEncuentroRepository.save(vigente);
        }

        String direccion = textoOpcional(dto.direccion());
        boolean hayUbicacion = dto.latitud() != null && dto.longitud() != null;
        if (direccion == null && !hayUbicacion) {
            throw ApiException.solicitudInvalida("Indicá una dirección o usá tu ubicación actual");
        }

        PuntoEncuentro nuevo = PuntoEncuentro.builder()
                .reclamo(reclamo)
                .propuestoPor(usuario)
                .direccion(direccion)
                .latitud(hayUbicacion ? dto.latitud() : null)
                .longitud(hayUbicacion ? dto.longitud() : null)
                .indicaciones(textoOpcional(dto.indicaciones()))
                .build();
        puntoEncuentroRepository.save(nuevo);

        return aDto(reclamo, usuario);
    }

    @Transactional
    public EncuentroDto aceptar(Usuario usuario, Long reclamoId, Long puntoId) {
        Reclamo reclamo = obtenerReclamoAceptado(usuario, reclamoId);
        PuntoEncuentro punto = puntoEncuentroRepository.findById(puntoId)
                .orElseThrow(() -> ApiException.noEncontrado("Punto de encuentro no encontrado"));

        if (!punto.getReclamo().getId().equals(reclamo.getId())) {
            throw ApiException.solicitudInvalida("Ese punto de encuentro no pertenece a este reclamo");
        }
        if (punto.getEstado() != EstadoPuntoEncuentro.PROPUESTO) {
            throw ApiException.solicitudInvalida("Esta propuesta ya no está vigente");
        }
        if (esDe(punto, usuario)) {
            throw ApiException.solicitudInvalida("No podés aceptar tu propia propuesta");
        }

        punto.setEstado(EstadoPuntoEncuentro.ACEPTADO);
        punto.setFechaRespuesta(LocalDateTime.now());
        puntoEncuentroRepository.save(punto);

        return aDto(reclamo, usuario);
    }

    // ---------- Helpers ----------

    private Reclamo obtenerReclamoAceptado(Usuario usuario, Long reclamoId) {
        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> ApiException.noEncontrado("Reclamo no encontrado"));
        boolean esPublicador = reclamo.getPublicacion().getPublicador().getId().equals(usuario.getId());
        boolean esReclamante = reclamo.getReclamante().getId().equals(usuario.getId());
        if (!esPublicador && !esReclamante) {
            throw ApiException.noAutorizado("No participás de este reclamo");
        }
        if (reclamo.getEstado() != EstadoReclamo.ACEPTADO) {
            throw ApiException.solicitudInvalida("El punto de encuentro se acuerda una vez aceptado el reclamo");
        }
        return reclamo;
    }

    /** ENCONTRADO: el objeto lo tiene quien publicó. PERDIDO: lo tiene quien dijo "Lo encontré". */
    private boolean tieneElObjeto(Reclamo reclamo, Usuario usuario) {
        Publicacion p = reclamo.getPublicacion();
        Usuario tenedor = p.getTipo() == TipoPublicacion.ENCONTRADO ? p.getPublicador() : reclamo.getReclamante();
        return tenedor.getId().equals(usuario.getId());
    }

    private Usuario contraparte(Reclamo reclamo, Usuario usuario) {
        Usuario publicador = reclamo.getPublicacion().getPublicador();
        return publicador.getId().equals(usuario.getId()) ? reclamo.getReclamante() : publicador;
    }

    private PuntoEncuentro vigente(Reclamo reclamo) {
        return puntoEncuentroRepository
                .findFirstByReclamoAndEstadoNotOrderByIdDesc(reclamo, EstadoPuntoEncuentro.REEMPLAZADO)
                .orElse(null);
    }

    private boolean esDe(PuntoEncuentro punto, Usuario usuario) {
        return punto.getPropuestoPor().getId().equals(usuario.getId());
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private EncuentroDto aDto(Reclamo reclamo, Usuario usuario) {
        PuntoEncuentro v = vigente(reclamo);
        boolean tengoElObjeto = tieneElObjeto(reclamo, usuario);
        boolean propuestaDelOtro = v != null && v.getEstado() == EstadoPuntoEncuentro.PROPUESTO && !esDe(v, usuario);
        boolean puedoProponer = v == null ? tengoElObjeto : propuestaDelOtro;

        PuntoEncuentroDto punto = v == null ? null : new PuntoEncuentroDto(
                v.getId(), v.getDireccion(), v.getLatitud(), v.getLongitud(), v.getIndicaciones(),
                v.getEstado(), esDe(v, usuario), v.getPropuestoPor().getNombre(), v.getFechaCreacion()
        );

        // Punto 2: el teléfono solo se comparte si la otra persona lo eligió en su perfil.
        // Acá siempre hay un reclamo aceptado (lo valida obtenerReclamoAceptado).
        Usuario otro = contraparte(reclamo, usuario);
        String telefono = otro.isTelefonoVisible() ? textoOpcional(otro.getTelefono()) : null;

        Publicacion p = reclamo.getPublicacion();
        return new EncuentroDto(
                reclamo.getId(), p.getId(), p.getTitulo(), tengoElObjeto,
                otro.getNombre(), telefono,
                punto, puedoProponer, propuestaDelOtro
        );
    }
}