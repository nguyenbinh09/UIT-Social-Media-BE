package com.example.demo.models;

import com.example.demo.enums.PrivacyName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "privacy")
@EqualsAndHashCode(callSuper = true)
public class Privacy extends BaseModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 20, unique = true)
    private PrivacyName name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "privacy", cascade = CascadeType.ALL)
    private List<Post> posts;
}
