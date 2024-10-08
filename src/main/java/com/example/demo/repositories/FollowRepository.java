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
    @Query("SELECT f.followedId FROM Follow f WHERE f.followerId = :followerId")
    List<String> findFollowedIdsByFollowerId(@Param("followerId") String followerId);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followedId = :followedId")
    List<String> findFollowerIdsByFollowedId(@Param("followedId") String followedId);

    Optional<Follow> findByFollowerIdAndFollowedId(String followerId, String followedId);
}
