package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query(value = "select r from ItemRequest as r where r.ownerId = ?1 order by r.created desc")
    List<ItemRequest> getItemRequestsByOwnerId(Long ownerId);

    @Query(value = "select r from ItemRequest as r where r.ownerId <> ?1 order by r.created desc")
    List<ItemRequest> getOtherUserItemRequests(Long userIdm, Pageable pageable);

    @Query(value = "select r from ItemRequest as r where r.id = ?1 order by r.created desc")
    Optional<ItemRequest> getItemRequestById(Long itemRequestId);
}
