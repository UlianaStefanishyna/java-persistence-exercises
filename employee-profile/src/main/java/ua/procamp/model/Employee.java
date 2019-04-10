package ua.procamp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;

/**
 * todo:
 * - implement no argument constructor
 * - implement getters and setters
 * - implement equals and hashCode based on identifier field
 * <p>
 * - configure JPA entity
 * - specify table name: "employee"
 * - configure auto generated identifier
 * - configure not nullable columns: email, firstName, lastName
 * <p>
 * - map unidirectional relation between {@link Employee} and {@link EmployeeProfile} on the child side
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fistName;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(mappedBy = "employee", fetch = LAZY)
    private EmployeeProfile employeeProfile;
}
