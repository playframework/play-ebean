/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import io.ebean.DB;
import java.util.concurrent.CompletionStage;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Result;

/** Wraps an action in an Ebean/DB transaction. */
public class TransactionalAction extends Action<Transactional> {
  public CompletionStage<Result> call(final Request req) {
    return DB.executeCall(() -> delegate.call(req));
  }
}
