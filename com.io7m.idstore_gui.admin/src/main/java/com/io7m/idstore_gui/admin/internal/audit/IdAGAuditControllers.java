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

package com.io7m.idstore_gui.admin.internal.audit;

import com.io7m.idstore_gui.admin.IdAGConfiguration;
import com.io7m.idstore_gui.admin.internal.IdAGStringsType;
import com.io7m.idstore_gui.admin.internal.view_controllers.IdAGViewControllerFactoryAbstract;
import com.io7m.repetoir.core.RPServiceDirectoryType;
import javafx.stage.Stage;

/**
 * A factory of controllers.
 */

public final class IdAGAuditControllers
  extends IdAGViewControllerFactoryAbstract<Void, IdAGAuditController>
{
  /**
   * A factory of controllers.
   *
   * @param inServices      The service directory
   * @param inConfiguration The configuration
   * @param inStrings       The strings
   */

  public IdAGAuditControllers(
    final RPServiceDirectoryType inServices,
    final IdAGConfiguration inConfiguration,
    final IdAGStringsType inStrings)
  {
    super(
      IdAGAuditController.class,
      "/com/io7m/idstore_gui/admin/internal/tabAudit.fxml",
      inServices,
      inConfiguration,
      inStrings
    );
  }

  @Override
  protected String createStageTitle(
    final Void arguments)
  {
    return "";
  }

  @Override
  protected IdAGAuditController createController(
    final Void ignored,
    final Stage stage)
  {
    return new IdAGAuditController(
      this.services(),
      this.configuration()
    );
  }
}
