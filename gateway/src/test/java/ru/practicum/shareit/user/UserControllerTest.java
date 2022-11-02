package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockBean
    private UserClient userClient;
    @Autowired
    private UserController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void postUser() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test Name");
        userDto.setEmail("Test@gmail.com");
        userDto.setId(1L);

        when(userClient.postUser(any())).thenReturn(ResponseEntity.of(Optional.of(userDto)));

        mockMvc.perform(post("/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void postUserWhenNameIsEmptyTHenReturnBadRequest() throws Exception {
        userDto = new UserDto();
        userDto.setName("");
        userDto.setEmail("Test@gmail.com");

        mockMvc.perform(post("/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUserWhenEmailIsEmptyTHenReturnBadRequest() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test");
        userDto.setEmail("");

        mockMvc.perform(post("/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUserWhenEmailIsWrongTHenReturnBadRequest() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test");
        userDto.setEmail("Test");

        mockMvc.perform(post("/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchUser() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test");
        userDto.setEmail("test");
        userDto.setId(1L);

        when(userClient.patchUser(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(userDto)));

        mockMvc.perform(patch("/users/1", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void getAllUsers() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test");
        userDto.setEmail("test");
        userDto.setId(1L);
        List<UserDto> result = List.of(userDto);

        when(userClient.getAllUsers()).thenReturn(ResponseEntity.of(Optional.of(result)));

        mockMvc.perform(get("/users", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class));
    }

    @Test
    void getUserById() throws Exception {
        userDto = new UserDto();
        userDto.setName("Test");
        userDto.setEmail("test");
        userDto.setId(1L);

        when(userClient.getUserById(anyLong())).thenReturn(ResponseEntity.of(Optional.of(userDto)));

        mockMvc.perform(get("/users/1", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void deleteUserById() throws Exception {
        when(userClient.deleteUserById(anyLong())).thenReturn(null);

        mockMvc.perform(delete("/users/1", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }
}