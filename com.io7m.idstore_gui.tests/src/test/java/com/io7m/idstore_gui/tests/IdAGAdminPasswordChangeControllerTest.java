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

import com.io7m.idstore_gui.admin.internal.IdAGStrings;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminPasswordChangeController;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminPasswordChangeControllers;
import com.io7m.percentpass.extension.PercentPassing;
import com.io7m.repetoir.core.RPServiceDirectory;
import com.io7m.xoanon.extension.XoBots;
import com.io7m.xoanon.extension.XoExtension;
import com.io7m.xoanon.extension.XoFXThread;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.D;
import static javafx.scene.input.KeyCode.I;
import static javafx.scene.input.KeyCode.M;
import static javafx.scene.input.KeyCode.N;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(XoExtension.class)
@Timeout(value = 1L, unit = TimeUnit.MINUTES)
public final class IdAGAdminPasswordChangeControllerTest
{
  private volatile IdAGAdminPasswordChangeController controller;
  private IdAGStringsType strings;
  private IdAGTemporaryConfiguration configuration;
  private RPServiceDirectory services;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.services =
      new RPServiceDirectory();
    this.strings =
      new IdAGStrings(Locale.ROOT);
    this.configuration =
      new IdAGTemporaryConfiguration();
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    this.configuration.close();
  }

  /**
   * Test that confirming works.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testConfirm(
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
        new IdAGAdminPasswordChangeControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var buttonCancel =
      (Button) bot.findWithId("buttonCancel");
    final var buttonChange =
      (Button) bot.findWithId("buttonChange");
    final var passwordField =
      (TextField) bot.findWithId("passwordField");
    final var passwordConfirmField =
      (TextField) bot.findWithId("passwordConfirmField");

    /*
     * Act.
     */

    bot.click(passwordField);
    bot.type(passwordField, A, D, M, I, N);
    bot.click(passwordConfirmField);
    bot.type(passwordConfirmField, A, D, M, I, N);
    bot.click(buttonChange);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertTrue(
      this.controller.result()
        .orElseThrow()
        .check("admin")
    );
  }

  /**
   * Test that confirming is not possible if passwords do not match.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testConfirmImpossible(
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
        new IdAGAdminPasswordChangeControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var buttonCancel =
      (Button) bot.findWithId("buttonCancel");
    final var buttonChange =
      (Button) bot.findWithId("buttonChange");
    final var passwordField =
      (TextField) bot.findWithId("passwordField");
    final var passwordConfirmField =
      (TextField) bot.findWithId("passwordConfirmField");

    /*
     * Act.
     */

    bot.click(passwordField);
    bot.type(passwordField, A, D, M, I, N);
    bot.click(passwordConfirmField);
    bot.type(passwordConfirmField, N, I, M, A, D);
    bot.click(buttonChange);

    XoFXThread.runAndWait(1L, TimeUnit.SECONDS, () -> {
      assertTrue(buttonChange.isDisable());
      return null;
    });

    bot.click(buttonCancel);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertFalse(this.controller.result().isPresent());
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
        new IdAGAdminPasswordChangeControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var cancelButton =
      (Button) bot.findWithId("buttonCancel");

    /*
     * Act.
     */

    bot.click(cancelButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertFalse(this.controller.result().isPresent());
  }
}
