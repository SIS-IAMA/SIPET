package iama.sipet.service;

import iama.sipet.entity.UserEntity;
import iama.sipet.response.UserResponseRest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IOperadorService {

    public ResponseEntity<UserResponseRest> buscarOperadores();
    public ResponseEntity<UserResponseRest> buscarPorId(Integer id);
    public ResponseEntity<UserResponseRest> crear(UserEntity userEntity, MultipartFile file) throws IOException;
    public ResponseEntity<UserResponseRest> actualizar( UserEntity userEntity, Integer id, MultipartFile file) throws IOException;
    public ResponseEntity<UserResponseRest> eliminar( Integer id);

    public ResponseEntity<UserResponseRest> updatePassword(String username, String password);
    public ResponseEntity<UserResponseRest> updateUser(String username, String user);

    public ResponseEntity<UserResponseRest> buscarUsuario(String user);
}