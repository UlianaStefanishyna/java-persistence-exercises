package ua.procamp.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Builder
public class Programs {

    private Long id;
    private String name;
    private String description;
    private Long version;
}