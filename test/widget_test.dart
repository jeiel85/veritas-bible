import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:veritas_bible/main.dart';
import 'package:veritas_bible/providers/bible_provider.dart';
import 'package:veritas_bible/providers/settings_provider.dart';

void main() {
  testWidgets('앱 루트가 정상적으로 기동된다', (WidgetTester tester) async {
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          ChangeNotifierProvider(create: (_) => BibleProvider()),
          ChangeNotifierProvider(create: (_) => SettingsProvider()),
        ],
        child: const OpenBibleApp(),
      ),
    );

    await tester.pump();

    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
