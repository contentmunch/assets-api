package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
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
        return ResponseEntity.ok(assetService.get(id));
    }

}
