/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package models;

import java.util.*;
import jakarta.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class Task extends Model {

    public static final Finder<Long, Task> find = new Finder<>(Task.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public boolean done;

    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date dueDate = new Date();
}
