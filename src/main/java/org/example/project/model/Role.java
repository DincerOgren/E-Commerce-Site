package org.example.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id")
    Integer rollId;


    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20,name="role_name")
    AppRole roleName;

    public Role(AppRole appRole) {
        this.roleName = appRole;
    }
}
