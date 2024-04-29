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
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminCreateController;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminCreateControllers;
import com.io7m.idstore.model.IdAdmin;
import com.io7m.idstore.model.IdAdminCreate;
import com.io7m.idstore.model.IdAdminPermission;
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
import javafx.scene.control.ListView;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.E;
import static javafx.scene.input.KeyCode.M;
import static javafx.scene.input.KeyCode.N;
import static javafx.scene.input.KeyCode.O;
import static javafx.scene.input.KeyCode.PERIOD;
import static javafx.scene.input.KeyCode.QUOTE;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.X;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(XoExtension.class)
@Timeout(value = 1L, unit = TimeUnit.MINUTES)
public final class IdAGAdminCreateControllerTest
{
  private volatile IdAGAdminCreateController controller;
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
   * Test that creating an admin works.
   *
   * @param stage The stage
   *
   * @throws Exception On errors
   */

  @PercentPassing(executionCount = 3, passPercent = 33.0)
  public void testCreateWithPermissions(
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
        new IdAGAdminCreateControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(admin, stage);
      return null;
    }).get();

    final var idNameField =
      (TextField) bot.findWithId("idNameField");
    final var realNameField =
      (TextField) bot.findWithId("realNameField");
    final var emailField =
      (TextField) bot.findWithId("emailField");
    final var createButton =
      (Button) bot.findWithId("createButton");
    final var permissionAssign =
      (Button) bot.findWithId("permissionAssignButton");
    final var permissionUnassign =
      (Button) bot.findWithId("permissionUnassignButton");
    final var permissionsAvailable =
      (ListView<?>) bot.findWithId("permissionsAvailableView");
    final var permissionsAssigned =
      (ListView<?>) bot.findWithId("permissionsAssignedView");

    /*
     * Act.
     */

    bot.click(idNameField);
    bot.type(idNameField, S, O, M, E, O, N, E);

    bot.click(realNameField);
    bot.type(realNameField, S, O, M, E, O, N, E);

    bot.click(emailField);
    bot.type(emailField, S, O, M, E, O, N, E);
    bot.typeWithShift(emailField, QUOTE);
    bot.type(emailField, E, X, PERIOD, C, O, M);

    final var permissionAuditRead =
      bot.findWithText("AUDIT_READ");

    bot.click(permissionsAvailable);
    bot.click(permissionAuditRead);
    bot.click(permissionAssign);

    final var permissionUserDelete =
      bot.findWithText("USER_DELETE");

    bot.click(permissionUserDelete);
    bot.click(permissionAssign);

    final var assigned =
      bot.findWithText(permissionsAssigned, "USER_DELETE");

    bot.click(assigned);
    bot.click(permissionUnassign);

    bot.click(createButton);
    bot.waitForStageToClose(1000L);

    /*
     * Assert.
     */

    final var result =
      this.controller.result()
        .orElseThrow();

    final var expected =
      new IdAdminCreate(
        Optional.empty(),
        new IdName("someone"),
        new IdRealName("someone"),
        new IdEmail("someone@ex.com"),
        IdPasswordAlgorithmPBKDF2HmacSHA256.create()
          .createHashed(""),
        IdAdminPermissionSet.of(IdAdminPermission.AUDIT_READ)
      );

    final var received =
      this.controller.result()
        .orElseThrow();

    assertEquals(expected.id(), received.id());
    assertEquals(expected.idName(), received.idName());
    assertEquals(expected.realName(), received.realName());
    assertEquals(expected.permissions(), received.permissions());
  }

  /**
   * Test that cancelling creation works.
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
        new IdAGAdminCreateControllers(
          this.services,
          this.configuration.configuration(),
          this.strings
        ).createDialogForStage(admin, stage);
      return null;
    }).get();

    final var cancelButton =
      (Button) bot.findWithId("cancelButton");

    /*
     * Act.
     */

    bot.click(cancelButton);
    bot.waitForStageToClose(1000L);

    /*
     * Assert.
     */

    assertEquals(Optional.empty(), this.controller.result());
  }
}
