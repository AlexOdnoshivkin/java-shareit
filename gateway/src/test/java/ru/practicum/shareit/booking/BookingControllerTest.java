package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private BookingController controller;

    @MockBean
    private BookingClient bookingClient;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BookItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getBookings() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(5));
        List<BookItemRequestDto> result = List.of(requestDto);

        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.of(Optional.of(result)));
        mockMvc.perform(get("/bookings", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId", is(requestDto.getItemId()), Long.class));
    }

    @Test
    void bookItem() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(5));

        when(bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(requestDto)));

        mockMvc.perform(post("/bookings", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(requestDto.getItemId()), Long.class));
    }

    @Test
    void addBookingWhenEndInPastThenReturnBadRequest() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().minusDays(1));


        when(bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(requestDto)));

        mockMvc.perform(post("/bookings", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingWhenStartInPastThenReturnBadRequest() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().minusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.of(Optional.of(requestDto)));

        mockMvc.perform(post("/bookings", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(5));

        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(ResponseEntity.of(Optional.of(requestDto)));
        mockMvc.perform(get("/bookings/1", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(requestDto.getItemId()), Long.class));
    }

    @Test
    void getOwnerBookings() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(5));
        List<BookItemRequestDto> result = List.of(requestDto);

        when(bookingClient.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.of(Optional.of(result)));
        mockMvc.perform(get("/bookings/owner", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId", is(requestDto.getItemId()), Long.class));
    }

    @Test
    void patchBooking() throws Exception {
        requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(5));

        when(bookingClient.patchBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.of(Optional.of(requestDto)));
        mockMvc.perform(patch("/bookings/1?approved=true", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(requestDto.getItemId()), Long.class));
    }
}