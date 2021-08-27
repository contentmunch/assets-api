package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/assets", produces = "application/json")
@RequiredArgsConstructor
public class AssetsController {

    private final AssetService assetService;

    @GetMapping("/{folderId}")
    public ResponseEntity<CollectionModel<Asset>> list(@PathVariable String folderId,
                                                       @RequestParam(required = false, defaultValue = "100") int pageSize,
                                                       @RequestParam(required = false) String nextPageToken) {

        return ResponseEntity.ok(assetService.list(folderId, pageSize, Optional.ofNullable(nextPageToken)));
    }


}
