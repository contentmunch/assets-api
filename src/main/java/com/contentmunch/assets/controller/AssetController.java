package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/asset", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/download/{fileId}")
    @Cacheable("assets")
    public ResponseEntity<Resource> getFile(@PathVariable String fileId) {

        log.debug("getting file {}", fileId);
        ByteArrayResource resource = new ByteArrayResource(assetService.getFile(fileId));

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileId)
                                .build().toString())
                .body(resource);

    }

}
