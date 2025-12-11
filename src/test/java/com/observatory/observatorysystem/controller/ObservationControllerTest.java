package com.observatory.observatorysystem.controller;

import com.observatory.observatorysystem.entity.ObservationRequest;
import com.observatory.observatorysystem.entity.ResearchProgram;
import com.observatory.observatorysystem.entity.Telescope;
import com.observatory.observatorysystem.entity.User;
import com.observatory.observatorysystem.repository.ObservationRequestRepository;
import com.observatory.observatorysystem.repository.ResearchProgramRepository;
import com.observatory.observatorysystem.repository.TelescopeRepository;
import com.observatory.observatorysystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObservationControllerTest {

    @Mock
    private ObservationRequestRepository observationRepository;

    @Mock
    private ResearchProgramRepository programRepository;

    @Mock
    private TelescopeRepository telescopeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ObservationController observationController;

    private ObservationRequest testObservation;
    private ResearchProgram testProgram;
    private Telescope testTelescope;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Иван Петров");
        testUser.setUsername("scientist1");
        testUser.setRole("SCIENTIST");

        testProgram = new ResearchProgram();
        testProgram.setId(1L);
        testProgram.setName("Изучение экзопланет");

        testTelescope = new Telescope();
        testTelescope.setId(1L);
        testTelescope.setName("БТА-1");
        testTelescope.setType("OPTICAL");

        testObservation = new ObservationRequest();
        testObservation.setId(1L);
        testObservation.setObjectName("TRAPPIST-1");
        testObservation.setCoordinates("23h 06m 29.0s -05° 02′ 29″");
        testObservation.setSpectralRange("VISIBLE");
        testObservation.setRequestedStart(LocalDateTime.now().plusDays(1));
        testObservation.setRequestedEnd(LocalDateTime.now().plusDays(1).plusHours(3));
        testObservation.setPriority(1);
        testObservation.setStatus("PENDING");
        testObservation.setProgram(testProgram);
        testObservation.setTelescope(testTelescope);
        testObservation.setUser(testUser);
    }

    @Test
    void testGetAllObservations() {
        // Arrange
        List<ObservationRequest> observations = Collections.singletonList(testObservation);
        when(observationRepository.findAll()).thenReturn(observations);

        // Act
        List<Map<String, Object>> result = observationController.getAllObservations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> firstObservation = result.get(0);
        assertEquals("TRAPPIST-1", firstObservation.get("objectName"));
        assertEquals("Изучение экзопланет", firstObservation.get("programName"));
        assertEquals("БТА-1", firstObservation.get("telescopeName"));
        assertEquals("Иван Петров", firstObservation.get("userName"));
    }

    @Test
    void testGetObservationById_Success() {
        // Arrange
        when(observationRepository.findById(1L)).thenReturn(Optional.of(testObservation));

        // Act
        ResponseEntity<Map<String, Object>> response = observationController.getObservationById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("TRAPPIST-1", body.get("objectName"));
        assertEquals("PENDING", body.get("status"));
    }

    @Test
    void testGetObservationById_NotFound() {
        // Arrange
        when(observationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.observatory.observatorysystem.exception.ResourceNotFoundException.class,
                () -> observationController.getObservationById(999L));
    }

    @Test
    void testGetObservationsByUser() {
        // Arrange
        List<ObservationRequest> userObservations = Arrays.asList(testObservation);
        when(observationRepository.findByUserId(1L)).thenReturn(userObservations);

        // Act
        List<Map<String, Object>> result = observationController.getObservationsByUser(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("TRAPPIST-1", result.get(0).get("objectName"));
    }

    @Test
    void testGetStatistics() {
        // Arrange
        List<ObservationRequest> allObservations = Arrays.asList(
                createObservation("PENDING"),
                createObservation("APPROVED"),
                createObservation("COMPLETED")
        );
        when(observationRepository.findAll()).thenReturn(allObservations);

        // Act
        Map<String, Object> stats = observationController.getStatistics();

        // Assert
        assertEquals(3, stats.get("totalRequests"));
        assertEquals(1, stats.get("pendingRequests"));
        assertEquals(1, stats.get("completedRequests"));
        assertNotNull(stats.get("programStats"));
        assertNotNull(stats.get("telescopeStats"));
        assertNotNull(stats.get("statusStats"));
    }

    @Test
    void testCreateObservation() {
        // Arrange
        ObservationController.ObservationRequestDto dto = new ObservationController.ObservationRequestDto();
        dto.setProgramId(1L);
        dto.setTelescopeId(1L);
        dto.setUserId(1L);
        dto.setObjectName("Новый объект");
        dto.setCoordinates("00h 00m 00.0s +00° 00′ 00″");
        dto.setStatus("PENDING");
        dto.setPriority(2);
        dto.setRequestedStart(LocalDateTime.now().plusDays(2));
        dto.setRequestedEnd(LocalDateTime.now().plusDays(2).plusHours(4));

        when(programRepository.findById(1L)).thenReturn(Optional.of(testProgram));
        when(telescopeRepository.findById(1L)).thenReturn(Optional.of(testTelescope));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(observationRepository.save(any(ObservationRequest.class))).thenReturn(testObservation);

        // Act
        ResponseEntity<Map<String, Object>> response = observationController.createObservation(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Заявка успешно создана", body.get("message"));
        verify(observationRepository, times(1)).save(any(ObservationRequest.class));
    }

    @Test
    void testUpdateObservation() {
        // Arrange
        ObservationController.ObservationRequestDto dto = new ObservationController.ObservationRequestDto();
        dto.setStatus("APPROVED");
        dto.setResultDescription("Наблюдение одобрено");

        when(observationRepository.findById(1L)).thenReturn(Optional.of(testObservation));
        when(observationRepository.save(any(ObservationRequest.class))).thenReturn(testObservation);

        // Act
        ResponseEntity<Map<String, Object>> response = observationController.updateObservation(1L, dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Заявка успешно обновлена", body.get("message"));
        assertEquals("APPROVED", testObservation.getStatus());
    }

    @Test
    void testDeleteObservation() {
        // Arrange
        when(observationRepository.findById(1L)).thenReturn(Optional.of(testObservation));
        doNothing().when(observationRepository).delete(testObservation);

        // Act
        ResponseEntity<Map<String, Object>> response = observationController.deleteObservation(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(observationRepository, times(1)).delete(testObservation);
    }

    @Test
    void testFilterObservations() {
        // Arrange
        List<ObservationRequest> allObservations = Arrays.asList(
                createObservation("PENDING", 1L, 1),
                createObservation("APPROVED", 2L, 2),
                createObservation("COMPLETED", 1L, 1)
        );
        when(observationRepository.findAll()).thenReturn(allObservations);

        // Act
        List<Map<String, Object>> result = observationController.filterObservations(
                1L,  // telescopeId
                "2024-01-01",  // startDate
                "2024-12-31",  // endDate
                1,  // priority
                "PENDING"  // status
        );

        // Assert
        assertEquals(1, result.size());
        Map<String, Object> filtered = result.get(0);
        assertEquals("PENDING", filtered.get("status"));
        assertEquals(1, filtered.get("priority"));
    }

    @Test
    void testFilterObservations_NoFilters() {
        // Arrange
        List<ObservationRequest> allObservations = Arrays.asList(
                createObservation("PENDING"),
                createObservation("APPROVED")
        );
        when(observationRepository.findAll()).thenReturn(allObservations);

        // Act
        List<Map<String, Object>> result = observationController.filterObservations(
                null, null, null, null, null
        );

        // Assert
        assertEquals(2, result.size());
    }

    private ObservationRequest createObservation(String status) {
        return createObservation(status, 1L, 1);
    }

    private ObservationRequest createObservation(String status, Long telescopeId, Integer priority) {
        ObservationRequest obs = new ObservationRequest();
        obs.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        obs.setObjectName("Тестовый объект");
        obs.setStatus(status);
        obs.setPriority(priority);
        obs.setRequestedStart(LocalDateTime.now().minusDays(5));
        obs.setRequestedEnd(LocalDateTime.now().minusDays(5).plusHours(3));

        Telescope telescope = new Telescope();
        telescope.setId(telescopeId);
        telescope.setName("Телескоп " + telescopeId);
        obs.setTelescope(telescope);

        ResearchProgram program = new ResearchProgram();
        program.setId(1L);
        program.setName("Тестовая программа");
        obs.setProgram(program);

        User user = new User();
        user.setId(1L);
        user.setFullName("Тестовый ученый");
        obs.setUser(user);

        return obs;
    }
}