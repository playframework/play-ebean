/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean.models.ddl;

import io.ebean.annotation.DbComment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Ebean drops the sequence of this entity with a PL/SQL block on Oracle, and the semicolons in the
 * column comment have to reach the database as they are.
 */
@Entity
public class Publisher {
  public static final String NAME_COMMENT = "Semicolons: ; and ;; stay as they are";

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @DbComment(Publisher.NAME_COMMENT)
  private String name;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
