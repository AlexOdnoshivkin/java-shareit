package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemClient itemClient;
    @Autowired
    private ItemController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }


    @Test
    void addItem() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        when(itemClient.addItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(post("/items", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class));
    }

    @Test
    void addItemWhenNameIsBlankThenReturnBadRequest() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        when(itemClient.addItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(post("/items", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWhenDescriptionIsBlankThenReturnBadRequest() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("");
        itemDto.setAvailable(true);

        when(itemClient.addItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(post("/items", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWhenAvailableIsNullThenReturnBadRequest() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");

        when(itemClient.addItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(post("/items", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test text");

        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(commentDto)));

        mockMvc.perform(post("/items/1/comment", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText()), String.class));
    }

    @Test
    void addCommentWhenTextIsEmptyThenReturnBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("");

        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(commentDto)));

        mockMvc.perform(post("/items/1/comment", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        when(itemClient.updateItem(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(patch("/items/1", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class));
    }

    @Test
    void searchItem() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);
        List<ItemDto> result = List.of(itemDto);

        when(itemClient.searchItem(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.of(Optional.of(result)));

        mockMvc.perform(get("/items/search?text=search", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));
    }

    @Test
    void searchItemWithWrongFromValue() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/items/search?text=search", 42L)
                                        .header("X-Sharer-User-Id", 1)
                                        .param("from", "-1")
                                        .param("size", "5"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                "javax.validation.ConstraintViolationException: searchItem.from:" +
                " must be greater than or equal to 0", thrown.getMessage());
    }

    @Test
    void searchItemWithWrongSizeValue() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/items/search?text=search", 42L)
                                        .header("X-Sharer-User-Id", 1)
                                        .param("from", "1")
                                        .param("size", "0"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                "javax.validation.ConstraintViolationException: searchItem.size:" +
                " must be greater than 0", thrown.getMessage());
    }

    @Test
    void getItemById() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        when(itemClient.getItemById(anyLong(), anyLong())).thenReturn(ResponseEntity.of(Optional.of(itemDto)));

        mockMvc.perform(get("/items/1", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class));
    }

    @Test
    void getUserItems() throws Exception {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);
        List<ItemDto> result = List.of(itemDto);

        when(itemClient.getUserItems(anyLong(), anyInt(), anyInt())).thenReturn(ResponseEntity.of(Optional.of(result)));

        mockMvc.perform(get("/items", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));
    }
}