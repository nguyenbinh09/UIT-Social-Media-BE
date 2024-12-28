package com.example.demo.repositories;

import com.example.demo.dtos.responses.GroupResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.Group;
import com.example.demo.models.Post;
import com.example.demo.models.PostReaction;
import com.example.demo.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

    public List<PostResponse> searchPosts(String keyword, User currentUser) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> root = query.from(Post.class);

        Predicate titlePredicate = cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        Predicate contentPredicate = cb.like(cb.lower(root.get("textContent")), "%" + keyword.toLowerCase() + "%");

        query.select(root).where(cb.or(titlePredicate, contentPredicate));
        List<Post> posts = entityManager.createQuery(query).getResultList();

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap);
    }

    public List<UserResponse> searchUsers(String keyword) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%");
        Predicate emailPredicate = cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%");
        Predicate nickNamePredicate = cb.like(cb.lower(root.join("profile").get("nickName")), "%" + keyword.toLowerCase() + "%");
        Predicate tagNamePredicate = cb.like(cb.lower(root.join("profile").get("tagName")), "%" + keyword.toLowerCase() + "%");

        query.select(root).where(cb.or(usernamePredicate, emailPredicate, nickNamePredicate, tagNamePredicate));
        List<User> users = entityManager.createQuery(query).getResultList();

        return users.stream()
                .map(user -> new UserResponse().toDTO(user))
                .collect(Collectors.toList());
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
