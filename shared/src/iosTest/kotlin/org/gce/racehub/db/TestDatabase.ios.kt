package org.gce.racehub.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver as nativeInMemoryDriver

internal actual fun inMemoryDriver(): SqlDriver = nativeInMemoryDriver(RaceHubDatabase.Schema)
