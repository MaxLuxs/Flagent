import 'package:flutter/material.dart';
import 'package:flagent_enhanced/flagent_enhanced.dart';

class EvaluateScreen extends StatefulWidget {
  const EvaluateScreen({super.key, required this.manager});

  final FlagentManager manager;

  @override
  State<EvaluateScreen> createState() => _EvaluateScreenState();
}

class _EvaluateScreenState extends State<EvaluateScreen> {
  final _flagKeyController = TextEditingController(text: 'my_feature_flag');
  final _entityIdController = TextEditingController(text: 'user123');
  final _entityTypeController = TextEditingController(text: 'user');

  EvalResult? _result;
  String? _error;
  bool _loading = false;

  @override
  void dispose() {
    _flagKeyController.dispose();
    _entityIdController.dispose();
    _entityTypeController.dispose();
    super.dispose();
  }

  Future<void> _evaluate() async {
    setState(() {
      _result = null;
      _error = null;
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
        entityContext: {'region': 'US', 'tier': 'premium'},
        enableDebug: true,
      );
      setState(() {
        _result = r;
        _error = null;
        _loading = false;
      });
    } catch (e, st) {
      setState(() {
        _error = e.toString();
        _result = null;
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TextField(
            controller: _flagKeyController,
            decoration: const InputDecoration(
              labelText: 'Flag key',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _entityIdController,
            decoration: const InputDecoration(
              labelText: 'Entity ID',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _entityTypeController,
            decoration: const InputDecoration(
              labelText: 'Entity type',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 24),
          FilledButton.icon(
            onPressed: _loading ? null : _evaluate,
            icon: _loading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.play_arrow),
            label: Text(_loading ? 'Evaluating…' : 'Evaluate'),
          ),
          if (_error != null) ...[
            const SizedBox(height: 16),
            Card(
              color: Theme.of(context).colorScheme.errorContainer,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.onErrorContainer)),
              ),
            ),
          ],
          if (_result != null) ...[
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Result', style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 8),
                    _ResultRow(label: 'Flag key', value: _result!.flagKey),
                    _ResultRow(label: 'Variant key', value: _result!.variantKey),
                    _ResultRow(label: 'Flag ID', value: _result!.flagID?.toString()),
                    _ResultRow(label: 'Variant ID', value: _result!.variantID?.toString()),
                    if (_result!.evalDebugLog != null)
                      _ResultRow(
                        label: 'Segment ID',
                        value: _result!.evalDebugLog!.segmentID?.toString(),
                      ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _ResultRow extends StatelessWidget {
  const _ResultRow({required this.label, required this.value});

  final String label;
  final String? value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Text('$label: ${value ?? '—'}'),
    );
  }
}
