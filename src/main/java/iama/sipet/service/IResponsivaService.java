package iama.sipet.service;


import iama.sipet.entity.ResponsivaEntity;
import iama.sipet.response.ResponsivaResponseRest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IResponsivaService {

    ResponsivaEntity create (ResponsivaEntity responsiva, MultipartFile file) throws IOException;
    ResponsivaEntity updatee (Integer id, MultipartFile file) throws  IOException;

}
