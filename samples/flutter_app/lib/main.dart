import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';

import 'screens/debug_screen.dart';
import 'screens/evaluate_screen.dart';
import 'screens/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const FlagentApp());
}

class FlagentApp extends StatelessWidget {
  const FlagentApp({super.key});

  @override
  Widget build(BuildContext context) {
    final basePath = Platform.environment['FLAGENT_BASE_URL'] ??
        'http://localhost:18000/api/v1';
    // Android emulator: host localhost is 10.0.2.2
    final resolved = Platform.isAndroid &&
            (basePath.contains('localhost') || basePath.contains('127.0.0.1'))
        ? basePath
            .replaceFirst('localhost', '10.0.2.2')
            .replaceFirst('127.0.0.1', '10.0.2.2')
        : basePath;

    final manager = FlagentManager(
      resolved,
      config: const FlagentConfig(
        cacheTtlMs: 5 * 60 * 1000,
        enableCache: true,
      ),
    );

    return MaterialApp(
      title: 'Flagent Flutter Sample',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: MainScaffold(manager: manager, baseUrl: resolved),
    );
  }
}

class MainScaffold extends StatefulWidget {
  const MainScaffold({
    super.key,
    required this.manager,
    required this.baseUrl,
  });

  final FlagentManager manager;
  final String baseUrl;

  @override
  State<MainScaffold> createState() => _MainScaffoldState();
}

class _MainScaffoldState extends State<MainScaffold> {
  int _selectedIndex = 0;

  @override
  void dispose() {
    widget.manager.destroy();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      HomeScreen(manager: widget.manager, baseUrl: widget.baseUrl),
      EvaluateScreen(manager: widget.manager),
      DebugScreen(manager: widget.manager),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Flagent Sample'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: IndexedStack(
        index: _selectedIndex,
        children: screens,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (i) => setState(() => _selectedIndex = i),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.flag_outlined),
            selectedIcon: Icon(Icons.flag),
            label: 'Evaluate',
          ),
          NavigationDestination(
            icon: Icon(Icons.bug_report_outlined),
            selectedIcon: Icon(Icons.bug_report),
            label: 'Debug',
          ),
        ],
      ),
    );
  }
}
