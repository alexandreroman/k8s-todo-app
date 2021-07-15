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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TodosController {
    private final TodoRepository repo;

    @GetMapping("/api/todos")
    @Transactional(readOnly = true)
    List<TodoDTO> getTodos() {
        return repo.findAll().stream().map(TodosController::toDTO).collect(Collectors.toList());
    }

    @PutMapping("/api/todos")
    @Transactional
    List<TodoDTO> putTodos(@RequestBody List<TodoDTO> todos) {
        log.debug("Deleting all todo entries");
        repo.deleteAll();
        log.debug("Saving todo entries: {}", todos);
        repo.saveAllAndFlush(todos.stream().map(TodosController::toEntity).collect(Collectors.toList()));
        log.info("Saved todo entries: {}", todos);
        return getTodos();
    }

    private static TodoDTO toDTO(TodoEntity e) {
        return new TodoDTO(e.getId(), e.getTitle(), e.isCompleted());
    }

    private static TodoEntity toEntity(TodoDTO dto) {
        return new TodoEntity(dto.id, dto.title, dto.completed);
    }
}
