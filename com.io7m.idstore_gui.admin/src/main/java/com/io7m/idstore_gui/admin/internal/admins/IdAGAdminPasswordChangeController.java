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


package com.io7m.idstore_gui.admin.internal.admins;

import com.io7m.idstore_gui.admin.IdAGConfiguration;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.main.IdAGScreenControllerType;
import com.io7m.idstore.model.IdPassword;
import com.io7m.idstore.model.IdPasswordAlgorithmPBKDF2HmacSHA256;
import com.io7m.idstore.model.IdPasswordException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A password creation controller.
 */

public final class IdAGAdminPasswordChangeController
  implements IdAGScreenControllerType
{
  private final IdAGConfiguration configuration;
  private final IdAGStringsType strings;
  private final Stage stage;

  @FXML private Node passwordFieldBad;
  @FXML private Node passwordConfirmFieldBad;
  @FXML private TextField passwordField;
  @FXML private TextField passwordConfirmField;
  @FXML private Button buttonChange;
  @FXML private Button buttonCancel;

  private Optional<IdPassword> result;

  /**
   * A password creation controller.
   *
   * @param inConfiguration The configuration
   * @param inStrings       The string resources
   * @param inStage         The owning stage
   */

  IdAGAdminPasswordChangeController(
    final IdAGConfiguration inConfiguration,
    final IdAGStringsType inStrings,
    final Stage inStage)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "inConfiguration");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.stage =
      Objects.requireNonNull(inStage, "stage");
    this.result =
      Optional.empty();
  }

  /**
   * @return The result of the password creation, if creation wasn't cancelled
   */

  public Optional<IdPassword> result()
  {
    return this.result;
  }

  @FXML
  private void onCancelSelected()
  {
    this.stage.close();
  }

  @FXML
  private void onChangeSelected()
  {
    try {
      this.result = Optional.of(
        IdPasswordAlgorithmPBKDF2HmacSHA256.create()
          .createHashed(this.passwordField.getText())
      );
      this.stage.close();
    } catch (final IdPasswordException e) {
      throw new IllegalStateException(e);
    }
  }

  @FXML
  private void onFieldTyped()
  {
    this.passwordFieldBad.setVisible(false);
    this.passwordConfirmFieldBad.setVisible(false);

    final var passwordsSame =
      this.passwordField.getText().trim()
        .equals(this.passwordConfirmField.getText().trim());

    final var passwordNonEmpty =
      !this.passwordField.getText()
        .isEmpty();

    final var passwordOk =
      passwordsSame && passwordNonEmpty;

    this.buttonChange.setDisable(!passwordOk);
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.passwordConfirmFieldBad.setVisible(true);
    this.passwordFieldBad.setVisible(true);
    this.buttonChange.setDisable(true);
  }
}
