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


package com.io7m.idstore_gui.admin.internal.view_controllers;

import com.io7m.idstore_gui.admin.IdAGConfiguration;
import com.io7m.idstore_gui.admin.internal.IdAGCSS;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.main.IdAGControllerAndStage;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * A convenient abstract view controller factory.
 *
 * @param <A> The type of view controller arguments
 * @param <C> The type of underlying controllers
 */

public abstract class IdAGViewControllerFactoryAbstract<A, C>
  implements IdAGViewControllerFactoryType<A, C>
{
  private final RPServiceDirectoryType services;
  private final IdAGConfiguration configuration;
  private final IdAGStringsType strings;
  private final Class<C> controllerClass;
  private final String fxmlResource;

  protected IdAGViewControllerFactoryAbstract(
    final Class<C> inControllerClass,
    final String inFXMLResource,
    final RPServiceDirectoryType inMainServices,
    final IdAGConfiguration inConfiguration,
    final IdAGStringsType inStrings)
  {
    this.controllerClass =
      Objects.requireNonNull(inControllerClass, "inControllerClass");
    this.fxmlResource =
      Objects.requireNonNull(inFXMLResource, "inFXMLResource");
    this.services =
      Objects.requireNonNull(inMainServices, "inMainServices");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  protected final RPServiceDirectoryType services()
  {
    return this.services;
  }

  protected final IdAGConfiguration configuration()
  {
    return this.configuration;
  }

  @Override
  public final IdAGControllerAndStage<C> createViewController(
    final A arguments)
    throws IOException
  {
    final var stage =
      new Stage();
    final var controller =
      this.createViewControllerForStage(arguments, stage);
    return new IdAGControllerAndStage<>(controller, stage);
  }

  @Override
  public final C createViewControllerForStage(
    final A arguments,
    final Stage stage)
    throws IOException
  {
    Objects.requireNonNull(stage, "stage");

    final var xml =
      this.fxmlResource();
    final var resources =
      this.strings.resources();
    final var loader =
      new FXMLLoader(xml, resources);

    loader.setControllerFactory(
      clazz -> this.createController(arguments, stage)
    );

    final Parent pane = loader.load();
    IdAGCSS.setCSS(this.configuration, pane);

    final C controller = loader.getController();
    stage.setScene(new Scene(pane));
    stage.setTitle(this.createStageTitle(arguments));
    return controller;
  }

  @Override
  public final URL fxmlResource()
  {
    return this.controllerClass.getResource(this.fxmlResource);
  }

  protected final IdAGStringsType strings()
  {
    return this.strings;
  }

  protected abstract String createStageTitle(
    A arguments
  );

  protected abstract C createController(
    A arguments,
    Stage stage
  );
}
