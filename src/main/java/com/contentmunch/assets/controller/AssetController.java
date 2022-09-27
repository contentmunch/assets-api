package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.Asset;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.service.AssetService;
import com.contentmunch.assets.service.PropertyService;
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
import org.springframework.web.client.RestTemplate;

import static com.contentmunch.assets.configuration.AssetCachingConfiguration.ASSETS_CACHE;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/asset", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;
    private final PropertyService propertyService;

    @GetMapping("/{id}")
    public ResponseEntity<Asset> get(@PathVariable String id) {
        var asset = assetService.get(id);
        if (asset.isPresent())
            return ResponseEntity.ok(asset.get());
        else
            throw new AssetNotFoundException("Asset with assetId: " + id + " not found/ or is not an image");
    }

    @GetMapping("/download/{propertyId}/{fileId}")
    public ResponseEntity<Resource> getFileFromDomain(@PathVariable String propertyId, @PathVariable String fileId) {
        var assetUrl = propertyService.propertyIdToDomain(propertyId) + "api/asset/download/" + fileId;
        return new RestTemplate().getForEntity(assetUrl, Resource.class);
    }

    @GetMapping("/download/{fileId}")
    @Cacheable(ASSETS_CACHE)
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
