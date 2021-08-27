package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Optional.ofNullable;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/asset", produces = "application/json")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;


    @GetMapping("/{id}")
    public ResponseEntity<Asset> get(@PathVariable String id) {
        return ResponseEntity.ok(assetService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        assetService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> put(@PathVariable String id,
                                     @RequestParam MultipartFile file,
                                     @RequestParam String name,
                                     @RequestParam(required = false) String description) {

        return ResponseEntity.ok(assetService.update(id, file, name, ofNullable(description)));

    }
}
