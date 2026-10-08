/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean.models.second;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SecondModel {
  @Id public Long id;
}
