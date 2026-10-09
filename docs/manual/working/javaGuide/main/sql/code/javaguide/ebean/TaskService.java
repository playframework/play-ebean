/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #async
// ###replace: package services;
package javaguide.ebean;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.DB;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

// ###insert: import models.Task;

public class TaskService {

  private final DatabaseExecutionContext executionContext;

  @Inject
  public TaskService(DatabaseExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public CompletionStage<Task> markDone(long id) {
    return supplyAsync(
        () ->
            DB.executeCall(
                () -> {
                  // All database operations of the transaction run in here, synchronously
                  Task task = Task.find.byId(id);
                  task.setDone(true);

                  task.save();
                  return task;
                }),
        executionContext);
  }
}
// #async
