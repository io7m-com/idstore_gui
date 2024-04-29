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

import com.io7m.idstore.model.IdAdmin;
import com.io7m.idstore.model.IdAdminPermissionSet;
import com.io7m.idstore.model.IdEmail;
import com.io7m.idstore.model.IdName;
import com.io7m.idstore.model.IdNonEmptyList;
import com.io7m.idstore.model.IdPasswordAlgorithmPBKDF2HmacSHA256;
import com.io7m.idstore.model.IdPasswordException;
import com.io7m.idstore.model.IdRealName;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class IdTestAdmins
{
  public static final IdAdmin TEST_ADMIN_0;
  public static final IdAdmin TEST_ADMIN_1;

  static {
    try {
      TEST_ADMIN_0 =
        new IdAdmin(
          UUID.randomUUID(),
          new IdName("admin"),
          new IdRealName("Admin"),
          new IdNonEmptyList<>(new IdEmail("someone@example.com"), List.of()),
          OffsetDateTime.now(),
          OffsetDateTime.now(),
          IdPasswordAlgorithmPBKDF2HmacSHA256.create()
            .createHashed("12345678"),
          IdAdminPermissionSet.all()
        );

      TEST_ADMIN_1 =
        new IdAdmin(
          UUID.randomUUID(),
          new IdName("admin1"),
          new IdRealName("Admin1"),
          new IdNonEmptyList<>(new IdEmail("someone1@example.com"), List.of()),
          OffsetDateTime.now(),
          OffsetDateTime.now(),
          IdPasswordAlgorithmPBKDF2HmacSHA256.create()
            .createHashed("12345678"),
          IdAdminPermissionSet.empty()
        );
    } catch (final IdPasswordException e) {
      throw new IllegalStateException(e);
    }
  }

  private IdTestAdmins()
  {

  }
}
