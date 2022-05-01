package com.github.mangila.springreactivemongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByUsername(String username);
}
