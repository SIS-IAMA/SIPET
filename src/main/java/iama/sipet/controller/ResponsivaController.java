package iama.sipet.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import iama.sipet.entity.ResponsivaEntity;
import iama.sipet.service.IResponsivaService;

@RestController
@RequestMapping("/api/responsivas")
public class ResponsivaController {
    @Autowired
    private IResponsivaService responsivaService;


}
