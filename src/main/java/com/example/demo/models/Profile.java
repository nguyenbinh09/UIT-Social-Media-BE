package com.example.demo.models;

import com.example.demo.enums.GenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "profiles")
@EqualsAndHashCode(callSuper = true)
public class Profile extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender")
    private GenderType gender;

    @Column(name = "bio")
    private String bio;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private Student student;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private Lecturer lecturer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_avatar_id", nullable = false)
    private MediaFile profileAvatar;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_background_id")
    private MediaFile profileBackground;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Skill> skills;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "information_detail_id", nullable = false)
    private InformationDetail informationDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
}
