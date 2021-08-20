package com.contentmunch.assets.controller;

import com.contentmunch.assets.configuration.AssetDriveConfig;
import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.data.Assets;
import com.contentmunch.assets.service.AssetsService;
import lombok.RequiredArgsConstructor;
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

    private final AssetsService assetsService;
    private final AssetDriveConfig assetDriveConfig;

    @GetMapping("/list")
    public ResponseEntity<Assets> list(@RequestParam(required = false) String nextPageToken) {
        return ResponseEntity.ok(assetsService.list(assetDriveConfig.getDefaultFolder(), Optional.ofNullable(nextPageToken)));
    }

    @GetMapping("/list/{folderId}")
    public ResponseEntity<Assets> list(@PathVariable String folderId,
                                       @RequestParam(required = false) String nextPageToken) {

        return ResponseEntity.ok(assetsService.list(folderId, Optional.ofNullable(nextPageToken)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        return ResponseEntity.ok(assetsService.getFile(id));
    }

    @PostMapping("/")
    public ResponseEntity<Asset> post(@RequestParam String folderId,
                                      @RequestParam MultipartFile file,
                                      @RequestParam String name,
                                      @RequestParam(required = false) String description,
                                      @RequestParam(required = false) String id) {

        if (id != null) {
            return ResponseEntity.ok(assetsService.update(folderId, file, id, name, ofNullable(description)));
        } else {
            return ResponseEntity.ok(assetsService.create(folderId, file, name, ofNullable(description)));
        }
    }
}
