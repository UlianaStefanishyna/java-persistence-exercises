package ua.procamp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.GenerationType.*;

/**
 * todo:
 * - implement not argument constructor
 * - implement getters and setters
 * - implement equals and hashCode based on identifier field
 *
 * - configure JPA entity
 * - specify table name: "employee_profile"
 * - configure not nullable columns: position, department
 *
 * - map relation between {@link Employee} and {@link EmployeeProfile} using foreign_key column: "employee_id"
 * - configure a derived identifier. E.g. map "employee_id" column should be also a primary key (id) for this entity
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "employee_profile")
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;

    @MapsId
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
