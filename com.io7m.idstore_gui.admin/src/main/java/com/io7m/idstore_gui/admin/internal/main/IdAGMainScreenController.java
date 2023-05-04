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

package com.io7m.idstore_gui.admin.internal.main;

import com.io7m.hibiscus.api.HBStateType;
import com.io7m.hibiscus.api.HBStateType.HBStateClosed;
import com.io7m.hibiscus.api.HBStateType.HBStateDisconnected;
import com.io7m.hibiscus.api.HBStateType.HBStateExecutingLogin;
import com.io7m.hibiscus.api.HBStateType.HBStateExecutingLoginFailed;
import com.io7m.idstore_gui.admin.IdAGConfiguration;
import com.io7m.idstore_gui.admin.internal.IdAGApplication;
import com.io7m.idstore_gui.admin.internal.IdAGCSS;
import com.io7m.idstore_gui.admin.internal.IdAGPerpetualSubscriber;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.about.IdAGAboutControllers;
import com.io7m.idstore_gui.admin.internal.client.IdAGClientService;
import com.io7m.idstore_gui.admin.internal.errors.IdAGErrorDialogs;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventBus;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusCancelled;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusCompleted;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusFailed;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventStatusInProgress;
import com.io7m.idstore_gui.admin.internal.events.IdAGEventType;
import com.io7m.idstore_gui.admin.internal.login.IdAGLoginControllers;
import com.io7m.idstore_gui.admin.internal.services.IdAGBootEvent;
import com.io7m.idstore_gui.admin.internal.services.IdAGBootServices;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import com.io7m.taskrecorder.core.TRTask;
import com.io7m.taskrecorder.core.TRTaskFailed;
import com.io7m.taskrecorder.core.TRTaskRecorder;
import com.io7m.taskrecorder.core.TRTaskSucceeded;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 * The main screen controller.
 */

public final class IdAGMainScreenController implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(IdAGMainScreenController.class);

  private final IdAGConfiguration configuration;
  private final IdAGStringsType strings;

  @FXML private ImageView mainStatusIcon;
  @FXML private ProgressBar mainProgress;
  @FXML private TextField mainStatusText;
  @FXML private MenuBar mainMenuBar;
  @FXML private MenuItem mainConnectMenuItem;
  @FXML private AnchorPane mainContent;

  private volatile RPServiceDirectoryType services;
  private volatile TRTask<RPServiceDirectoryType> task;
  private volatile IdAGClientService client;
  private volatile IdAGEventBus events;
  private Image iconError;
  private Image iconApp;
  private IdAGErrorDialogs errorDialogs;

  /**
   * The main screen controller.
   *
   * @param inConfiguration The application configuration
   * @param inStrings       The application strings
   */

  IdAGMainScreenController(
    final IdAGConfiguration inConfiguration,
    final IdAGStringsType inStrings)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.contentHide();

    this.iconApp =
      loadIcon("idstore.png");
    this.iconError =
      loadIcon("error16.png");

    this.mainMenuBar.setDisable(true);
    this.mainStatusIcon.setImage(this.iconApp);
    this.mainStatusText.setText("");
    this.mainProgress.setProgress(INDETERMINATE_PROGRESS);

    final var bootFuture =
      IdAGBootServices.create(
        this.configuration,
        this.strings,
        this::onBootEvent
      );

    bootFuture.whenComplete((bootTask, exception) -> {
      Platform.runLater(() -> {
        this.mainMenuBar.setDisable(false);
        this.mainProgress.setVisible(false);
      });

      this.task = bootTask;
      if (exception != null) {
        LOG.debug("services failed: ", exception);
        try (var recorder =
               TRTaskRecorder.<RPServiceDirectoryType>create(
                 LOG, "Booting application...")) {
          recorder.setTaskFailed(
            exception.getMessage(),
            Optional.of(exception));
          this.task = recorder.toTask();
        }
        this.onBootFailed();
        return;
      }

      final var resolution = bootTask.resolution();
      if (resolution instanceof TRTaskFailed<?> failed) {
        LOG.debug("services failed: {}", failed);
        this.onBootFailed();
        return;
      }

      if (resolution instanceof TRTaskSucceeded<RPServiceDirectoryType> succeeded) {
        this.services = succeeded.result();
      }

      Platform.runLater(() -> {
        try {
          this.onBootCompleted();
        } catch (final Exception e) {
          LOG.error("error: ", e);
          Platform.exit();
        }
      });
    });
  }

  private static Image loadIcon(final String x)
  {
    return new Image(IdAGMainScreenController.class.getResource(
        "/com/io7m/idstore_gui/admin/internal/" + x)
                       .toString());
  }

  private void onBootCompleted()
    throws IOException
  {
    this.client =
      this.services.requireService(IdAGClientService.class);
    this.events =
      this.services.requireService(IdAGEventBus.class);
    this.errorDialogs =
      this.services.requireService(IdAGErrorDialogs.class);

    this.events.subscribe(new IdAGPerpetualSubscriber<>(this::onEvent));

    final var tabs = this.createTabs();
    this.mainContent.getChildren().add(tabs);
    AnchorPane.setBottomAnchor(tabs, Double.valueOf(0.0));
    AnchorPane.setTopAnchor(tabs, Double.valueOf(0.0));
    AnchorPane.setLeftAnchor(tabs, Double.valueOf(0.0));
    AnchorPane.setRightAnchor(tabs, Double.valueOf(0.0));

    this.client.status()
      .addListener((obs, statusOld, statusNew) -> {
        this.configureMainContentViewForClientStatus(statusNew);
      });
  }

  private void configureMainContentViewForClientStatus(
    final HBStateType<?, ?, ?, ?> status)
  {
    if (status instanceof HBStateExecutingLogin) {
      Platform.runLater(() -> {
        this.contentHide();
        this.mainConnectMenuItem.setDisable(true);
        this.mainConnectMenuItem.setText(
          this.strings.format("menu.disconnect"));
      });
      return;
    }

    if (status instanceof HBStateClosed
        || status instanceof HBStateExecutingLoginFailed
        || status instanceof HBStateDisconnected) {
      Platform.runLater(() -> {
        this.contentHide();
        this.mainConnectMenuItem.setDisable(false);
        this.mainConnectMenuItem.setText(
          this.strings.format("menu.connect"));
      });
      return;
    }

    Platform.runLater(() -> {
      this.contentShow();
      this.mainConnectMenuItem.setDisable(false);
      this.mainConnectMenuItem.setText(
        this.strings.format("menu.disconnect"));
    });
  }

  private Node createTabs()
    throws IOException
  {
    final var mainXML =
      IdAGApplication.class.getResource(
        "/com/io7m/idstore_gui/admin/internal/mainContent.fxml");
    Objects.requireNonNull(mainXML, "mainXML");

    final var mainLoader =
      new FXMLLoader(mainXML, this.strings.resources());
    final var factory =
      new IdAGMainControllerFactory(this.services, this.configuration);

    mainLoader.setControllerFactory(factory);

    final Pane pane = mainLoader.load();
    IdAGCSS.setCSS(this.configuration, pane);
    return pane;
  }

  private void contentShow()
  {
    this.mainContent.setVisible(true);
    this.mainContent.setDisable(false);
  }

  private void contentHide()
  {
    this.mainContent.setVisible(false);
    this.mainContent.setDisable(true);
  }

  private void onEvent(
    final IdAGEventType event)
  {
    Platform.runLater(() -> {
      this.configureStatusBarForEvent(event);
      this.openErrorDialogForEventIfNecessary(event);
    });
  }

  private void openErrorDialogForEventIfNecessary(
    final IdAGEventType event)
  {
    final var status = event.status();
    if (status instanceof IdAGEventStatusFailed failed) {
      this.errorDialogs.open(failed.task(), failed);
    }
  }

  private void configureStatusBarForEvent(
    final IdAGEventType event)
  {
    final var status = event.status();
    this.mainStatusText.setText(firstLineOf(event));
    this.mainStatusIcon.setImage(this.iconApp);

    if (status instanceof IdAGEventStatusInProgress inProgress) {
      this.mainProgress.setVisible(true);
      final var progressOpt = inProgress.progress();
      if (progressOpt.isPresent()) {
        this.mainProgress.setProgress(progressOpt.getAsDouble());
      } else {
        this.mainProgress.setProgress(INDETERMINATE_PROGRESS);
      }
    } else if (status instanceof IdAGEventStatusCompleted) {
      this.mainProgress.setVisible(false);
    } else if (status instanceof IdAGEventStatusCancelled) {
      this.mainProgress.setVisible(false);
    } else if (status instanceof IdAGEventStatusFailed) {
      this.mainStatusIcon.setImage(this.iconError);
      this.mainProgress.setVisible(false);
    }
  }

  private static String firstLineOf(
    final IdAGEventType event)
  {
    return event.message().split("\n")[0];
  }

  private void onBootFailed()
  {
    Platform.runLater(() -> {

    });
  }

  private void onBootEvent(
    final IdAGBootEvent event)
  {
    Platform.runLater(() -> {
      this.mainStatusText.setText(event.message());
      this.mainProgress.setVisible(true);
      this.mainProgress.setProgress(event.progress());
    });
  }

  @FXML
  private void onConnectSelected()
    throws IOException
  {
    final var state =
      this.client.status()
        .get();

    if (state instanceof HBStateExecutingLoginFailed
        || state instanceof HBStateDisconnected) {
      new IdAGLoginControllers(this.services, this.configuration, this.strings)
        .openDialogAndWait(null);
      return;
    }

    this.client.disconnect();
  }

  @FXML
  private void onExitSelected()
  {
    Platform.exit();
  }

  @FXML
  private void onAboutSelected()
    throws IOException
  {
    new IdAGAboutControllers(this.services, this.configuration, this.strings)
      .openDialogAndWait(null);
  }
}
