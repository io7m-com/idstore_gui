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
import com.io7m.idstore_gui.admin.internal.users.IdAGUserEmailAddController;
import com.io7m.idstore_gui.admin.internal.users.IdAGUserEmailAddControllers;
import com.io7m.idstore.model.IdEmail;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.E;
import static javafx.scene.input.KeyCode.M;
import static javafx.scene.input.KeyCode.O;
import static javafx.scene.input.KeyCode.PERIOD;
import static javafx.scene.input.KeyCode.QUOTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(XoExtension.class)
@Timeout(value = 10L, unit = TimeUnit.SECONDS)
public final class IdAGUserEmailAddControllerTest
{
  private volatile IdAGUserEmailAddController controller;
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
        new IdAGUserEmailAddControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    final var addButton =
      (Button) bot.findWithId("buttonCreate");
    final var nameField =
      (TextField) bot.findWithId("emailField");

    /*
     * Act.
     */

    bot.click(nameField);
    bot.type(nameField, A);
    bot.typeWithShift(nameField, QUOTE);
    bot.type(nameField, E, PERIOD, C, O, M);
    bot.click(addButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertEquals(
      new IdEmail("a@e.com"),
      this.controller.result().orElseThrow()
    );
  }

  /**
   * Test that cancelling works.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 10, passPercent = 30.0)
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
        new IdAGUserEmailAddControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(null, stage);
      return null;
    }).get();

    /*
     * Act.
     */

    final var cancelButton =
      (Button) bot.findWithId("buttonCancel");

    bot.click(cancelButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertEquals(
      Optional.empty(),
      this.controller.result()
    );
  }
}
