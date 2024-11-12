package com.example.demo.repositories;

import com.example.demo.models.GroupMembershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupMembershipRequestRepository extends JpaRepository<GroupMembershipRequest, Long> {
    //    @Query("SELECT gmr FROM GroupMembershipRequest gmr WHERE gmr.user.id = :userId AND gmr.group.id = :groupId AND gmr.isDeleted = :isDeleted")
    Optional<GroupMembershipRequest> findByUserIdAndGroupIdAndIsDeleted(String userId, Long groupId, Boolean isDeleted);
}
