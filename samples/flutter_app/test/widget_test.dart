import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';
import 'package:flutter_app/screens/home_screen.dart';
import 'package:flutter_app/screens/evaluate_screen.dart';
import 'package:flutter_app/screens/debug_screen.dart';

/// Creates a manager that won't be used for network in build-only tests.
/// Use enableCache: false to avoid background timer.
FlagentManager createTestManager() {
  return FlagentManager(
    'http://localhost:18000/api/v1',
    config: const FlagentConfig(enableCache: false),
  );
}

void main() {
  group('HomeScreen', () {
    testWidgets('shows title and configuration', (tester) async {
      final manager = createTestManager();
      addTearDown(manager.destroy);

      await tester.pumpWidget(
        MaterialApp(
          home: HomeScreen(
            manager: manager,
            baseUrl: 'http://test.example/api/v1',
          ),
        ),
      );

      expect(find.text('Flagent Flutter Sample'), findsOneWidget);
      expect(find.text('Configuration'), findsOneWidget);
      expect(find.text('http://test.example/api/v1'), findsOneWidget);
      expect(find.text('Evaluate'), findsOneWidget);
      expect(find.text('Debug'), findsOneWidget);
    });
  });

  group('EvaluateScreen', () {
    testWidgets('shows form and Evaluate button', (tester) async {
      final manager = createTestManager();
      addTearDown(manager.destroy);

      await tester.pumpWidget(
        MaterialApp(
          home: EvaluateScreen(manager: manager),
        ),
      );

      expect(find.text('Evaluate'), findsOneWidget);
      expect(find.byType(TextField), findsNWidgets(3));
    });
  });

  group('DebugScreen', () {
    testWidgets('shows Debug title and cache buttons', (tester) async {
      final manager = createTestManager();
      addTearDown(manager.destroy);

      await tester.pumpWidget(
        MaterialApp(
          home: DebugScreen(manager: manager),
        ),
      );

      expect(find.text('Debug'), findsOneWidget);
      expect(find.text('Clear cache'), findsOneWidget);
      expect(find.text('Evict expired'), findsOneWidget);
      expect(find.text('Evaluate'), findsWidgets);
    });

    testWidgets('Clear cache updates message', (tester) async {
      final manager = createTestManager();
      addTearDown(manager.destroy);

      await tester.pumpWidget(
        MaterialApp(
          home: DebugScreen(manager: manager),
        ),
      );

      await tester.tap(find.text('Clear cache'));
      await tester.pumpAndSettle();

      expect(find.text('Cache cleared'), findsOneWidget);
    });

    testWidgets('Evict expired updates message', (tester) async {
      final manager = createTestManager();
      addTearDown(manager.destroy);

      await tester.pumpWidget(
        MaterialApp(
          home: DebugScreen(manager: manager),
        ),
      );

      await tester.tap(find.text('Evict expired'));
      await tester.pumpAndSettle();

      expect(find.text('Expired entries evicted'), findsOneWidget);
    });
  });
}
