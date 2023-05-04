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

import com.io7m.idstore_gui.admin.IdAGConfiguration;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminsController;
import com.io7m.idstore_gui.admin.internal.admins.IdAGAdminsControllers;
import com.io7m.idstore_gui.admin.internal.audit.IdAGAuditController;
import com.io7m.idstore_gui.admin.internal.audit.IdAGAuditControllers;
import com.io7m.idstore_gui.admin.internal.profile.IdAGProfileController;
import com.io7m.idstore_gui.admin.internal.profile.IdAGProfileControllers;
import com.io7m.idstore_gui.admin.internal.users.IdAGUsersController;
import com.io7m.idstore_gui.admin.internal.users.IdAGUsersControllers;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The main factory of controllers for the UI.
 */

public final class IdAGMainControllerFactory
  implements Callback<Class<?>, Object>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(IdAGMainControllerFactory.class);

  private final RPServiceDirectoryType services;
  private final IdAGConfiguration configuration;

  /**
   * The main factory of controllers for the UI.
   *
   * @param inServices      The service directory
   * @param inConfiguration The UI configuration
   */

  public IdAGMainControllerFactory(
    final RPServiceDirectoryType inServices,
    final IdAGConfiguration inConfiguration)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public Object call(
    final Class<?> param)
  {
    try {
      return param.cast(this.create(param));
    } catch (final Exception e) {
      LOG.error("factory: ", e);
      throw new IllegalStateException(e);
    }
  }

  private Object create(
    final Class<?> param)
    throws Exception
  {
    if (Objects.equals(
      param.getCanonicalName(),
      IdAGUsersController.class.getCanonicalName())) {
      return new IdAGUsersControllers(
        this.services,
        this.configuration,
        this.services.requireService(IdAGStringsType.class))
        .createViewController(null)
        .controller();
    }

    if (Objects.equals(
      param.getCanonicalName(),
      IdAGAdminsController.class.getCanonicalName())) {
      return new IdAGAdminsControllers(
        this.services,
        this.configuration,
        this.services.requireService(IdAGStringsType.class))
        .createViewController(null)
        .controller();
    }

    if (Objects.equals(
      param.getCanonicalName(),
      IdAGAuditController.class.getCanonicalName())) {
      return new IdAGAuditControllers(
        this.services,
        this.configuration,
        this.services.requireService(IdAGStringsType.class))
        .createViewController(null)
        .controller();
    }

    if (Objects.equals(
      param.getCanonicalName(),
      IdAGProfileController.class.getCanonicalName())) {
      return new IdAGProfileControllers(
        this.services,
        this.configuration,
        this.services.requireService(IdAGStringsType.class))
        .createViewController(null)
        .controller();
    }

    throw new IllegalStateException(
      "Unrecognized controller: %s".formatted(param.getCanonicalName())
    );
  }
}
