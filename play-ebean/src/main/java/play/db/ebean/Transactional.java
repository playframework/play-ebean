/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import play.mvc.With;

/**
 * Wraps the annotated action in an Ebean transaction.
 *
 * <p>The transaction gets committed as soon as the action returns its result, or rolled back if the
 * action throws an exception. As Ebean binds the transaction to the current thread, it only covers
 * the database operations that the action runs on its own thread, before it returns. Database
 * operations that run on another thread, e.g. in {@code CompletableFuture.supplyAsync}, don't run
 * in this transaction, even if they start before the action returns, and a {@code CompletionStage}
 * that fails doesn't roll it back. To run asynchronous database operations in a transaction, begin
 * it on the thread that runs them, e.g. with {@code DB.executeCall}.
 */
@With(TransactionalAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {}
