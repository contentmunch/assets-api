package com.contentmunch.assets.controller;

import com.contentmunch.assets.data.video.VideoMetadata;
import com.contentmunch.assets.exception.AssetNotFoundException;
import com.contentmunch.assets.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/videos", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    private final VideoService videoService;

    @GetMapping("/{id}")
    public ResponseEntity<VideoMetadata> get(@PathVariable String id) {
        var video = videoService.getMetadata(id);
        if (video.isPresent())
            return ResponseEntity.ok(video.get());
        else
            throw new AssetNotFoundException("Video with Id: " + id + " not found");
    }

}
