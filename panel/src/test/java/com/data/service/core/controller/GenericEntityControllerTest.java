package com.data.service.core.controller;

import com.data.service.core.search.MetricRequest;
import com.data.service.core.search.SearchRequest;
import com.data.service.core.service.GenericService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
class GenericEntityControllerTest {

    @Mock
    private EntityRegistry registry;

    @Mock
    private GenericService service;

    private TestController testController;

    @BeforeEach
    void setUp() {
        testController = new TestController(registry, new ObjectMapper());
        when(registry.hasEntity("tests")).thenReturn(true);
        when(registry.getService("tests")).thenReturn(service);
    }

    @Test
    void testGetAll() {
        TestModel model = new TestModel(1L, "value");
        when(service.findAll()).thenReturn(List.of(model));

        ResponseEntity<?> response = testController.getAll("tests");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(model), response.getBody());
        verify(service, times(1)).findAll();
    }

    @Test
    void testCreate() {
        TestModel model = new TestModel(1L, "value");
        when(service.getModelClass()).thenReturn((Class) TestModel.class);
        when(service.save(any(TestModel.class))).thenReturn(model);

        ResponseEntity<?> response = testController.create("tests", Map.of("id", 1L, "name", "value"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(model, response.getBody());
        verify(service, times(1)).save(any(TestModel.class));
    }

    @Test
    void testGetByIdFound() {
        TestModel model = new TestModel(1L, "value");
        when(service.findById(1L)).thenReturn(model);

        ResponseEntity<?> response = testController.getById("tests", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(model, response.getBody());
        verify(service, times(1)).findById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(service.findById(1L)).thenReturn(null);

        ResponseEntity<?> response = testController.getById("tests", 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).findById(1L);
    }

    @Test
    void testDelete() {
        ResponseEntity<Void> response = testController.delete("tests", 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).deleteById(1L);
    }

    @Test
    void testGetMetric() {
        MetricRequest request = new MetricRequest();
        when(service.getMetric(request)).thenReturn(10.5);

        ResponseEntity<?> response = testController.getMetric("tests", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.5, response.getBody());
        verify(service, times(1)).getMetric(request);
    }

    @Test
    void testQuery() {
        SearchRequest request = new SearchRequest();
        TestModel model = new TestModel(2L, "query-result");
        when(service.query(request)).thenReturn(List.of(model));

        ResponseEntity<?> response = testController.query("tests", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(model), response.getBody());
        verify(service, times(1)).query(request);
    }

    static class TestModel {
        public Long id;
        public String name;

        TestModel() {
        }

        TestModel(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class TestController extends GenericEntityController {
        TestController(EntityRegistry registry, ObjectMapper objectMapper) {
            super(registry, objectMapper);
        }
    }
}
