package com.observatory.observatorysystem.controller;

import com.observatory.observatorysystem.entity.Telescope;
import com.observatory.observatorysystem.exception.ResourceNotFoundException;
import com.observatory.observatorysystem.repository.TelescopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telescopes")
public class TelescopeController {

    @Autowired
    private TelescopeRepository telescopeRepository;

    // 1. GET - получить все телескопы
    @GetMapping
    public List<Telescope> getAllTelescopes() {
        return telescopeRepository.findAll();
    }

    // 2. GET - получить телескоп по ID
    @GetMapping("/{id}")
    public Telescope getTelescopeById(@PathVariable Long id) {
        return telescopeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Телескоп с ID " + id + " не найдена"));
    }

    // 3. POST - создать новый телескоп
    @PostMapping
    public Telescope createTelescope(@RequestBody Telescope telescope) {
        return telescopeRepository.save(telescope);
    }

    // 4. PUT - обновить телескоп
    @PutMapping("/{id}")
    public Telescope updateTelescope(@PathVariable Long id,
                                     @RequestBody Telescope telescopeDetails) {
        Telescope telescope = telescopeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Телескоп с ID " + id + " не найдена"));

        telescope.setName(telescopeDetails.getName());
        telescope.setType(telescopeDetails.getType());
        telescope.setIsOperational(telescopeDetails.getIsOperational());
        telescope.setLocation(telescopeDetails.getLocation());

        return telescopeRepository.save(telescope);
    }

    // 5. DELETE - удалить телескоп
    @DeleteMapping("/{id}")
    public String deleteTelescope(@PathVariable Long id) {
        telescopeRepository.deleteById(id);
        return "Телескоп удален";
    }
}
