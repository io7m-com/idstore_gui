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
import com.io7m.idstore_gui.admin.internal.client.IdAGClientService;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventBus;
import com.io7m.idstore_gui.admin.internal.login.IdAGLoginController;
import com.io7m.idstore_gui.admin.internal.login.IdAGLoginControllers;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferences;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferencesDebuggingEnabled;
import com.io7m.idstore_gui.admin.internal.preferences.IdAGPreferencesServiceType;
import com.io7m.idstore.protocol.admin.IdACommandType;
import com.io7m.idstore.protocol.admin.IdAResponseError;
import com.io7m.idstore.protocol.admin.IdAResponseLogin;
import com.io7m.idstore.protocol.admin.IdAResponseType;
import com.io7m.percentpass.extension.PercentPassing;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.xoanon.extension.XoBots;
import com.io7m.xoanon.extension.XoExtension;
import com.io7m.xoanon.extension.XoFXThread;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.io7m.idstore_gui.tests.IdTestAdmins.TEST_ADMIN_0;
import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.H;
import static javafx.scene.input.KeyCode.L;
import static javafx.scene.input.KeyCode.O;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.T;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(XoExtension.class)
@Timeout(value = 1L, unit = TimeUnit.MINUTES)
public final class IdAGLoginControllerTest
{
  private volatile IdAGLoginController controller;
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
   * Test that connecting works.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testConnect(
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
        new IdAGLoginControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var hostField =
      (TextField) bot.findWithId("hostField");
    final var portField =
      (TextField) bot.findWithId("portField");
    final var userField =
      (TextField) bot.findWithId("userField");
    final var passField =
      (TextField) bot.findWithId("passField");
    final var cancelButton =
      (Button) bot.findWithId("cancelButton");
    final var connectButton =
      (Button) bot.findWithId("connectButton");

    Mockito.when(this.client.loginAsyncOrElseThrow(any(), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseLogin(UUID.randomUUID(), TEST_ADMIN_0)
        )
      );

    /*
     * Act.
     */

    bot.click(hostField);
    bot.type(hostField, L, O, C, A, L, H, O, S, T);
    bot.click(userField);
    bot.type(userField, A);
    bot.click(passField);
    bot.type(passField, A);
    bot.click(connectButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    Mockito.verify(this.client, new Times(1))
      .loginAsyncOrElseThrow(
        eq(new IdAClientCredentials(
          "a",
          "a",
          URI.create("http://localhost:51000/"),
          Map.of()
        )),
        any()
      );

    Mockito.verify(this.client, new Times(1))
      .state();
  }

  /**
   * Test that connecting works with https.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testConnectHTTPS(
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
        new IdAGLoginControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var hostField =
      (TextField) bot.findWithId("hostField");
    final var portField =
      (TextField) bot.findWithId("portField");
    final var userField =
      (TextField) bot.findWithId("userField");
    final var passField =
      (TextField) bot.findWithId("passField");
    final var httpsBox =
      (CheckBox) bot.findWithId("httpsBox");
    final var cancelButton =
      (Button) bot.findWithId("cancelButton");
    final var connectButton =
      (Button) bot.findWithId("connectButton");

    Mockito.when(this.client.loginAsyncOrElseThrow(any(), any()))
      .thenReturn(
        CompletableFuture.completedFuture(
          new IdAResponseLogin(UUID.randomUUID(), TEST_ADMIN_0)
        )
      );

    /*
     * Act.
     */

    bot.click(hostField);
    bot.type(hostField, L, O, C, A, L, H, O, S, T);
    bot.click(httpsBox);
    bot.click(userField);
    bot.type(userField, A);
    bot.click(passField);
    bot.type(passField, A);
    bot.click(connectButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    Mockito.verify(this.client, new Times(1))
      .loginAsyncOrElseThrow(
        eq(new IdAClientCredentials(
          "a",
          "a",
          URI.create("https://localhost:51000/"),
          Map.of()
        )),
        any()
      );

    Mockito.verify(this.client, new Times(1))
      .state();
  }

  /**
   * Test that cancelling works.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testCancel(
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
        new IdAGLoginControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var hostField =
      (TextField) bot.findWithId("hostField");
    final var portField =
      (TextField) bot.findWithId("portField");
    final var userField =
      (TextField) bot.findWithId("userField");
    final var passField =
      (TextField) bot.findWithId("passField");
    final var cancelButton =
      (Button) bot.findWithId("cancelButton");
    final var connectButton =
      (Button) bot.findWithId("connectButton");

    /*
     * Act.
     */

    bot.click(cancelButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    Mockito.verify(this.client, new Times(1))
      .state();

    Mockito.verifyNoMoreInteractions(this.client);
  }
}
