package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllByOwnerId(Long userId, Pageable pageable);

    @Query(value = "SELECT * from items " +
            "where lower(name) like lower(concat('%', ?1, '%')) " +
            "or lower(description) like lower(concat('%', ?1, '%')) " +
            "and available=true",
            countQuery = "SELECT count(*) from items " +
                    "where lower(name) like lower(concat('%', ?1, '%')) " +
                    "or lower(description) like lower(concat('%', ?1, '%')) " +
                    "and available=true",
            nativeQuery = true)
    Page<Item> findItems(String text, Pageable pageable);

    @Query(value = "select i from Item i where i.request.id = ?1")
    List<Item> findItemsByRequest(Long requestId);
}