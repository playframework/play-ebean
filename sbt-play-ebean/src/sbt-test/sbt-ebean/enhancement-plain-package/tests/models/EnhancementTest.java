/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.ebean.bean.EntityBean;
import org.junit.Test;

public class EnhancementTest {

  @Test
  public void enhancesOnlyTheConfiguredPackage() {
    assertTrue(EntityBean.class.isAssignableFrom(models.Task.class));
    assertFalse(EntityBean.class.isAssignableFrom(other.Other.class));
  }
}
