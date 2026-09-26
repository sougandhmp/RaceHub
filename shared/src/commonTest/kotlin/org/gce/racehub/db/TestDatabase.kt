package org.gce.racehub.db

import app.cash.sqldelight.db.SqlDriver

/** A fresh, empty in-memory RaceHub database with the schema created. */
internal expect fun inMemoryDriver(): SqlDriver
