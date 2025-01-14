package com.example.demo.repositories;

import com.example.demo.dtos.responses.GroupResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.*;
import com.example.demo.services.ProfileResponseBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class SearchRepository {
    private final PostReactionRepository postReactionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final SavedPostRepository savedPostRepository;
    private final ProfileResponseBuilder profileResponseBuilder;

    public List<PostResponse> searchPosts(String keyword, User currentUser) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> root = query.from(Post.class);

        Predicate titlePredicate = cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        Predicate contentPredicate = cb.like(cb.lower(root.get("textContent")), "%" + keyword.toLowerCase() + "%");

        query.select(root).where(cb.or(titlePredicate, contentPredicate));
        List<Post> posts = entityManager.createQuery(query).getResultList();

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder);
    }

    public List<UserResponse> searchUsers(String keyword) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%");
        Predicate emailPredicate = cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%");

        Join<User, Student> studentJoin = root.join("student", JoinType.LEFT);
        Join<User, Lecturer> lecturerJoin = root.join("lecturer", JoinType.LEFT);
        Join<Student, Profile> studentProfileJoin = studentJoin.join("profile", JoinType.LEFT);
        Join<Lecturer, Profile> lecturerProfileJoin = lecturerJoin.join("profile", JoinType.LEFT);

        Predicate studentNickNamePredicate = cb.like(cb.lower(studentProfileJoin.get("nickName")), "%" + keyword.toLowerCase() + "%");
        Predicate studentTagNamePredicate = cb.like(cb.lower(studentProfileJoin.get("tagName")), "%" + keyword.toLowerCase() + "%");
        Predicate studentStudentCodePredicate = cb.like(cb.lower(studentJoin.get("studentCode")), "%" + keyword.toLowerCase() + "%");

        Predicate lecturerNickNamePredicate = cb.like(cb.lower(lecturerProfileJoin.get("nickName")), "%" + keyword.toLowerCase() + "%");
        Predicate lecturerTagNamePredicate = cb.like(cb.lower(lecturerProfileJoin.get("tagName")), "%" + keyword.toLowerCase() + "%");
        Predicate lecturerLecturerCodePredicate = cb.like(cb.lower(lecturerJoin.get("lecturerCode")), "%" + keyword.toLowerCase() + "%");

        query.select(root).where(
                cb.or(
                        usernamePredicate,
                        emailPredicate,
                        studentNickNamePredicate,
                        studentTagNamePredicate,
                        studentStudentCodePredicate,
                        lecturerNickNamePredicate,
                        lecturerTagNamePredicate,
                        lecturerLecturerCodePredicate
                )
        );

        List<User> users = entityManager.createQuery(query).getResultList();

        return new UserResponse().mapUsersToDTOs(users, profileResponseBuilder);
    }


    public List<GroupResponse> searchGroups(String keyword) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> query = cb.createQuery(Group.class);
        Root<Group> root = query.from(Group.class);

        Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");

        query.select(root).where(cb.or(namePredicate, descriptionPredicate));
        List<Group> groups = entityManager.createQuery(query).getResultList();

        return new GroupResponse().mapGroupsToDTOs(groups);
    }
}
