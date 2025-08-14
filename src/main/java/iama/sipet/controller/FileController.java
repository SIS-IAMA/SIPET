package iama.sipet.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/upload")
public class FileController {

    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String filename) throws IOException {

        Path file = Paths.get("upload", folder, filename);
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("No se puede leer el archivo: " + filename);
        }

        // Detectar el tipo MIME real del archivo
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream"; // valor por defecto
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/debug/files")
    public List<String> listarArchivos() throws IOException {
        Path rootPath = Paths.get("/app");
        if (!Files.exists(rootPath)) {
            return List.of("La ruta /app no existe en este contenedor");
        }
        return Files.walk(rootPath, 9)
                .map(Path::toString)
                .toList();
    }


}
