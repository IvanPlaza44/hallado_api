package com.hallado.api.service;

import com.hallado.api.dto.request.PublicacionRequestDto;
import com.hallado.api.dto.request.ReclamoRequestDto;
import com.hallado.api.dto.response.PaginaDto;
import com.hallado.api.dto.response.PublicacionDetalleDto;
import com.hallado.api.dto.response.PublicacionResumenDto;
import com.hallado.api.dto.response.PublicacionSeguidaDto;
import com.hallado.api.dto.response.ReclamoResponseDto;
import com.hallado.api.entity.Foto;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Seguimiento;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.OrdenResultados;
import com.hallado.api.enums.RangoFecha;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.PublicacionRepository;
import com.hallado.api.repository.ReclamoRepository;
import com.hallado.api.repository.SeguimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final ReclamoRepository reclamoRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final CloudinaryService cloudinaryService;
    private final ReputacionService reputacionService;

    // ---------- Crear / editar (carga guiada, igual al patrón de "borrador" de Ronda) ----------

    @Transactional
    public PublicacionDetalleDto crear(Usuario publicador, PublicacionRequestDto dto, Usuario quienMira) {
        if (dto.tipo() == null) {
            throw ApiException.solicitudInvalida("Debe indicar si perdiste algo o encontraste algo");
        }
        Publicacion p = Publicacion.builder()
                .publicador(publicador)
                .tipo(dto.tipo())
                .estado(EstadoPublicacion.BORRADOR)
                .build();
        aplicarCambios(p, dto);
        publicacionRepository.save(p);
        return aDetalle(p, quienMira);
    }

    @Transactional
    public PublicacionDetalleDto actualizar(Usuario usuario, Long id, PublicacionRequestDto dto) {
        Publicacion p = obtenerPropia(usuario, id);
        aplicarCambios(p, dto);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return aDetalle(p, usuario);
    }

    private void aplicarCambios(Publicacion p, PublicacionRequestDto dto) {
        if (dto.categoria() != null) p.setCategoria(dto.categoria());
        if (dto.titulo() != null) p.setTitulo(dto.titulo());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.palabrasClave() != null) p.setPalabrasClave(dto.palabrasClave());
        if (dto.fechaSuceso() != null) p.setFechaSuceso(dto.fechaSuceso());
        if (dto.franjaHoraria() != null) p.setFranjaHoraria(dto.franjaHoraria());
        if (dto.barrio() != null) p.setBarrio(dto.barrio());
        if (dto.latitud() != null) p.setLatitud(dto.latitud());
        if (dto.longitud() != null) p.setLongitud(dto.longitud());
        if (dto.direccionExacta() != null) p.setDireccionExacta(dto.direccionExacta());
        if (dto.horaSuceso() != null) p.setHoraSuceso(dto.horaSuceso());
        if (dto.preguntaVerificacion() != null) p.setPreguntaVerificacion(dto.preguntaVerificacion());
        if (dto.respuestaVerificacion() != null) p.setRespuestaVerificacion(dto.respuestaVerificacion());
    }

    @Transactional
    public PublicacionDetalleDto agregarFoto(Usuario usuario, Long id, MultipartFile archivo) {
        Publicacion p = obtenerPropia(usuario, id);
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida("Debe adjuntar una imagen");
        }
        p.getFotos().size(); // fuerza la carga de la lista lazy antes de agregar (ver nota en Ronda)

        String url = cloudinaryService.subirImagen(archivo, "hallado/publicaciones");
        Foto foto = Foto.builder().publicacion(p).url(url).build();
        p.getFotos().add(foto);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return aDetalle(p, usuario);
    }

    @Transactional
    public void eliminar(Usuario usuario, Long id) {
        Publicacion p = obtenerPropia(usuario, id);
        publicacionRepository.delete(p);
    }

    // ---------- Borradores, publicar, renovar y vencimiento ----------

    @Transactional
    public PublicacionDetalleDto publicar(Usuario usuario, Long id) {
        Publicacion p = obtenerPropia(usuario, id);
        if (p.getEstado() != EstadoPublicacion.BORRADOR) {
            throw ApiException.solicitudInvalida("La publicación ya está publicada");
        }
        // Para publicar tienen que estar los datos obligatorios completos
        if (p.getCategoria() == null || p.getTitulo() == null || p.getTitulo().isBlank()
                || p.getFechaSuceso() == null || p.getBarrio() == null || p.getBarrio().isBlank()) {
            throw ApiException.solicitudInvalida(
                    "Completá categoría, título, fecha del suceso y barrio antes de publicar");
        }
        // Si es algo ENCONTRADO, la pregunta de verificación y su respuesta son obligatorias
        if (p.getTipo() == TipoPublicacion.ENCONTRADO
                && (p.getPreguntaVerificacion() == null || p.getPreguntaVerificacion().isBlank()
                || p.getRespuestaVerificacion() == null || p.getRespuestaVerificacion().isBlank())) {
            throw ApiException.solicitudInvalida(
                    "Si encontraste algo, cargá una pregunta de verificación y su respuesta");
        }
        p.setEstado(EstadoPublicacion.ABIERTA);
        p.setFechaPublicacion(LocalDateTime.now());
        p.setFechaVencimiento(LocalDateTime.now().plusDays(30));
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return aDetalle(p, usuario);
    }

    public List<PublicacionResumenDto> misBorradores(Usuario usuario) {
        return publicacionRepository
                .findByPublicadorAndEstadoIn(usuario, List.of(EstadoPublicacion.BORRADOR))
                .stream().map(this::aResumen).toList();
    }

    @Transactional
    public PublicacionDetalleDto renovar(Usuario usuario, Long id) {
        Publicacion p = obtenerPropia(usuario, id);
        if (p.getEstado() != EstadoPublicacion.ABIERTA && p.getEstado() != EstadoPublicacion.VENCIDA) {
            throw ApiException.solicitudInvalida("Solo se pueden renovar publicaciones abiertas o vencidas");
        }
        p.setEstado(EstadoPublicacion.ABIERTA);
        p.setFechaVencimiento(LocalDateTime.now().plusDays(30));
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return aDetalle(p, usuario);
    }

    /** Corre cada hora: las ABIERTAS cuyo plazo terminó pasan a VENCIDA. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void vencerPublicaciones() {
        publicacionRepository
                .findByEstadoAndFechaVencimientoBefore(EstadoPublicacion.ABIERTA, LocalDateTime.now())
                .forEach(p -> p.setEstado(EstadoPublicacion.VENCIDA));
    }

    // ---------- Buscador guiado y resultados ----------

    public PaginaDto<PublicacionResumenDto> buscar(TipoPublicacion tipoBuscado, CategoriaItem categoria,
                                                     String palabrasClave, String barrio,
                                                     RangoFecha rango, LocalDate fechaDesde, LocalDate fechaHasta,
                                                     OrdenResultados orden, int pagina, int tamanio) {

        // "La búsqueda devuelve las publicaciones del tipo opuesto": si el usuario perdió algo,
        // busca entre lo ENCONTRADO, y viceversa.
        TipoPublicacion tipoAMostrar = tipoBuscado == null ? null
                : (tipoBuscado == TipoPublicacion.PERDIDO ? TipoPublicacion.ENCONTRADO : TipoPublicacion.PERDIDO);

        var rangoFechas = resolverRango(rango, fechaDesde, fechaHasta);

        Specification<Publicacion> spec = Specification.where(PublicacionSpecifications.estado(EstadoPublicacion.ABIERTA))
                .and(PublicacionSpecifications.tipo(tipoAMostrar))
                .and(PublicacionSpecifications.categoria(categoria))
                .and(PublicacionSpecifications.texto(palabrasClave))
                .and(PublicacionSpecifications.barrio(barrio))
                .and(PublicacionSpecifications.fechaSucesoDesde(rangoFechas[0]))
                .and(PublicacionSpecifications.fechaSucesoHasta(rangoFechas[1]));

        Sort sort = orden == OrdenResultados.CERCANOS
                // Ordenar por distancia real requiere una consulta geoespacial que no entra en un Sort simple;
                // por ahora se ordena por más recientes y queda documentado como mejora futura.
                ? Sort.by(Sort.Direction.DESC, "fechaPublicacion")
                : Sort.by(Sort.Direction.DESC, "fechaPublicacion");

        Page<Publicacion> page = publicacionRepository.findAll(spec, PageRequest.of(pagina, tamanio, sort));
        List<PublicacionResumenDto> contenido = page.getContent().stream().map(this::aResumen).toList();

        return new PaginaDto<>(contenido, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private LocalDate[] resolverRango(RangoFecha rango, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (rango == null) return new LocalDate[]{null, null};
        LocalDate hoy = LocalDate.now();
        return switch (rango) {
            case HOY -> new LocalDate[]{hoy, hoy};
            case ULTIMOS_3_DIAS -> new LocalDate[]{hoy.minusDays(3), hoy};
            case ULTIMA_SEMANA -> new LocalDate[]{hoy.minusWeeks(1), hoy};
            case RANGO -> new LocalDate[]{fechaDesde, fechaHasta};
        };
    }

    public List<PublicacionResumenDto> misPublicaciones(Usuario usuario) {
        return publicacionRepository
                .findByPublicadorAndEstadoIn(usuario, List.of(
                        EstadoPublicacion.ABIERTA, EstadoPublicacion.EN_RECLAMO,
                        EstadoPublicacion.RESUELTA, EstadoPublicacion.VENCIDA))
                .stream().map(this::aResumen).toList();
    }

    // ---------- Detalle ----------

    public PublicacionDetalleDto obtenerDetalle(Long id, Usuario quienMira) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        // Un borrador solo lo ve su dueño
        if (p.getEstado() == EstadoPublicacion.BORRADOR
                && (quienMira == null || !p.getPublicador().getId().equals(quienMira.getId()))) {
            throw ApiException.noEncontrado("Publicación no encontrada");
        }
        return aDetalle(p, quienMira);
    }

    // ---------- Reclamos ("Es mío" / "Lo encontré") ----------

    @Transactional
    public ReclamoResponseDto reclamar(Usuario reclamante, Long publicacionId, ReclamoRequestDto dto) {
        Publicacion p = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));

        if (p.getPublicador().getId().equals(reclamante.getId())) {
            throw ApiException.solicitudInvalida("No podés reclamar tu propia publicación");
        }
        if (p.getEstado() != EstadoPublicacion.ABIERTA && p.getEstado() != EstadoPublicacion.EN_RECLAMO) {
            throw ApiException.solicitudInvalida("Esta publicación ya está resuelta");
        }
        if (reclamoRepository.findByPublicacionAndReclamanteAndEstado(p, reclamante, EstadoReclamo.PENDIENTE).isPresent()) {
            throw ApiException.conflicto("Ya tenés un reclamo pendiente sobre esta publicación");
        }

        Reclamo reclamo = Reclamo.builder()
                .publicacion(p)
                .reclamante(reclamante)
                .mensaje(dto.mensaje())
                .build();
        reclamoRepository.save(reclamo);

        if (p.getEstado() == EstadoPublicacion.ABIERTA) {
            p.setEstado(EstadoPublicacion.EN_RECLAMO);
            publicacionRepository.save(p);
        }

        return aReclamoDto(reclamo);
    }

    public List<ReclamoResponseDto> listarReclamos(Usuario usuario, Long publicacionId) {
        Publicacion p = obtenerPropia(usuario, publicacionId);
        return reclamoRepository.findByPublicacion(p).stream().map(this::aReclamoDto).toList();
    }

    @Transactional
    public void aceptarReclamo(Usuario usuario, Long publicacionId, Long reclamoId) {
        Publicacion p = obtenerPropia(usuario, publicacionId);
        Reclamo reclamo = obtenerReclamoDe(p, reclamoId);

        reclamo.setEstado(EstadoReclamo.ACEPTADO);
        reclamo.setFechaResolucion(LocalDateTime.now());
        reclamoRepository.save(reclamo);

        // Los demás reclamos pendientes sobre la misma publicación quedan rechazados.
        reclamoRepository.findByPublicacion(p).stream()
                .filter(r -> !r.getId().equals(reclamo.getId()) && r.getEstado() == EstadoReclamo.PENDIENTE)
                .forEach(r -> {
                    r.setEstado(EstadoReclamo.RECHAZADO);
                    r.setFechaResolucion(LocalDateTime.now());
                    reclamoRepository.save(r);
                });

        p.setEstado(EstadoPublicacion.RESUELTA);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
    }

    @Transactional
    public void rechazarReclamo(Usuario usuario, Long publicacionId, Long reclamoId) {
        Publicacion p = obtenerPropia(usuario, publicacionId);
        Reclamo reclamo = obtenerReclamoDe(p, reclamoId);

        reclamo.setEstado(EstadoReclamo.RECHAZADO);
        reclamo.setFechaResolucion(LocalDateTime.now());
        reclamoRepository.save(reclamo);

        boolean quedanPendientes = reclamoRepository.findByPublicacion(p).stream()
                .anyMatch(r -> r.getEstado() == EstadoReclamo.PENDIENTE);
        if (!quedanPendientes && p.getEstado() == EstadoPublicacion.EN_RECLAMO) {
            p.setEstado(EstadoPublicacion.ABIERTA);
            publicacionRepository.save(p);
        }
    }

    private Reclamo obtenerReclamoDe(Publicacion p, Long reclamoId) {
        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> ApiException.noEncontrado("Reclamo no encontrado"));
        if (!reclamo.getPublicacion().getId().equals(p.getId())) {
            throw ApiException.solicitudInvalida("Ese reclamo no pertenece a esta publicación");
        }
        return reclamo;
    }

    // ---------- Seguir una publicación ----------

    @Transactional
    public void seguir(Usuario usuario, Long publicacionId) {
        Publicacion p = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        if (seguimientoRepository.existsByUsuarioAndPublicacion(usuario, p)) return;
        seguimientoRepository.save(Seguimiento.builder().usuario(usuario).publicacion(p).estadoVisto(p.getEstado()).build());
    }

    @Transactional
    public List<PublicacionSeguidaDto> seguidas(Usuario usuario) {
        return seguimientoRepository.findByUsuarioOrderByFechaCreacionDesc(usuario).stream()
                .map(s -> {
                    Publicacion p = s.getPublicacion();
                    // Seguimientos creados antes de guardar estadoVisto toman el estado actual como referencia.
                    if (s.getEstadoVisto() == null) {
                        s.setEstadoVisto(p.getEstado());
                        seguimientoRepository.save(s);
                    }
                    boolean cambioEstado = s.getEstadoVisto() != p.getEstado();
                    return new PublicacionSeguidaDto(
                            p.getId(), p.getTipo(), p.getCategoria(), p.getTitulo(), p.getBarrio(), p.getFechaSuceso(),
                            p.getLatitud(), p.getLongitud(),
                            p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                            p.getEstado(), p.getFechaPublicacion(), p.getPublicador().getNombre(),
                            cambioEstado
                    );
                })
                .toList();
    }

    @Transactional
    public void marcarSeguidaVista(Usuario usuario, Long publicacionId) {
        Publicacion p = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        Seguimiento s = seguimientoRepository.findByUsuarioAndPublicacion(usuario, p)
                .orElseThrow(() -> ApiException.noEncontrado("No seguís esta publicación"));
        s.setEstadoVisto(p.getEstado());
        seguimientoRepository.save(s);
    }

    @Transactional
    public void dejarDeSeguir(Usuario usuario, Long publicacionId) {
        Publicacion p = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        seguimientoRepository.findByUsuarioAndPublicacion(usuario, p).ifPresent(seguimientoRepository::delete);
    }

    // ---------- Helpers ----------

    private Publicacion obtenerPropia(Usuario usuario, Long id) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        if (!p.getPublicador().getId().equals(usuario.getId())) {
            throw ApiException.noAutorizado("Esta publicación no te pertenece");
        }
        return p;
    }

    private PublicacionResumenDto aResumen(Publicacion p) {
        return new PublicacionResumenDto(
                p.getId(), p.getTipo(), p.getCategoria(), p.getTitulo(), p.getBarrio(), p.getFechaSuceso(),
                p.getLatitud(), p.getLongitud(),
                p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                p.getEstado(), p.getFechaPublicacion(), p.getPublicador().getNombre(),
                p.getFechaVencimiento()
        );
    }

    private PublicacionDetalleDto aDetalle(Publicacion p, Usuario quienMira) {
        boolean esPropia = quienMira != null && quienMira.getId().equals(p.getPublicador().getId());

        EstadoReclamo miReclamoEstado = null;
        boolean direccionVisible = esPropia;
        if (quienMira != null && !esPropia) {
            var miReclamo = reclamoRepository.findByPublicacionAndReclamanteAndEstado(p, quienMira, EstadoReclamo.ACEPTADO);
            if (miReclamo.isPresent()) {
                miReclamoEstado = EstadoReclamo.ACEPTADO;
                direccionVisible = true;
            } else {
                miReclamoEstado = reclamoRepository.findByPublicacion(p).stream()
                        .filter(r -> r.getReclamante().getId().equals(quienMira.getId()))
                        .filter(r -> r.getEstado() == EstadoReclamo.PENDIENTE)
                        .findFirst().map(Reclamo::getEstado).orElse(null);
            }
        }

        boolean siguiendo = quienMira != null && seguimientoRepository.existsByUsuarioAndPublicacion(quienMira, p);

        long devoluciones = reputacionService.devolucionesConcretadas(p.getPublicador());
        long positivas = reputacionService.valoracionesPositivas(p.getPublicador());
        long negativas = reputacionService.valoracionesNegativas(p.getPublicador());
        double reputacion = (positivas + negativas) == 0 ? 0.0 : (positivas * 1.0) / (positivas + negativas) * 5.0;

        return new PublicacionDetalleDto(
                p.getId(), p.getTipo(), p.getCategoria(), p.getTitulo(), p.getDescripcion(), p.getPalabrasClave(),
                p.getFechaSuceso(), p.getFranjaHoraria(), p.getBarrio(), p.getLatitud(), p.getLongitud(),
                direccionVisible ? p.getDireccionExacta() : null,
                p.getEstado(), p.getFechaPublicacion(),
                p.getFotos().stream().map(Foto::getUrl).toList(),
                p.getPublicador().getId(), p.getPublicador().getNombre(), Math.round(reputacion * 10.0) / 10.0,
                esPropia, siguiendo, miReclamoEstado,
                p.getHoraSuceso(),
                p.getPreguntaVerificacion(),
                esPropia ? p.getRespuestaVerificacion() : null,
                p.getFechaVencimiento()
        );
    }

    private ReclamoResponseDto aReclamoDto(Reclamo r) {
        long positivas = reputacionService.valoracionesPositivas(r.getReclamante());
        long negativas = reputacionService.valoracionesNegativas(r.getReclamante());
        double reputacion = (positivas + negativas) == 0 ? 0.0 : (positivas * 1.0) / (positivas + negativas) * 5.0;

        return new ReclamoResponseDto(
                r.getId(), r.getPublicacion().getId(), r.getReclamante().getId(), r.getReclamante().getNombre(),
                Math.round(reputacion * 10.0) / 10.0, r.getMensaje(), r.getEstado(), r.getFechaCreacion()
        );
    }
}