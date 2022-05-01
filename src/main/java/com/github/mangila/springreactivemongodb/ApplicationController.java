package com.github.mangila.springreactivemongodb;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService service;

    @GetMapping(
            value = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserDto>> findUserByUsername(@PathVariable String username) {
        return service.findUserByUsername(username)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(
            value = "/{username}/{uuid}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<DataBuffer> downloadFileByUuid(@PathVariable String username,
                                               @PathVariable String uuid) {
        return service.downloadFileByUuid(uuid, username)
                .switchIfEmpty(Mono.error(new ApplicationException("File does not exists")));
    }

    @PostMapping(
            value = "upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserDto>> upload(@RequestPart String username,
                                                @RequestPart List<FilePart> files) {
        return service.upload(username, files)
                .doOnSuccess(voidMethod -> log.info("{} uploaded {} files", username, files.size()))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
