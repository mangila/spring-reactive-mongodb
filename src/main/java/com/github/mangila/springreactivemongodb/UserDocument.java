package com.github.mangila.springreactivemongodb;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user")
@Data
@NoArgsConstructor
public class UserDocument {

    @Id
    private String username;
    private Instant registered;
}
