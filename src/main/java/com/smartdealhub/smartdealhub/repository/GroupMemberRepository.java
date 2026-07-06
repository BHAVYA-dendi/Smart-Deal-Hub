package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.GroupMember;
import com.smartdealhub.smartdealhub.model.GroupMemberKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberKey> {
    List<GroupMember> findByDeal_Id(Long dealId);
    List<GroupMember> findByDeal_IdAndApprovedFalse(Long dealId);
    Optional<GroupMember> findByDeal_IdAndUser_UserId(Long dealId, Long userId);
    List<GroupMember> findByUser_UserId(Long userId);
    long countByDeal_Id(Long dealId);
    long countByDeal_IdAndApprovedTrue(Long dealId);
    long countByDeal_IdAndApprovedFalse(Long dealId);
}