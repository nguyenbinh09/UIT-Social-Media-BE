package com.example.demo.repositories;

import com.example.demo.enums.RoleName;
import com.example.demo.models.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.user.id = :userId AND gm.group.id = :groupId AND gm.isDeleted = false")
    Optional<GroupMembership> findByUserIdAndGroupId(String userId, Long groupId);

    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.id = :groupId AND gm.isDeleted = false")
    List<GroupMembership> findAllByGroupId(Long groupId);

    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.id = :groupId AND gm.role = :role AND gm.isDeleted = false")
    List<GroupMembership> findAdminsByGroupId(@Param("groupId") Long groupId, @Param("role") RoleName role);

    @Query("SELECT gm FROM GroupMembership gm WHERE gm.user.id = :userId AND gm.isDeleted = false")
    List<GroupMembership> findAllByUserId(String userId);

}
