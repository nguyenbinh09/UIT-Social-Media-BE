package com.example.demo.repositories;

import com.example.demo.models.Follow;
import com.example.demo.models.User;
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

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :userId")
    Long countFollowersByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    Long countFollowingByUserId(@Param("userId") String userId);

    @Query("""
                SELECT DISTINCT u 
                FROM Follow f1
                JOIN Follow f2 ON f1.followed.id = f2.follower.id
                JOIN User u ON u.id = f2.followed.id
                WHERE f1.follower.id = :currentUserId 
                  AND u.id != :currentUserId
                  AND u.id NOT IN (
                    SELECT f.followed.id FROM Follow f WHERE f.follower.id = :currentUserId
                  )
            """)
    List<User> findFriendsOfFriends(@Param("currentUserId") String currentUserId);
}
