/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #content
// ###replace: package models;
package javaguide.ebean;

import io.ebean.*;
import jakarta.persistence.*;
import java.util.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class Task extends Model {

  @Id
  @Constraints.Min(10)
  private Long id;

  @Constraints.Required private String name;

  private boolean done;

  @Formats.DateTime(pattern = "dd/MM/yyyy")
  private Date dueDate = new Date();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDone() {
    return done;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public static final Finder<Long, Task> find = new Finder<>(Task.class);
}
// #content
