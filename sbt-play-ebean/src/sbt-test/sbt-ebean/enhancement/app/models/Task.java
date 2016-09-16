package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class Task extends Model {

    public static Model.Finder<Long, Task> find = new Model.Finder<>(Task.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public boolean done;

    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date dueDate = new Date();
}
