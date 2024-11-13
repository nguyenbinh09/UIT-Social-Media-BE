package com.example.demo.repositories;

import com.example.demo.models.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    //    @Query("SELECT gm FROM GroupMembership gm WHERE gm.user.id = :userId AND gm.group.id = :groupId AND gm.isDeleted = false")
    Optional<GroupMembership> findByUserIdAndGroupIdAndIsDeleted(String userId, Long groupId, Boolean isDeleted);

    List<GroupMembership> findAllByGroupId(Long groupId);

    List<GroupMembership> findAdminsByGroupId(Long groupId);
}
