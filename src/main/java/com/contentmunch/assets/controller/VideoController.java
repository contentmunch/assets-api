package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.video.VideoAsset;
import com.contentmunch.assets.data.video.VideoAssets;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    private final VideoService videoService;

    @GetMapping("/videos/{id}")
    public ResponseEntity<VideoAsset> get(@PathVariable String id) {
        var video = videoService.getVideo(id);
        if (video.isPresent()) return ResponseEntity.ok(video.get());
        else throw new AssetNotFoundException("Video with Id: " + id + " not found");
    }


    @GetMapping("/{folderId}/videos")
    public ResponseEntity<VideoAssets> getByFolderId(
            @PathVariable String folderId,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String nextPageToken) {

        return ResponseEntity.ok(videoService.findVideosByFolderId(folderId, pageSize, nextPageToken));
    }

    @GetMapping("/{folderId}/video")
    public ResponseEntity<VideoAsset> getByFolderIdAndName(
            @PathVariable String folderId,
            @RequestParam String name) {

        final var video = videoService.findVideoBy(folderId, name);
        if (video.isPresent()) return ResponseEntity.ok(video.get());
        else throw new AssetNotFoundException("Video with name: " + name + " not found in folder: " + folderId);
    }

}
