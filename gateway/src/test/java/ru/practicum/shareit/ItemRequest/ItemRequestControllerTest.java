package ru.practicum.shareit.ItemRequest;

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
import ru.practicum.shareit.ItemRequest.dto.ItemRequestDto;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestClient itemRequestClient;
    @Autowired
    private ItemRequestController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void postRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setId(1L);

        when(itemRequestClient.postRequest(anyLong(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(itemRequestDto)));

        mockMvc.perform(post("/requests", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void postRequestWhenDescriptionIsEmptyThenReturnBadRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("");
        itemRequestDto.setId(1L);

        mockMvc.perform(post("/requests", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOwnRequestByUser() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setId(1L);
        List<ItemRequestDto> result = List.of(itemRequestDto);
        when(itemRequestClient.getAllOwnRequestByUser(anyLong())).thenReturn(ResponseEntity.of(Optional.of(result)));


        mockMvc.perform(get("/requests", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void getAllRequestsOtherUsers() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setId(1L);
        List<ItemRequestDto> result = List.of(itemRequestDto);
        when(itemRequestClient.getAllRequestsOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.of(Optional.of(result)));

        mockMvc.perform(get("/requests/all", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void getAllRequestsOtherUsersWithWrongFromValue() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/requests/all", 42L)
                                        .header("X-Sharer-User-Id", 1)
                                        .param("from", "-1")
                                        .param("size", "5"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is javax.validation." +
                "ConstraintViolationException: getAllRequestsOtherUsers.from: " +
                "must be greater than or equal to 0", thrown.getMessage());
    }

    @Test
    void getAllRequestsOtherUsersWithWrongSizeValue() {
        NestedServletException thrown = Assertions
                .assertThrows(NestedServletException.class, () ->
                        mockMvc.perform(get("/requests/all", 42L)
                                        .header("X-Sharer-User-Id", 1)
                                        .param("from", "1")
                                        .param("size", "-5"))
                                .andExpect(status().isBadRequest()));

        assertEquals("Request processing failed; nested exception is " +
                "javax.validation.ConstraintViolationException: " +
                "getAllRequestsOtherUsers.size: must be greater than 0", thrown.getMessage());
    }

    @Test
    void getItemRequestById() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setId(1L);
        when(itemRequestClient.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.of(Optional.of(itemRequestDto)));

        mockMvc.perform(get("/requests/1", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class));
    }
}