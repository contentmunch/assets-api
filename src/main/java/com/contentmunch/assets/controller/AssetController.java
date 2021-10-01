package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/asset", produces = "application/json")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;


    @GetMapping("/{id}")
    public ResponseEntity<Asset> get(@PathVariable String id) {
        var asset = assetService.get(id);
        if (asset.isPresent())
            return ResponseEntity.ok(asset.get());
        else
            throw new AssetNotFoundException("Asset with assetId: " + id + " not found/ or is not an image");
    }

}
