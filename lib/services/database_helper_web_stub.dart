// Stub implementation for non-web platforms
import 'package:sqflite/sqflite.dart';

DatabaseFactory get databaseFactoryWeb {
  throw UnsupportedError('databaseFactoryWeb is not supported on this platform');
}