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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.Service.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReturnDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private BookingController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BookingReturnDto bookingReturnDto;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addBooking() throws Exception {
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(5));
        bookingReturnDto = new BookingReturnDto();
        bookingReturnDto.setId(1L);

        when(bookingService.addBooking(any(), anyLong())).thenReturn(bookingReturnDto);

        mockMvc.perform(post("/bookings", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingReturnDto.getId()), Long.class));

    }

    @Test
    void patchBooking() throws Exception {
        bookingReturnDto = new BookingReturnDto();
        bookingReturnDto.setId(1L);

        when(bookingService.patchBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingReturnDto);
        mockMvc.perform(patch("/bookings/1?approved=true", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingReturnDto.getId()), Long.class));
    }

    @Test
    void getBooking() throws Exception {
        bookingReturnDto = new BookingReturnDto();
        bookingReturnDto.setId(1L);

        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(bookingReturnDto);
        mockMvc.perform(get("/bookings/1", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingReturnDto.getId()), Long.class));
    }

    @Test
    void getUserBookings() throws Exception {
        bookingReturnDto = new BookingReturnDto();
        bookingReturnDto.setId(1L);
        List<BookingReturnDto> result = List.of(bookingReturnDto);

        when(bookingService.getUserBookingList(anyLong(), any(), anyInt(), anyInt())).thenReturn(result);
        mockMvc.perform(get("/bookings", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingReturnDto.getId()), Long.class));
    }

    @Test
    void getOwnerBookings() throws Exception {
        bookingReturnDto = new BookingReturnDto();
        bookingReturnDto.setId(1L);
        List<BookingReturnDto> result = List.of(bookingReturnDto);

        when(bookingService.getOwnerBookingList(anyLong(), any(), anyInt(), anyInt())).thenReturn(result);
        mockMvc.perform(get("/bookings/owner", 42L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingReturnDto.getId()), Long.class));
    }
}