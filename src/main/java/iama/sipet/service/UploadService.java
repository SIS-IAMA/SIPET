package iama.sipet.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class UploadService {

    public String saveUpload(MultipartFile file, String url) throws IOException {
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();


            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String uniqueFilename = UUID.randomUUID().toString() + "-" + originalName;

            // Convertir siempre a ruta absoluta y normalizar
            Path uploadDir = Paths.get(url).toAbsolutePath().normalize();
            System.out.println("Ruta absoluta archivo en: " + uploadDir);
            // Crear directorio si no existe
            Files.createDirectories(uploadDir);

            Path path = Paths.get(uploadDir+"/"+uniqueFilename);
            System.out.println("Guardando archivo en: " + path.toAbsolutePath());
            Files.write(path, bytes);
            return uniqueFilename;
        }
        return null;
    }

    public void  deleteUpload(String nombre, String url) {
        // Convertir siempre a ruta absoluta y normalizar
        Path uploadDir = Paths.get(url).toAbsolutePath().normalize();
        System.out.println("Ruta absoluta archivo en: " + uploadDir);

        File file = new File(uploadDir+"/"+nombre);
        boolean estado = file.delete();

        if (!estado) {
            throw new RuntimeException("Error al eliminar el archivo");
        }
    }

}
