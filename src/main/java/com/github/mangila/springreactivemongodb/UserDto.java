package com.github.mangila.springreactivemongodb;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class UserDto {

    private String username;
    private Instant registered;
    private List<String> fileUuids;
}
