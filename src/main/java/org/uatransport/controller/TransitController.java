package org.uatransport.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uatransport.entity.Transit;
import org.uatransport.entity.dto.TransitDTO;
import org.uatransport.service.TransitService;

import java.util.List;

@RestController
@RequestMapping("/transit")
@CrossOrigin
@RequiredArgsConstructor
public class TransitController {

    private final TransitService transitService;
    private final ModelMapper modelMapper;

    @GetMapping(params = "id")
    public ResponseEntity<TransitDTO> getTransitById(@RequestParam("id") Integer id) {
        TransitDTO transitDTO = modelMapper.map(transitService.getById(id), TransitDTO.class);
        return new ResponseEntity<>(transitDTO, HttpStatus.OK);
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
        return new ResponseEntity<>(transitService.add(transit), HttpStatus.CREATED);
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