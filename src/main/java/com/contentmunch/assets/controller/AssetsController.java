package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/assets", produces = "application/json")
@RequiredArgsConstructor
public class AssetsController {

    private final AssetService assetService;

    @GetMapping("/list/{folderId}")
    public ResponseEntity<CollectionModel<Asset>> list(@PathVariable String folderId,
                                                       @RequestParam(required = false, defaultValue = "100") int pageSize,
                                                       @RequestParam(required = false) String nextPageToken) {

        return ResponseEntity.ok(assetService.list(folderId, pageSize, Optional.ofNullable(nextPageToken)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> get(@PathVariable String id) {
        return ResponseEntity.ok(assetService.get(id));
    }

    @PostMapping("/")
    public ResponseEntity<Asset> post(@RequestParam String folderId,
                                      @RequestParam MultipartFile file,
                                      @RequestParam String name,
                                      @RequestParam(required = false) String id,
                                      @RequestParam(required = false) String description) {

        if (id != null) {
            return ResponseEntity.ok(assetService.update(folderId, file, id, name, ofNullable(description)));
        } else {
            return ResponseEntity.ok(assetService.create(folderId, file, name, ofNullable(description)));
        }
    }
}
