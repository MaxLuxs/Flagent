import 'package:flutter/material.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';

class DebugScreen extends StatefulWidget {
  const DebugScreen({super.key, required this.manager});

  final FlagentManager manager;

  @override
  State<DebugScreen> createState() => _DebugScreenState();
}

class _DebugScreenState extends State<DebugScreen> {
  final _flagKeyController = TextEditingController(text: 'my_feature_flag');
  final _entityIdController = TextEditingController(text: 'user123');
  final _entityTypeController = TextEditingController(text: 'user');

  EvalResult? _lastResult;
  String? _error;
  bool _loading = false;
  String? _cacheMessage;
  final List<EvalResult> _lastEvals = [];
  static const int _maxLastEvals = 10;

  @override
  void dispose() {
    _flagKeyController.dispose();
    _entityIdController.dispose();
    _entityTypeController.dispose();
    super.dispose();
  }

  Future<void> _evaluate() async {
    setState(() {
      _error = null;
      _cacheMessage = null;
      _loading = true;
    });

    try {
      final r = await widget.manager.evaluate(
        flagKey: _flagKeyController.text.trim().isEmpty
            ? null
            : _flagKeyController.text.trim(),
        entityID: _entityIdController.text.trim().isEmpty
            ? null
            : _entityIdController.text.trim(),
        entityType: _entityTypeController.text.trim().isEmpty
            ? null
            : _entityTypeController.text.trim(),
        enableDebug: true,
      );
      setState(() {
        _lastResult = r;
        _loading = false;
        while (_lastEvals.length >= _maxLastEvals) _lastEvals.removeLast();
        _lastEvals.insert(0, r);
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  Future<void> _clearCache() async {
    await widget.manager.clearCache();
    setState(() => _cacheMessage = 'Cache cleared');
  }

  Future<void> _evictExpired() async {
    await widget.manager.evictExpired();
    setState(() => _cacheMessage = 'Expired entries evicted');
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            'Debug',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Evaluate', style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _flagKeyController,
                    decoration: const InputDecoration(
                      labelText: 'Flag key',
                      border: OutlineInputBorder(),
                      isDense: true,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: _entityIdController,
                    decoration: const InputDecoration(
                      labelText: 'Entity ID',
                      border: OutlineInputBorder(),
                      isDense: true,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: _entityTypeController,
                    decoration: const InputDecoration(
                      labelText: 'Entity type',
                      border: OutlineInputBorder(),
                      isDense: true,
                    ),
                  ),
                  const SizedBox(height: 12),
                  FilledButton.icon(
                    onPressed: _loading ? null : _evaluate,
                    icon: _loading
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.play_arrow, size: 20),
                    label: Text(_loading ? '…' : 'Evaluate'),
                  ),
                ],
              ),
            ),
          ),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Card(
              color: Theme.of(context).colorScheme.errorContainer,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Text(
                  _error!,
                  style: TextStyle(
                    fontSize: 12,
                    color: Theme.of(context).colorScheme.onErrorContainer,
                  ),
                ),
              ),
            ),
          ],
          if (_lastResult != null) ...[
            const SizedBox(height: 12),
            Card(
              color: Theme.of(context).colorScheme.primaryContainer,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Last result',
                      style: Theme.of(context).textTheme.titleSmall,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'flagKey: ${_lastResult!.flagKey ?? "—"}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).colorScheme.onPrimaryContainer,
                      ),
                    ),
                    Text(
                      'variantKey: ${_lastResult!.variantKey ?? "—"}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).colorScheme.onPrimaryContainer,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
          const SizedBox(height: 16),
          Text('Cache', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Row(
            children: [
              FilledButton.tonal(
                onPressed: _clearCache,
                child: const Text('Clear cache'),
              ),
              const SizedBox(width: 8),
              FilledButton.tonal(
                onPressed: _evictExpired,
                child: const Text('Evict expired'),
              ),
            ],
          ),
          if (_cacheMessage != null) ...[
            const SizedBox(height: 8),
            Text(
              _cacheMessage!,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
          if (_lastEvals.isNotEmpty) ...[
            const SizedBox(height: 24),
            Text(
              'Last evaluations (${_lastEvals.length})',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            ..._lastEvals.take(10).map(
                  (r) => Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      dense: true,
                      title: Text(
                        '${r.flagKey ?? "?"} → ${r.variantKey ?? "—"}',
                        style: const TextStyle(fontSize: 13),
                      ),
                      subtitle: r.evalContext?.entityID != null
                          ? Text(
                              'entity: ${r.evalContext!.entityID}',
                              style: const TextStyle(fontSize: 11),
                            )
                          : null,
                    ),
                  ),
                ),
          ],
        ],
      ),
    );
  }
}
