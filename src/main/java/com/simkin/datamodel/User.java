package com.simkin.datamodel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "users")
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class User {

    @Id
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
}
