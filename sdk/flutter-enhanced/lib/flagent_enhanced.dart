library flagent_enhanced;

export 'src/config/flagent_config.dart';
export 'src/cache/evaluation_cache.dart';
export 'src/flagent.dart' show Flagent, FlagentOptions;
export 'src/manager/flagent_manager.dart' show FlagentManager, buildEvaluationEntity;

// Re-export for convenience
export 'package:flagent_client/flagent_client.dart'
    show EvaluationEntity, EvalResult;
