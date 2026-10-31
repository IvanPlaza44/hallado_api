package com.hallado.api.service;

import com.hallado.api.dto.response.HistorialCasoDto;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.RolHistorial;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.repository.AgradecimientoRepository;
import com.hallado.api.repository.ReclamoRepository;
import com.hallado.api.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Historial de devoluciones concretadas (publicación RESUELTA con un reclamo ACEPTADO).
 *
 * Quién devolvió y quién recuperó depende del tipo de publicación: en una de algo ENCONTRADO
 * el publicador devolvió y el reclamante recuperó; en una de algo PERDIDO es al revés.
 */
@Service
@RequiredArgsConstructor
public class HistorialService {

    static final int PLAZO_VALORACION_DIAS = 7;

    private final ReclamoRepository reclamoRepository;
    private final ValoracionRepository valoracionRepository;
    private final AgradecimientoRepository agradecimientoRepository;

    @Transactional(readOnly = true)
    public List<HistorialCasoDto> listar(Usuario usuario, RolHistorial rol, LocalDate desde, LocalDate hasta,
                                         TipoPublicacion tipo, CategoriaItem categoria) {
        List<Reclamo> aceptados = new ArrayList<>(
                reclamoRepository.findByPublicacion_PublicadorAndEstado(usuario, EstadoReclamo.ACEPTADO));
        aceptados.addAll(reclamoRepository.findByReclamanteAndEstado(usuario, EstadoReclamo.ACEPTADO));

        return aceptados.stream()
                .filter(r -> r.getPublicacion().getEstado() == EstadoPublicacion.RESUELTA)
                .filter(r -> rol == null || rolDe(r, usuario) == rol)
                .filter(r -> tipo == null || r.getPublicacion().getTipo() == tipo)
                .filter(r -> categoria == null || r.getPublicacion().getCategoria() == categoria)
                .filter(r -> desde == null || !fechaDevolucion(r).toLocalDate().isBefore(desde))
                .filter(r -> hasta == null || !fechaDevolucion(r).toLocalDate().isAfter(hasta))
                .sorted(Comparator.comparing(HistorialService::fechaDevolucion).reversed())
                .map(r -> aDto(r, usuario))
                .toList();
    }

    private HistorialCasoDto aDto(Reclamo aceptado, Usuario usuario) {
        Publicacion p = aceptado.getPublicacion();
        RolHistorial rol = rolDe(aceptado, usuario);
        Usuario contraparte = contraparte(aceptado, usuario);
        LocalDateTime limite = fechaLimiteValoracion(aceptado);

        HistorialCasoDto.MiValoracion miValoracion = valoracionRepository.findFirstByEmisorAndPublicacion(usuario, p)
                .map(v -> new HistorialCasoDto.MiValoracion(v.isPositiva(), v.getComentario()))
                .orElse(null);
        HistorialCasoDto.MiAgradecimiento miAgradecimiento = agradecimientoRepository.findByAutorAndPublicacion(usuario, p)
                .map(a -> new HistorialCasoDto.MiAgradecimiento(a.getMensaje()))
                .orElse(null);

        boolean yaValore = miValoracion != null;
        boolean yaAgradeci = miAgradecimiento != null;

        return new HistorialCasoDto(
                p.getId(), rol, fechaDevolucion(aceptado), p.getTitulo(), p.getTipo(), p.getCategoria(),
                p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                new HistorialCasoDto.Contraparte(contraparte.getId(), contraparte.getNombre()),
                miValoracion, yaValore, !yaValore && !LocalDateTime.now().isAfter(limite), limite,
                miAgradecimiento, yaAgradeci, rol == RolHistorial.RECUPERE && !yaAgradeci
        );
    }

    static boolean participa(Reclamo aceptado, Usuario usuario) {
        return aceptado.getPublicacion().getPublicador().getId().equals(usuario.getId())
                || aceptado.getReclamante().getId().equals(usuario.getId());
    }

    static RolHistorial rolDe(Reclamo aceptado, Usuario usuario) {
        boolean esPublicador = aceptado.getPublicacion().getPublicador().getId().equals(usuario.getId());
        boolean publicoAlgoEncontrado = aceptado.getPublicacion().getTipo() == TipoPublicacion.ENCONTRADO;
        return esPublicador == publicoAlgoEncontrado ? RolHistorial.DEVOLVI : RolHistorial.RECUPERE;
    }

    static Usuario contraparte(Reclamo aceptado, Usuario usuario) {
        Usuario publicador = aceptado.getPublicacion().getPublicador();
        return publicador.getId().equals(usuario.getId()) ? aceptado.getReclamante() : publicador;
    }

    static LocalDateTime fechaDevolucion(Reclamo aceptado) {
        return aceptado.getFechaResolucion() != null
                ? aceptado.getFechaResolucion()
                : aceptado.getPublicacion().getFechaActualizacion();
    }

    static LocalDateTime fechaLimiteValoracion(Reclamo aceptado) {
        return fechaDevolucion(aceptado).plusDays(PLAZO_VALORACION_DIAS);
    }
}
