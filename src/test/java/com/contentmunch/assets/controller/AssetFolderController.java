package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.AssetFolder;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/assetFolder", produces = "application/json")
@RequiredArgsConstructor
public class AssetFolderController {

    private final AssetService assetService;

    @PostMapping("/{folderId}")
    public ResponseEntity<AssetFolder> post(@PathVariable String folderId,
                                            @RequestParam String name) {

        return ResponseEntity.ok(assetService.createFolder(folderId, name));
    }
}
