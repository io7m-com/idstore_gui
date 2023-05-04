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

package com.io7m.idstore_gui.tests;

import com.io7m.hibiscus.api.HBStateType;
import com.io7m.idstore.admin_client.api.IdAClientAsynchronousType;
import com.io7m.idstore.admin_client.api.IdAClientCredentials;
import com.io7m.idstore.admin_client.api.IdAClientFactoryType;
import com.io7m.idstore_gui.admin.internal.IdAGStrings;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminsController;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminsControllers;
import com.io7m.idstore_gui.admin.internal.client.IdAGClientService;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventBus;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferences;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferencesDebuggingEnabled;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferencesServiceType;
import com.io7m.idstore.model.IdAdminSummary;
import com.io7m.idstore.model.IdPage;
import com.io7m.idstore.protocol.admin.IdACommandAdminGet;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchBegin;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchNext;
import com.io7m.idstore.protocol.admin.IdACommandAdminSearchPrevious;
import com.io7m.idstore.protocol.admin.IdACommandType;
import com.io7m.idstore.protocol.admin.IdAResponseAdminGet;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchBegin;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchNext;
import com.io7m.idstore.protocol.admin.IdAResponseAdminSearchPrevious;
import com.io7m.idstore.protocol.admin.IdAResponseError;
import com.io7m.idstore.protocol.admin.IdAResponseType;
import com.io7m.percentpass.extension.PercentPassing;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.xoanon.extension.XoBots;
import com.io7m.xoanon.extension.XoExtension;
import com.io7m.xoanon.extension.XoFXThread;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.io7m.idstore_gui.tests.IdTestAdmins.TEST_ADMIN_0;
import static com.io7m.idstore_gui.tests.IdTestAdmins.TEST_ADMIN_1;
import static javafx.scene.input.KeyCode.A;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(XoExtension.class)
@Timeout(value = 1L, unit = TimeUnit.MINUTES)
public final class IdAGAdminsControllerTest
{
  private volatile IdAGAdminsController controller;
  private IdAGStringsType strings;
  private IdAGTemporaryConfiguration configuration;
  private RPServiceDirectory services;
  private IdAGPreferencesServiceType preferences;
  private IdAGEventBus events;
  private IdAClientFactoryType clients;
  private IdAClientAsynchronousType client;
  private IdAGClientService clientService;
  private SubmissionPublisher<
    HBStateType<
      IdACommandType<?>,
      IdAResponseType,
      IdAResponseError,
      IdAClientCredentials>
    > clientState;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.clientState =
      new SubmissionPublisher<>();
    this.configuration =
      new IdAGTemporaryConfiguration();
    this.events =
      new IdAGEventBus();

    this.preferences =
      Mockito.mock(IdAGPreferencesServiceType.class);

    Mockito.when(this.preferences.preferences())
      .thenReturn(new IdAGPreferences(
        UUID.randomUUID(),
        IdAGPreferencesDebuggingEnabled.DEBUGGING_ENABLED,
        List.of(),
        List.of()
      ));

    this.clients =
      Mockito.mock(IdAClientFactoryType.class);
    this.client =
      Mockito.mock(IdAClientAsynchronousType.class);

    Mockito.when(this.clients.openAsynchronousClient(any()))
      .thenReturn(this.client);
    Mockito.when(this.client.state())
      .thenReturn(this.clientState);

    this.strings =
      new IdAGStrings(Locale.ROOT);
    this.clientService =
      IdAGClientService.create(
        this.events,
        this.clients,
        Locale.ROOT
      );

    this.services = new RPServiceDirectory();
    this.services.register(IdAGPreferencesServiceType.class, this.preferences);
    this.services.register(IdAGStringsType.class, this.strings);
    this.services.register(IdAGClientService.class, this.clientService);
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    this.configuration.close();
  }

  /**
   * Test nothing.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testNothing(
    final Stage stage)
    throws Exception
  {
    /*
     * Arrange.
     */

    final var bot =
      XoBots.createForStage(stage);

    XoFXThread.run(() -> {
      this.controller =
        new IdAGAdminsControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createViewControllerForStage(null, stage);
      return null;
    }).get();

    /*
     * Act.
     */

    /*
     * Assert.
     */
  }

  /**
   * Test nothing.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testSearch(
    final Stage stage)
    throws Exception
  {
    /*
     * Arrange.
     */

    final var bot =
      XoBots.createForStage(stage);

    XoFXThread.run(() -> {
      this.controller =
        new IdAGAdminsControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createViewControllerForStage(null, stage);
      return null;
    }).get();

    final var uuid0 =
      UUID.fromString("ff5b36dc-31a0-41ab-b823-934bafe18881");
    final var uuid1 =
      UUID.fromString("5f5eb211-cd2b-41df-976d-d0475c3eb959");

    final var page0 = new IdPage<>(
      List.of(
        new IdAdminSummary(
          uuid0,
          TEST_ADMIN_0.idName(),
          TEST_ADMIN_0.realName(),
          OffsetDateTime.now(),
          OffsetDateTime.now()
        )
      ),
      1,
      2,
      0L
    );

    final var page1 = new IdPage<>(
      List.of(
        new IdAdminSummary(
          uuid1,
          TEST_ADMIN_1.idName(),
          TEST_ADMIN_1.realName(),
          OffsetDateTime.now(),
          OffsetDateTime.now()
        )
      ),
      2,
      2,
      0L
    );

    Mockito.when(this.client.executeAsyncOrElseThrow(
        any(IdACommandAdminGet.class), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseAdminGet(
            UUID.randomUUID(),
            Optional.of(TEST_ADMIN_0))
        )
      );

    Mockito.when(this.client.executeAsyncOrElseThrow(
      any(IdACommandAdminSearchBegin.class), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseAdminSearchBegin(UUID.randomUUID(), page0)
        )
      );

    Mockito.when(this.client.executeAsyncOrElseThrow(
        any(IdACommandAdminSearchNext.class), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseAdminSearchNext(UUID.randomUUID(), page1)
        )
      );

    Mockito.when(this.client.executeAsyncOrElseThrow(
        any(IdACommandAdminSearchPrevious.class), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseAdminSearchPrevious(UUID.randomUUID(), page0)
        )
      );

    final var searchField =
      (TextField) bot.findWithId("adminSearchField");
    final var searchButton =
      (Button) bot.findWithId("adminSearchButton");
    final var adminTable =
      (TableView<?>) bot.findWithId("adminTable");
    final var nextButton =
      (Button) bot.findWithId("adminPageNext");
    final var prevButton =
      (Button) bot.findWithId("adminPagePrev");

    /*
     * Act.
     */

    /*
     * Walk through several pages of results.
     */

    bot.click(searchField);
    bot.type(searchButton, A);
    bot.click(searchButton);
    Thread.sleep(100L);

    {
      final var adminNode = bot.findWithText(adminTable, uuid0.toString());
      bot.click(adminNode);
    }

    Thread.sleep(100L);
    bot.click(nextButton);
    Thread.sleep(100L);

    {
      final var adminNode = bot.findWithText(adminTable, uuid1.toString());
      bot.click(adminNode);
    }

    Thread.sleep(100L);
    bot.click(prevButton);
    Thread.sleep(100L);

    {
      final var adminNode = bot.findWithText(adminTable, uuid0.toString());
      bot.click(adminNode);
    }

    /*
     * Assert.
     */

  }
}
