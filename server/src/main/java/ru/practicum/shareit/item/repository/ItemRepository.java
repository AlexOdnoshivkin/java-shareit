package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item as i where lower(i.name) like lower(concat('%', ?1, '%')) or " +
            "lower(i.description) like lower(concat('%', ?1, '%')) order by i.id asc")
    List<Item> searchItemsBuNameAndDescription(String text, Pageable pageable);

    @Query("select i from Item as i where i.userId = ?1 order by i.id asc")
    List<Item> findItemsByUserId(Long userId, Pageable pageable);
}
