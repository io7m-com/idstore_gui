/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.idstore_gui.admin.internal.client;

import com.io7m.hibiscus.api.HBStateType;
import com.io7m.hibiscus.api.HBStateType.HBStateDisconnected;
import com.io7m.idstore.admin_client.api.IdAClientConfiguration;
import com.io7m.idstore.admin_client.api.IdAClientConnectionParameters;
import com.io7m.idstore.admin_client.api.IdAClientException;
import com.io7m.idstore.admin_client.api.IdAClientFactoryType;
import com.io7m.idstore.admin_client.api.IdAClientType;
import com.io7m.idstore.error_codes.IdErrorCode;
import com.io7m.idstore.error_codes.IdException;
import com.io7m.idstore.error_codes.IdStandardErrorCodes;
import com.io7m.idstore.model.IdAdmin;
import com.io7m.idstore.model.IdAdminColumn;
import com.io7m.idstore.model.IdAdminColumnOrdering;
import com.io7m.idstore.model.IdAdminCreate;
import com.io7m.idstore.model.IdAdminSearchByEmailParameters;
import com.io7m.idstore.model.IdAdminSearchParameters;
import com.io7m.idstore.model.IdAdminSummary;
import com.io7m.idstore.model.IdAuditEvent;
import com.io7m.idstore.model.IdAuditSearchParameters;
import com.io7m.idstore.model.IdBan;
import com.io7m.idstore.model.IdEmail;
import com.io7m.idstore.model.IdLogin;
import com.io7m.idstore.model.IdName;
import com.io7m.idstore.model.IdPage;
import com.io7m.idstore.model.IdPassword;
import com.io7m.idstore.model.IdRealName;
import com.io7m.idstore.model.IdTimeRange;
import com.io7m.idstore.model.IdUser;
import com.io7m.idstore.model.IdUserColumnOrdering;
import com.io7m.idstore.model.IdUserCreate;
import com.io7m.idstore.model.IdUserSearchByEmailParameters;
import com.io7m.idstore.model.IdUserSearchParameters;
import com.io7m.idstore.model.IdUserSummary;
import com.io7m.idstore.protocol.admin.IdACommandAdminCreate;
import com.io7m.idstore.protocol.admin.IdACommandAdminDelete;
import com.io7m.idstore.protocol.admin.IdACommandAdminEmailAdd;
import com.io7m.idstore.protocol.admin.IdACommandAdminEmailRemove;
import com.io7m.idstore.protocol.admin.IdACommandAdminGet;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchBegin;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchByEmailBegin;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchByEmailNext;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchByEmailPrevious;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchNext;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchPrevious;
import com.io7m.idstore.protocol.admin.IdACommandAdminSelf;
import com.io7m.idstore.protocol.admin.IdACommandAdminUpdateCredentials;
import com.io7m.idstore.protocol.admin.IdACommandAuditSearchBegin;
import com.io7m.idstore.protocol.admin.IdACommandAuditSearchNext;
import com.io7m.idstore.protocol.admin.IdACommandAuditSearchPrevious;
import com.io7m.idstore.protocol.admin.IdACommandType;
import com.io7m.idstore.protocol.admin.IdACommandUserBanCreate;
import com.io7m.idstore.protocol.admin.IdACommandUserBanDelete;
import com.io7m.idstore.protocol.admin.IdACommandUserBanGet;
import com.io7m.idstore.protocol.admin.IdACommandUserCreate;
import com.io7m.idstore.protocol.admin.IdACommandUserDelete;
import com.io7m.idstore.protocol.admin.IdACommandUserEmailAdd;
import com.io7m.idstore.protocol.admin.IdACommandUserEmailRemove;
import com.io7m.idstore.protocol.admin.IdACommandUserGet;
import com.io7m.idstore.protocol.admin.IdACommandUserGetByEmail;
import com.io7m.idstore.protocol.admin.IdACommandUserLoginHistory;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchBegin;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchByEmailBegin;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchByEmailNext;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchByEmailPrevious;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchNext;
import com.io7m.idstore.protocol.admin.IdACommandUserSearchPrevious;
import com.io7m.idstore.protocol.admin.IdACommandUserUpdateCredentials;
import com.io7m.idstore.protocol.admin.IdAResponseAdminCreate;
import com.io7m.idstore.protocol.admin.IdAResponseAdminGet;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchBegin;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchByEmailBegin;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchByEmailNext;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchByEmailPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchNext;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSelf;
import com.io7m.idstore.protocol.admin.IdAResponseAdminUpdate;
import com.io7m.idstore.protocol.admin.IdAResponseAuditSearchBegin;
import com.io7m.idstore.protocol.admin.IdAResponseAuditSearchNext;
import com.io7m.idstore.protocol.admin.IdAResponseAuditSearchPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseError;
import com.io7m.idstore.protocol.admin.IdAResponseLogin;
import com.io7m.idstore.protocol.admin.IdAResponseType;
import com.io7m.idstore.protocol.admin.IdAResponseUserBanCreate;
import com.io7m.idstore.protocol.admin.IdAResponseUserBanGet;
import com.io7m.idstore.protocol.admin.IdAResponseUserCreate;
import com.io7m.idstore.protocol.admin.IdAResponseUserGet;
import com.io7m.idstore.protocol.admin.IdAResponseUserLoginHistory;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchBegin;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchByEmailBegin;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchByEmailNext;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchByEmailPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchNext;
import com.io7m.idstore.protocol.admin.IdAResponseUserSearchPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseUserUpdate;
import com.io7m.idstore_gui.admin.internal.IdAGPerpetualSubscriber;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventBus;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusCompleted;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusFailed;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusInProgress;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventType;
import com.io7m.repetoir.core.RPServiceType;
import com.io7m.seltzer.api.SStructuredErrorType;
import com.io7m.taskrecorder.core.TRTaskRecorder;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.io7m.idstore.model.IdUserColumn.BY_IDNAME;

/**
 * A client service.
 */

public final class IdAGClientService implements RPServiceType, AutoCloseable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(IdAGClientService.class);

  private static final IdUserColumnOrdering DEFAULT_USER_ORDERING =
    new IdUserColumnOrdering(BY_IDNAME, true);

  private static final IdAdminColumnOrdering DEFAULT_ADMIN_ORDERING =
    new IdAdminColumnOrdering(IdAdminColumn.BY_IDNAME, true);

  private final IdAGEventBus eventBus;
  private final SimpleObjectProperty<HBStateType> status;
  private final IdAClientType client;
  private final ExecutorService executor;
  private final Duration loginTimeout;
  private URI serverLatest;
  private IdAdmin self;
  private final Duration commandTimeout;

  private IdAGClientService(
    final IdAGEventBus inEventBus,
    final IdAClientType inClient)
  {
    this.eventBus =
      Objects.requireNonNull(inEventBus, "eventBus");
    this.client =
      Objects.requireNonNull(inClient, "client");

    this.loginTimeout =
      Duration.ofSeconds(30L);
    this.commandTimeout =
      Duration.ofSeconds(30L);
    this.serverLatest =
      URI.create("urn:unspecified");
    this.status =
      new SimpleObjectProperty<>(new HBStateDisconnected());
    this.executor =
      Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
  }

  /**
   * Create a new client service.
   *
   * @param eventBus The event bus
   * @param clients  The client factory
   * @param locale   The locale
   *
   * @return A new service
   *
   * @throws IdAClientException   On errors
   * @throws InterruptedException On interruption
   */

  public static IdAGClientService create(
    final IdAGEventBus eventBus,
    final IdAClientFactoryType clients,
    final Locale locale)
    throws IdAClientException, InterruptedException
  {
    final var client =
      clients.create(new IdAClientConfiguration(Clock.systemUTC(), locale));
    final var service =
      new IdAGClientService(eventBus, client);

    client.state()
      .subscribe(new IdAGPerpetualSubscriber<>(s -> {
        service.status.set(s);
        transformState(s).ifPresent(eventBus::submit);
      }));
    return service;
  }

  private static Optional<IdAGEventType> transformState(
    final HBStateType e)
  {
    return switch (e) {
      case final HBStateType.HBStateClosed st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Closed.",
            new IdAGEventStatusCompleted()
          )
        );
      }
      case final HBStateType.HBStateClosing st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Closing...",
            new IdAGEventStatusInProgress(OptionalDouble.empty())
          )
        );
      }
      case final HBStateType.HBStateConnected st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Connected.",
            new IdAGEventStatusCompleted()
          )
        );
      }
      case final HBStateType.HBStateConnecting st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Connecting...",
            new IdAGEventStatusInProgress(OptionalDouble.empty())
          )
        );
      }
      case final HBStateType.HBStateConnectionFailed st -> {
        yield transformStateConnectionFailed(st);
      }
      case final HBStateType.HBStateConnectionSucceeded st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Connected.",
            new IdAGEventStatusCompleted()
          )
        );
      }
      case final HBStateDisconnected st -> {
        yield Optional.of(
          new IdAGClientEvent(
            "Disconnected.",
            new IdAGEventStatusCompleted()
          )
        );
      }
    };
  }

  private static Optional<IdAGEventType> transformStateConnectionFailed(
    final HBStateType.HBStateConnectionFailed st)
  {
    final var recorder =
      TRTaskRecorder.create(LOG, "Connecting to server.");

    final var responseOpt =
      st.response();

    String message = "An unrecognized error occurred.";
    Map<String, String> attributes = Map.of();
    Optional<String> remediating = Optional.empty();
    Optional<Throwable> exception = Optional.empty();

    if (responseOpt.isPresent()) {
      final var response = responseOpt.get();
      if (response instanceof final IdAResponseError error) {
        message = error.message();
        attributes = error.attributes();
        remediating = error.remediatingAction();
        exception = error.exception();
      }
    } else {
      final Optional<Exception> exOpt = st.exception();
      if (exOpt.isPresent()) {
        final var exceptionR = exOpt.get();
        if (exceptionR instanceof final SStructuredErrorType<?> exs) {
          attributes = exs.attributes();
          remediating = exs.remediatingAction();
        }
        message =
          Optional.ofNullable(exceptionR.getMessage())
          .orElse(exceptionR.getClass().getSimpleName());
      }
    }

    recorder.setStepFailed(message);
    recorder.setTaskFailed(message);
    final var task = recorder.toTask();

    return Optional.of(
      new IdAGClientEvent(
        "Connection failed.",
        new IdAGEventStatusFailed(
          task,
          IdStandardErrorCodes.IO_ERROR,
          message,
          attributes,
          remediating,
          exception
        )
      )
    );
  }

  private static URI uriOf(
    final boolean https,
    final String host,
    final int port)
  {
    if (https) {
      return URI.create(
        "https://%s:%d/".formatted(host, Integer.valueOf(port))
      );
    } else {
      return URI.create(
        "http://%s:%d/".formatted(host, Integer.valueOf(port))
      );
    }
  }

  /**
   * @return The current client status
   */

  public ReadOnlyObjectProperty<HBStateType> status()
  {
    return this.status;
  }

  @Override
  public String description()
  {
    return String.format(
      "[IdAGClientService 0x%s]",
      Integer.toUnsignedString(this.hashCode(), 16)
    );
  }

  @Override
  public String toString()
  {
    return this.description();
  }

  @Override
  public void close()
    throws Exception
  {
    this.client.close();
    this.executor.close();
  }

  /**
   * Connect to the server and log in.
   *
   * @param host     The hostname
   * @param port     The port
   * @param https    {@code true} if https is required
   * @param username The username
   * @param password The password
   *
   * @return The future representing the login in process
   */

  public CompletableFuture<IdAdmin> login(
    final String host,
    final int port,
    final boolean https,
    final String username,
    final String password)
  {
    this.serverLatest =
      uriOf(https, host, port);

    final var credentials =
      new IdAClientConnectionParameters(
        username,
        password,
        this.serverLatest,
        Map.of(),
        this.loginTimeout,
        this.commandTimeout
      );

    return this.execute(() -> this.client.connectOrThrow(credentials))
      .thenApply(IdAResponseLogin.class::cast)
      .thenCompose(x -> this.send(new IdACommandAdminSelf()))
      .thenApply(IdAResponseAdminSelf.class::cast)
      .thenApply(IdAResponseAdminSelf::admin);
  }

  interface OpType<T>
  {
    T execute()
      throws Exception;
  }

  private <T> CompletableFuture<T> execute(
    final OpType<T> op)
  {
    final var future = new CompletableFuture<T>();
    this.executor.execute(() -> {
      try {
        future.complete(op.execute());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  private <T extends IdAResponseType> CompletableFuture<T> send(
    final IdACommandType<T> command)
  {
    final var future = new CompletableFuture<T>();
    this.executor.execute(() -> {
      try {
        future.complete(
          this.client.sendAndWaitOrThrow(
            command,
            this.commandTimeout
          )
        );
        this.eventBus.submit(
          new IdAGClientEvent(
            "Executed %s".formatted(command.getClass().getSimpleName()),
            new IdAGEventStatusCompleted()
          )
        );
      } catch (final Throwable e) {
        future.completeExceptionally(
          this.handleCommandException(command, e)
        );
      }
    });
    return future;
  }

  private Throwable handleCommandException(
    final IdACommandType<?> command,
    final Throwable exception)
  {
    final var recorder =
      TRTaskRecorder.create(
        LOG,
        "Executing %s".formatted(command.getClass().getSimpleName())
      );

    final String message;
    if (exception.getMessage() != null) {
      message = exception.getMessage();
    } else {
      message = exception.getClass().getSimpleName();
    }
    recorder.setStepFailed(message);
    recorder.setTaskFailed(message);
    final var task = recorder.toTask();

    final IdErrorCode errorCode;
    final Map<String, String> attributes;
    final Optional<String> remediating;
    if (exception instanceof final IdException ex) {
      errorCode = ex.errorCode();
      attributes = ex.attributes();
      remediating = ex.remediatingAction();
    } else {
      errorCode = IdStandardErrorCodes.IO_ERROR;
      attributes = Map.of();
      remediating = Optional.empty();
    }

    this.eventBus.submit(
      new IdAGClientEvent(
        message,
        new IdAGEventStatusFailed(
          task,
          errorCode,
          message,
          attributes,
          remediating,
          Optional.ofNullable(exception)
        )
      )
    );
    return exception;
  }

  /**
   * Disconnect from the server.
   */

  public void disconnect()
  {
    this.execute(() -> {
      this.client.disconnect();
      return null;
    });
  }

  /**
   * Start searching for users.
   *
   * @param search           The search query
   * @param timeCreatedRange The created time range
   * @param timeUpdatedRange The updated time range
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchBegin(
    final IdTimeRange timeCreatedRange,
    final IdTimeRange timeUpdatedRange,
    final Optional<String> search)
  {
    final var command =
      new IdACommandUserSearchBegin(
        new IdUserSearchParameters(
          timeCreatedRange,
          timeUpdatedRange,
          search,
          DEFAULT_USER_ORDERING,
          100
        ));

    return this.send(command)
      .thenApply(IdAResponseUserSearchBegin::page);
  }

  /**
   * Get the next page of users.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchNext()
  {
    return this.send(new IdACommandUserSearchNext())
      .thenApply(IdAResponseUserSearchNext::page);
  }

  /**
   * Get the previous page of users.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchPrevious()
  {
    return this.send(new IdACommandUserSearchPrevious())
      .thenApply(IdAResponseUserSearchPrevious::page);
  }

  /**
   * Retrieve a user.
   *
   * @param id The user ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Optional<IdUser>> userGet(
    final UUID id)
  {
    return this.send(new IdACommandUserGet(id))
      .thenApply(IdAResponseUserGet::user);
  }

  /**
   * Update the given user.
   *
   * @param id       The ID
   * @param idName   The ID name
   * @param realName The real name
   * @param password The password
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdUser> userUpdate(
    final UUID id,
    final Optional<IdName> idName,
    final Optional<IdRealName> realName,
    final Optional<IdPassword> password)
  {
    return this.send(
        new IdACommandUserUpdateCredentials(id, idName, realName, password))
      .thenApply(IdAResponseUserUpdate::user);
  }

  /**
   * Retrieve a user.
   *
   * @param email The user email
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Optional<IdUser>> userGetForEmail(
    final IdEmail email)
  {
    return this.send(new IdACommandUserGetByEmail(email))
      .thenApply(IdAResponseUserGet::user);
  }

  /**
   * Start searching for audit events.
   *
   * @param timeRange The time range
   * @param owner     The owner
   * @param type      The type
   * @param message   The message
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAuditEvent>> auditSearchBegin(
    final IdTimeRange timeRange,
    final Optional<String> owner,
    final Optional<String> type,
    final Optional<String> message)
  {

    return this.send(new IdACommandAuditSearchBegin(
        new IdAuditSearchParameters(
          timeRange,
          owner,
          type,
          100
        )))
      .thenApply(IdAResponseAuditSearchBegin::page);
  }

  /**
   * Get the previous page of events.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAuditEvent>> auditSearchPrevious()
  {
    return this.send(new IdACommandAuditSearchPrevious())
      .thenApply(IdAResponseAuditSearchPrevious::page);
  }

  /**
   * Get the next page of events.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAuditEvent>> auditSearchNext()
  {
    return this.send(new IdACommandAuditSearchNext())
      .thenApply(IdAResponseAuditSearchNext::page);
  }

  /**
   * Start searching for users.
   *
   * @param search           The search query
   * @param timeCreatedRange The created time range
   * @param timeUpdatedRange The updated time range
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchByEmailBegin(
    final IdTimeRange timeCreatedRange,
    final IdTimeRange timeUpdatedRange,
    final String search)
  {
    final var command = new IdACommandUserSearchByEmailBegin(
      new IdUserSearchByEmailParameters(
        timeCreatedRange,
        timeUpdatedRange,
        search,
        DEFAULT_USER_ORDERING,
        100
      )
    );
    return this.send(command)
      .thenApply(IdAResponseUserSearchByEmailBegin::page);
  }

  /**
   * Get the next page of users.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchByEmailNext()
  {
    return this.send(new IdACommandUserSearchByEmailNext())
      .thenApply(IdAResponseUserSearchByEmailNext::page);
  }

  /**
   * Get the previous page of users.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdUserSummary>> userSearchByEmailPrevious()
  {
    return this.send(new IdACommandUserSearchByEmailPrevious())
      .thenApply(IdAResponseUserSearchByEmailPrevious::page);
  }

  /**
   * Delete a user.
   *
   * @param id The user ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Void> userDelete(
    final UUID id)
  {
    return this.send(new IdACommandUserDelete(id))
      .thenRun(() -> {
      });
  }

  /**
   * Fetch the admin's own profile.
   *
   * @return The logged-in admin
   */

  public IdAdmin self()
  {
    if (this.self == null) {
      throw new IllegalStateException("Not logged in.");
    }

    return this.self;
  }

  /**
   * Get the admin's own profile.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdAdmin> adminSelf()
  {
    return this.send(new IdACommandAdminSelf())
      .thenApply(IdAResponseAdminSelf::admin)
      .thenApply(a -> {
        this.self = a;
        return a;
      });
  }

  /**
   * Add an email to the given admin.
   *
   * @param email The email
   * @param id    The ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdAdmin> adminEmailAdd(
    final UUID id,
    final IdEmail email)
  {
    return this.send(new IdACommandAdminEmailAdd(id, email))
      .thenApply(IdAResponseAdminUpdate::admin);
  }

  /**
   * Remove an email from the given admin.
   *
   * @param email The email
   * @param id    The ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdAdmin> adminEmailRemove(
    final UUID id,
    final IdEmail email)
  {
    return this.send(new IdACommandAdminEmailRemove(id, email))
      .thenApply(IdAResponseAdminUpdate::admin);
  }

  /**
   * Add an email to the given user.
   *
   * @param email The email
   * @param id    The ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdUser> userEmailAdd(
    final UUID id,
    final IdEmail email)
  {
    return this.send(new IdACommandUserEmailAdd(id, email))
      .thenApply(IdAResponseUserUpdate::user);
  }

  /**
   * Remove an email from the given user.
   *
   * @param email The email
   * @param id    The ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdUser> userEmailRemove(
    final UUID id,
    final IdEmail email)
  {
    return this.send(new IdACommandUserEmailRemove(id, email))
      .thenApply(IdAResponseUserUpdate::user);
  }

  /**
   * Get the ban for the given user.
   *
   * @param id The ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Optional<IdBan>> userBanGet(
    final UUID id)
  {
    return this.send(new IdACommandUserBanGet(id))
      .thenApply(IdAResponseUserBanGet::ban);
  }

  /**
   * Create a ban for the given user.
   *
   * @param ban The ban
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdBan> userBanCreate(
    final IdBan ban)
  {
    return this.send(new IdACommandUserBanCreate(ban))
      .thenApply(IdAResponseUserBanCreate::ban);
  }

  /**
   * Delete a ban for the given user.
   *
   * @param id The user ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Optional<IdBan>> userBanDelete(
    final UUID id)
  {
    return this.send(new IdACommandUserBanDelete(id))
      .thenApply(x -> Optional.empty());
  }

  /**
   * Get the login history for the given user.
   *
   * @param id The user ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<List<IdLogin>> userLoginHistory(
    final UUID id)
  {
    return this.send(new IdACommandUserLoginHistory(id))
      .thenApply(IdAResponseUserLoginHistory::history);
  }

  /**
   * Create a user.
   *
   * @param create The user creation info
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdUser> userCreate(
    final IdUserCreate create)
  {
    final var command =
      new IdACommandUserCreate(
        create.id(),
        create.idName(),
        create.realName(),
        create.email(),
        create.password()
      );

    return this.send(command)
      .thenApply(IdAResponseUserCreate::user);
  }

  /**
   * Create an admin.
   *
   * @param create The admin creation info
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdAdmin> adminCreate(
    final IdAdminCreate create)
  {
    final var command = new IdACommandAdminCreate(
      create.id(),
      create.idName(),
      create.realName(),
      create.email(),
      create.password(),
      create.permissions()
        .impliedPermissions()
    );

    return this.send(command)
      .thenApply(IdAResponseAdminCreate::admin);
  }

  /**
   * Retrieve an admin.
   *
   * @param id The admin ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Optional<IdAdmin>> adminGet(
    final UUID id)
  {
    return this.send(new IdACommandAdminGet(id))
      .thenApply(IdAResponseAdminGet::admin);
  }

  /**
   * Start searching for admins.
   *
   * @param search           The search query
   * @param timeCreatedRange The created time range
   * @param timeUpdatedRange The updated time range
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchByEmailBegin(
    final IdTimeRange timeCreatedRange,
    final IdTimeRange timeUpdatedRange,
    final String search)
  {
    final var command =
      new IdACommandAdminSearchByEmailBegin(
        new IdAdminSearchByEmailParameters(
          timeCreatedRange,
          timeUpdatedRange,
          search,
          DEFAULT_ADMIN_ORDERING,
          100
        ));

    return this.send(command)
      .thenApply(IdAResponseAdminSearchByEmailBegin::page);
  }

  /**
   * Get the next page of admins.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchByEmailNext()
  {
    return this.send(new IdACommandAdminSearchByEmailNext())
      .thenApply(IdAResponseAdminSearchByEmailNext::page);
  }

  /**
   * Get the previous page of admins.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchByEmailPrevious()
  {
    return this.send(new IdACommandAdminSearchByEmailPrevious())
      .thenApply(IdAResponseAdminSearchByEmailPrevious::page);
  }

  /**
   * Start searching for admins.
   *
   * @param search           The search query
   * @param timeCreatedRange The created time range
   * @param timeUpdatedRange The updated time range
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchBegin(
    final IdTimeRange timeCreatedRange,
    final IdTimeRange timeUpdatedRange,
    final Optional<String> search)
  {
    final var command = new IdACommandAdminSearchBegin(
      new IdAdminSearchParameters(
        timeCreatedRange,
        timeUpdatedRange,
        search,
        DEFAULT_ADMIN_ORDERING,
        100
      ));

    return this.send(command)
      .thenApply(IdAResponseAdminSearchBegin::page);
  }

  /**
   * Get the next page of admins.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchNext()
  {
    return this.send(new IdACommandAdminSearchNext())
      .thenApply(IdAResponseAdminSearchNext::page);
  }

  /**
   * Get the previous page of admins.
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdPage<IdAdminSummary>> adminSearchPrevious()
  {
    return this.send(new IdACommandAdminSearchPrevious())
      .thenApply(IdAResponseAdminSearchPrevious::page);
  }

  /**
   * Update the given admin.
   *
   * @param id       The ID
   * @param idName   The ID name
   * @param realName The real name
   * @param password The password
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<IdAdmin> adminUpdate(
    final UUID id,
    final Optional<IdName> idName,
    final Optional<IdRealName> realName,
    final Optional<IdPassword> password)
  {
    return this.send(
        new IdACommandAdminUpdateCredentials(id, idName, realName, password))
      .thenApply(IdAResponseAdminUpdate::admin);
  }

  /**
   * Delete an admin.
   *
   * @param id The admin ID
   *
   * @return A future representing the operation in progress
   */

  public CompletableFuture<Void> adminDelete(
    final UUID id)
  {
    return this.send(new IdACommandAdminDelete(id))
      .thenRun(() -> {
      });
  }
}
