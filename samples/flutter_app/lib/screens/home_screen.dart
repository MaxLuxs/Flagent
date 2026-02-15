import 'package:flutter/material.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({
    super.key,
    required this.manager,
    required this.baseUrl,
  });

  final FlagentManager manager;
  final String baseUrl;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            'Flagent Flutter Sample',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 8),
          Text(
            'This app demonstrates the Flagent SDK with UI: single evaluation, batch evaluation, and a debug screen with cache controls.',
            style: Theme.of(context).textTheme.bodyLarge,
          ),
          const SizedBox(height: 24),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Configuration',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 8),
                  Text('Base URL: $baseUrl'),
                  const Text('Cache: enabled (5 min TTL)'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Tabs',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 8),
                  const ListTile(
                    leading: Icon(Icons.flag),
                    title: Text('Evaluate'),
                    subtitle: Text(
                      'Single flag evaluation by key or ID with entity context.',
                    ),
                  ),
                  const ListTile(
                    leading: Icon(Icons.bug_report),
                    title: Text('Debug'),
                    subtitle: Text(
                      'Evaluate form, result, cache actions (clear, evict), last evaluations.',
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
