package ua.procamp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.sql.ordering.antlr.GeneratedOrderByFragmentRendererTokenTypes;

import javax.persistence.*;

/**
 * TODO: you're job is to implement mapping for JPA entity {@link Movie}
 * - specify id
 * - configure id as auto-increment column
 * - explicitly specify each column name ("id", "name", "director", and "duration" accordingly)
 * - specify not null constraint for fields {@link Movie#name} and {@link Movie#director}
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "director")
    private String director;

    @Column(name = "duration")
    private Integer durationSeconds;
}
