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

/**
 * Identity server (Admin UI tool)
 */

open module com.io7m.idstore_gui.admin
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.idstore.admin_client.api;
  requires com.io7m.idstore.admin_client;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;

  requires com.io7m.jade.api;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jproperties.core;
  requires com.io7m.junreachable.core;
  requires com.io7m.jxtrand.api;
  requires com.io7m.repetoir.core;
  requires com.io7m.seltzer.api;
  requires com.io7m.taskrecorder.core;
  requires org.slf4j;

  exports com.io7m.idstore_gui.admin;

  exports com.io7m.idstore_gui.admin.internal
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.admins
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.users
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.main
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.login
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.preferences
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.client
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.events
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.about
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.audit
    to com.io7m.idstore_gui.tests;
  exports com.io7m.idstore_gui.admin.internal.profile
    to com.io7m.idstore_gui.tests;
}
