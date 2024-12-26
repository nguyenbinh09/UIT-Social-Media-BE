package com.example.demo.repositories;

import com.example.demo.models.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f.followed.id FROM Follow f WHERE f.follower.id = :followerId")
    List<String> findFollowedIdsByFollowerId(@Param("followerId") String followerId);

    @Query("SELECT f.follower.id FROM Follow f WHERE f.followed.id = :followedId")
    List<String> findFollowerIdsByFollowedId(@Param("followedId") String followedId);

    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    Optional<Follow> findByFollowerIdAndFollowedId(@Param("followerId") String followerId, @Param("followedId") String followedId);

    List<Follow> findByFollowedId(String id);

    List<Follow> findByFollowerId(String id);
}
