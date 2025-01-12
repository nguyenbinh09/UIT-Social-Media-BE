package com.example.demo.repositories;

import com.example.demo.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByGroupIdAndInviteeId(Long groupId, String inviteeId);

    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :id AND i.isDeleted = false AND i.status = 'PENDING'")
    List<Invitation> findByInviteeId(@Param("id") String id);

}
