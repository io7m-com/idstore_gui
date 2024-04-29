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
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminDeleteConfirmController;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminDeleteConfirmControllers;
import com.io7m.idstore.model.IdAdmin;
import com.io7m.idstore.model.IdAdminPermissionSet;
import com.io7m.idstore.model.IdEmail;
import com.io7m.idstore.model.IdName;
import com.io7m.idstore.model.IdNonEmptyList;
import com.io7m.idstore.model.IdPasswordAlgorithmPBKDF2HmacSHA256;
import com.io7m.idstore.model.IdRealName;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
public final class IdAGAdminDeleteConfirmControllerTest
{
  private volatile IdAGAdminDeleteConfirmController controller;
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
    final var admin =
      new IdAdmin(
        UUID.randomUUID(),
        new IdName("admin"),
        new IdRealName("Admin"),
        new IdNonEmptyList<>(new IdEmail("someone@example.com"), List.of()),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        IdPasswordAlgorithmPBKDF2HmacSHA256.create().createHashed("12345678"),
        IdAdminPermissionSet.all()
      );

    /*
     * Arrange.
     */

    final var bot =
      XoBots.createForStage(stage);

    XoFXThread.run(() -> {
      this.controller =
        new IdAGAdminDeleteConfirmControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(admin, stage);
      return null;
    }).get();

    final var cancelButton =
      (Button) bot.findWithId("cancel");
    final var deleteButton =
      (Button) bot.findWithId("delete");
    final var nameField =
      (TextField) bot.findWithId("adminNameField");

    /*
     * Act.
     */

    bot.click(nameField);
    bot.type(nameField, A, D, M, I, N);
    bot.click(deleteButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertTrue(this.controller.isDeleteRequested());
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
    final var admin =
      new IdAdmin(
        UUID.randomUUID(),
        new IdName("admin"),
        new IdRealName("Admin"),
        new IdNonEmptyList<>(new IdEmail("someone@example.com"), List.of()),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        IdPasswordAlgorithmPBKDF2HmacSHA256.create().createHashed("12345678"),
        IdAdminPermissionSet.all()
      );

    /*
     * Arrange.
     */

    final var bot =
      XoBots.createForStage(stage);

    XoFXThread.run(() -> {
      this.controller =
        new IdAGAdminDeleteConfirmControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(admin, stage);
      return null;
    }).get();

    final var cancelButton =
      (Button) bot.findWithId("cancel");
    final var deleteButton =
      (Button) bot.findWithId("delete");

    /*
     * Act.
     */

    bot.click(cancelButton);
    bot.waitForStageToClose(1_000L);

    /*
     * Assert.
     */

    assertFalse(this.controller.isDeleteRequested());
  }
}
