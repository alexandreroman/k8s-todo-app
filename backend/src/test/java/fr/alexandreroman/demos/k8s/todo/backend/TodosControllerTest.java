/*
 * Copyright (c) 2021 VMware, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.k8s.todo.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TodosControllerTest {
    @Autowired
    private TestRestTemplate rest;
    @MockBean
    private TodoRepository repo;

    @Test
    void testGetTodosEmpty() {
        when(repo.findAll()).thenReturn(Collections.emptyList());
        assertThat(rest.getForEntity("/api/todos", TodoDTO[].class).getBody()).isEmpty();
    }

    @Test
    void testGetTodosSingle() {
        when(repo.findAll())
                .thenReturn(List.of(new TodoEntity("1", "Hello", true)));
        assertThat(rest.getForEntity("/api/todos", TodoDTO[].class).getBody())
                .containsExactly(new TodoDTO("1", "Hello", true));
    }

    @Test
    void testGetTodos() {
        when(repo.findAll()).thenReturn(List.of(
                new TodoEntity("1", "Hello", true),
                new TodoEntity("2", "Hi", false)));
        assertThat(rest.getForEntity("/api/todos", TodoDTO[].class).getBody()).containsExactly(
                new TodoDTO("1", "Hello", true),
                new TodoDTO("2", "Hi", false));
    }

    @Test
    void testPutTodosNone() {
        rest.put("/api/todos", new TodoDTO[]{
                new TodoDTO("1", "Hello", true)
        });
        verify(repo, never()).save(any(TodoEntity.class));
    }

    @Test
    void testPutTodosSingle() {
        rest.put("/api/todos", new TodoDTO[]{new TodoDTO("1", "Hello", true)});
        verify(repo, times(1))
                .saveAllAndFlush(List.of(new TodoEntity("1", "Hello", true)));
    }

    @Test
    void testPutTodos() {
        rest.put("/api/todos", new TodoDTO[]{
                new TodoDTO("1", "Hello", true),
                new TodoDTO("2", "Hi", false)});
        verify(repo, times(1)).saveAllAndFlush(List.of(
                new TodoEntity("1", "Hello", true),
                new TodoEntity("2", "Hi", false)));
    }
}
