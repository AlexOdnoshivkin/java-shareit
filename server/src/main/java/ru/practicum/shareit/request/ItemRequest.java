package ru.practicum.shareit.request;

import lombok.*;
import ru.practicum.shareit.item.model.Item;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item_requests")
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "description")
    private String description;
    @CollectionTable(name = "users", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "owner_id")
    private Long ownerId;
    @Column(name = "created")
    private LocalDateTime created;
    @OneToMany (mappedBy = "itemRequest")
    private List<Item> items = new ArrayList<>();

}
