package com.hallado.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hallado.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

        public String subirImagen(MultipartFile archivo) {
        return subirImagen(archivo, "ronda/publicaciones");
    }

    public String subirImagen(MultipartFile archivo, String carpeta) {
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(archivo.getBytes(),
                    ObjectUtils.asMap("folder", carpeta));
            return (String) resultado.get("secure_url");
        } catch (IOException e) {
            throw ApiException.solicitudInvalida("No se pudo subir la imagen: " + e.getMessage());
        }
    }
}
