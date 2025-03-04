/***************************************************************************
                            platformutilities.h  -  utilities for sigpacgo
                              -------------------
              begin                : Wed Dec 04 10:48:28 CET 2015
              copyright            : (C) 2015 by Marco Bernasocchi
              email                : marco@opengis.ch
 ***************************************************************************/

#ifndef PLATFORMUTILITIES_H
#define PLATFORMUTILITIES_H

// ...existing code...

/**
 * This method will do initialization tasks and copy sample projects to a writable location
 * It will also call afterUpdate whenever a new version is detected.
 */
void initSystem();

/**
 * The path to share data location.
 * Under this path, there should be the app specific directories qgis/ proj/ sigpacgo/ ...
 * Refers to /share or /usr/share on Linux.
 * This path is assumed to be read only.
 */
virtual QString systemSharedDataLocation() const;

// ...existing code...

/**
 * The main application directory within which projects and datasets can be imported.
 */
Q_INVOKABLE virtual QString applicationDirectory() const;

// ...existing code...

QgsMessageLog::logMessage( tr( "Failed to save file resource" ), "SIGPAC-Go", Qgis::Critical );

// ...existing code...

#endif // PLATFORMUTILITIES_H
