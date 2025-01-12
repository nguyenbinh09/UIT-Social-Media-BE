package com.example.demo.repositories;

import com.example.demo.models.GroupMembershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRequestRepository extends JpaRepository<GroupMembershipRequest, Long> {
    @Query("SELECT gmr FROM GroupMembershipRequest gmr WHERE gmr.user.id = :userId AND gmr.group.id = :groupId AND gmr.isDeleted = false")
    Optional<GroupMembershipRequest> findByUserIdAndGroupId(String userId, Long groupId);

    @Query("SELECT gmr FROM GroupMembershipRequest gmr WHERE gmr.group.id = :groupId AND gmr.isDeleted = false AND gmr.status = 'PENDING'")
    List<GroupMembershipRequest> findRequestsByGroupId(Long groupId);
}
