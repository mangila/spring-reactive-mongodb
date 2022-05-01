package com.github.mangila.springreactivemongodb;


import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService {

    private final ReactiveGridFsTemplate template;
    private final UserRepository userRepository;

    public Mono<UserDto> upload(String username, List<FilePart> files) {
        return Mono.when(insertUser(username), insertFiles(files, username))
                .then(findUserByUsername(username));
    }

    public Mono<UserDto> findUserByUsername(String username) {
        return Mono.zip(
                userRepository.findByUsername(username),
                findAllFilesByUsername(username).collectList(),
                this::toDto
        );
    }

    public Flux<DataBuffer> downloadFileByUuid(String uuid, String username) {
        return template.find(new Query(where("filename").is(uuid)
                        .andOperator(where("metadata.username").is(username))))
                .flatMap(template::getResource)
                .flatMap(ReactiveGridFsResource::getDownloadStream);
    }

    private Mono<UserDocument> insertUser(String username) {
        final var u = new UserDocument();
        u.setUsername(username);
        u.setRegistered(Instant.now());
        return userRepository.insert(u);
    }

    private Flux<ObjectId> insertFiles(List<FilePart> files, String username) {
        return Flux.fromIterable(files).flatMap(filePart -> store(filePart, username));
    }

    private Mono<ObjectId> store(FilePart filePart, String username) {
        var metadata = BasicDBObjectBuilder
                .start()
                .append("original-name", filePart.filename())
                .append("username", username)
                .get();
        return template.store(filePart.content(), username + "-" + UUID.randomUUID(), metadata);
    }

    private Flux<GridFSFile> findAllFilesByUsername(String username) {
        return template.find(new Query(where("metadata.username")
                .is(username)));
    }

    private UserDto toDto(UserDocument userDocument, List<GridFSFile> files) {
        return UserDto.builder()
                .username(userDocument.getUsername())
                .registered(userDocument.getRegistered())
                .fileUuids(files.stream().map(GridFSFile::getFilename).toList())
                .build();
    }

}
