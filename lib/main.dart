import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/bible_provider.dart';
import 'providers/settings_provider.dart';
import 'screens/splash_screen.dart';
import 'screens/home_screen.dart';
import 'screens/setup_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => BibleProvider()),
        ChangeNotifierProvider(create: (_) => SettingsProvider()),
      ],
      child: const OpenBibleApp(),
    ),
  );
}

class OpenBibleApp extends StatelessWidget {
  const OpenBibleApp({super.key});

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);
    final bibleProvider = Provider.of<BibleProvider>(context);

    return MaterialApp(
      title: 'Veritas Bible',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.light,
        colorSchemeSeed: settingsProvider.themeColor,
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        colorSchemeSeed: settingsProvider.themeColor,
        useMaterial3: true,
      ),
      themeMode: settingsProvider.isDarkMode ? ThemeMode.dark : ThemeMode.light,
      home: FutureBuilder<bool>(
        future: bibleProvider.hasBibleData(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const Scaffold(body: Center(child: CircularProgressIndicator()));
          final hasData = snapshot.data ?? false;
          // 설정상 완료되었으나 실제 데이터가 없는 경우에도 SetupScreen으로 유도
          if (!settingsProvider.isInitialized || !hasData) {
            return const SetupScreen();
          }
          return const SplashScreen();
        },
      ),
    );
  }
}
