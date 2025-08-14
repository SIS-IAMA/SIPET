package iama.sipet.controller;


import iama.sipet.entity.PeticionesEntity;
import iama.sipet.response.PeticionesResponseRest;
import iama.sipet.service.IPeticionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/peticiones")
public class PeticionesController {
    @Autowired
    private IPeticionService peticionService;

    @PutMapping("/{id}/{estado}")
    public ResponseEntity<PeticionesResponseRest> updatePeticiones(@PathVariable Integer id, @PathVariable boolean estado, @RequestBody PeticionesEntity comentarioEntity){
        ResponseEntity<PeticionesResponseRest> response = peticionService.gestionar(id,estado,comentarioEntity);
        return response;
    }

    @GetMapping
    public ResponseEntity<PeticionesResponseRest> getAll(){
        ResponseEntity<PeticionesResponseRest> response = peticionService.getAll();
        return response;
    }

    @GetMapping("/todas/{user}")
    public ResponseEntity<PeticionesResponseRest> getByUser(@PathVariable String user){
        ResponseEntity<PeticionesResponseRest> response = peticionService.getByUser(user);
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeticionesResponseRest> getById(@PathVariable Integer id){
        ResponseEntity<PeticionesResponseRest> response = peticionService.getById(id);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PeticionesResponseRest> deletePeticion(@PathVariable Integer id){
        ResponseEntity<PeticionesResponseRest> response = peticionService.delete(id);
        return response;
    }

}
