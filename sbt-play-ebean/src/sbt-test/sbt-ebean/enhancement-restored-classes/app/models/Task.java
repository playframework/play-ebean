/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package models;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Task extends Model {

  @Id public Long id;

  public String name;
}
