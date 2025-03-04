/***************************************************************************
                            platformutilities.cpp  -  utilities for sigpacgo
                              -------------------
              begin                : Wed Dec 04 10:48:28 CET 2015
              copyright            : (C) 2015 by Marco Bernasocchi
              email                : marco@opengis.ch
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include "appinterface.h"
#include "fileutils.h"
#include "platformutilities.h"
#include "projectsource.h"
#include "qfield.h"
#include "qfieldcloudconnection.h"
#include "qgsmessagelog.h"
#include "resourcesource.h"
#include "stringutils.h"
#include "urlutils.h"

// ...existing code...

void PlatformUtilities::copySampleProjects()
{
  const bool success = FileUtils::copyRecursively( systemSharedDataLocation() + QLatin1String( "/sigpacgo/sample_projects" ), systemLocalDataLocation( QLatin1String( "sample_projects" ) ) );
  Q_ASSERT( success );
}

void PlatformUtilities::initSystem()
{
  // ...existing code...
}

void PlatformUtilities::afterUpdate()
{
  const QStringList dirs = appDataDirs();
  for ( const QString &dir : dirs )
  {
    QDir appDataDir( dir );
    appDataDir.mkpath( QStringLiteral( "proj" ) );
    appDataDir.mkpath( QStringLiteral( "auth" ) );
    appDataDir.mkpath( QStringLiteral( "fonts" ) );
    appDataDir.mkpath( QStringLiteral( "basemaps" ) );
    appDataDir.mkpath( QStringLiteral( "logs" ) );
    appDataDir.mkpath( QStringLiteral( "plugins" ) );
  }

  QDir applicationDir( applicationDirectory() );
  applicationDir.mkpath( QStringLiteral( "Imported Projects" ) );
  applicationDir.mkpath( QStringLiteral( "Imported Datasets" ) );
}

QString PlatformUtilities::systemSharedDataLocation() const
{
  /**
   * By default, assume that we have a layout like this:
   *
   * [prefix_path]
   * |-- bin
   * |   |-- sigpacgo.exe
   * |-- share
   * |   |-- sigpacgo
   * |   |   |-- sample_projects
   * |   |-- proj
   * |   |   |-- data
   * |   |   |   |--  proj.db
   *
   * systemSharedDataLocation()'s return value will therefore be - relative to sigpacgo.exe - '../share'.
   * However it is possible to override this default logic through a environment variable named
   * SIGPACGO_SYSTEM_SHARED_DATA_PATH. If present, its value will be used as the return value instead.
  */
  const static QString sharePath = QDir( QFileInfo( !QCoreApplication::applicationFilePath().isEmpty() ? QCoreApplication::applicationFilePath() : QCoreApplication::arguments().value( 0 ) ).canonicalPath()
                                         + QLatin1String( "/../share" ) )
                                     .absolutePath();
  const static QString environmentSharePath = QString( qgetenv( "SIGPACGO_SYSTEM_SHARED_DATA_PATH" ) );
  return !environmentSharePath.isEmpty() ? QDir( environmentSharePath ).absolutePath() : sharePath;
}

// ...existing code...

QStringList PlatformUtilities::appDataDirs() const
{
  return QStringList() << QStandardPaths::standardLocations( QStandardPaths::DocumentsLocation ).first() + QStringLiteral( "/sigpacgo/" );
}

QString PlatformUtilities::applicationDirectory() const
{
  return QStandardPaths::standardLocations( QStandardPaths::DocumentsLocation ).first() + QStringLiteral( "/sigpacgo/" );
}

// ...existing code...

QgsMessageLog::logMessage( tr( "Failed to save file resource" ), "SIGPAC-Go", Qgis::Critical );

// ...existing code...

ProjectSource *PlatformUtilities::openProject( QObject * )
{
  QSettings settings;
  ProjectSource *source = new ProjectSource();
  QString fileName { QFileDialog::getOpenFileName( nullptr,
                                                   tr( "Open File" ),
                                                   settings.value( QStringLiteral( "sigpacgo/lastOpenDir" ), QString() ).toString(),
                                                   QStringLiteral( "%1 (*.%2);;%3 (*.%4);;%5 (*.%6);;%7 (*.%8)" ).arg( tr( "All Supported Files" ), ( SUPPORTED_PROJECT_EXTENSIONS + SUPPORTED_VECTOR_EXTENSIONS + SUPPORTED_RASTER_EXTENSIONS ).join( QStringLiteral( " *." ) ), tr( "QGIS Project Files" ), SUPPORTED_PROJECT_EXTENSIONS.join( QStringLiteral( " *." ) ), tr( "Vector Datasets" ), SUPPORTED_VECTOR_EXTENSIONS.join( QStringLiteral( " *." ) ), tr( "Raster Datasets" ), SUPPORTED_RASTER_EXTENSIONS.join( QStringLiteral( " *." ) ) ) ) };
  if ( !fileName.isEmpty() )
  {
    settings.setValue( QStringLiteral( "/sigpacgo/lastOpenDir" ), QFileInfo( fileName ).absolutePath() );
    QTimer::singleShot( 0, this, [source, fileName]() { emit source->projectOpened( fileName ); } );
  }
  return source;
}

// ...existing code...

PlatformUtilities *PlatformUtilities::instance()
{
  return sPlatformUtils;
}

// ...existing code...
