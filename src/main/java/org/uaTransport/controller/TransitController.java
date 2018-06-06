package org.uaTransport.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uaTransport.entity.Transit;
import org.uaTransport.exception.ResourceNotFoundException;
import org.uaTransport.repository.NonExtendableCategoryRepository;
import org.uaTransport.service.TransitService;

import java.util.List;

@RestController
@RequestMapping("/transit")
@CrossOrigin
@RequiredArgsConstructor
public class TransitController {

    private final TransitService transitService;
    private final NonExtendableCategoryRepository nonExtendableCategoryRepository;

    @GetMapping(params = "id")
    public ResponseEntity<Transit> getTransitById(@RequestParam("id") Integer id) {
        return new ResponseEntity<>(transitService.getById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Transit>> getAllTransits() {
        return new ResponseEntity<>(transitService.getAll(), HttpStatus.OK);
    }

    @GetMapping(params = "categoryId")
    public ResponseEntity<List<Transit>> getTransitsByCategoryId(@RequestParam("categoryId") Integer categoryId) {
        return new ResponseEntity<>(transitService.getAllByCategoryId(categoryId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Transit> addTransit(@RequestBody Transit transit) {
        Transit savedTransit;
        Integer categoryId = transit.getCategory().getId();
        if (nonExtendableCategoryRepository.existsById(categoryId)) {
            savedTransit = transitService.addTransit(transit);
        } else {
            throw new ResourceNotFoundException(String.format("Category with id '%s' not found", categoryId));
        }

        return new ResponseEntity<>(savedTransit, HttpStatus.CREATED);
    }

    @DeleteMapping(params = "id")
    public void deleteTransit(@RequestParam("id") Integer id) {
        transitService.delete(id);
    }

    @PutMapping(params = "id")
    public ResponseEntity<Transit> updateTransit(@RequestBody Transit transit, @RequestParam("id") Integer id) {
        Transit updatedTransit = transitService.update(transit.setId(id));
        return new ResponseEntity<>(updatedTransit, HttpStatus.OK);
    }
}