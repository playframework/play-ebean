/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package other;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** An entity outside of the configured package, so it must not get enhanced. */
@Entity
public class Other {
  @Id public Long id;
}
