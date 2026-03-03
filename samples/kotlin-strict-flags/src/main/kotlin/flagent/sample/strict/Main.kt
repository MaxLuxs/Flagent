package flagent.sample.strict

import flagent.sample.strict.generated.FlagKeys

/**
 * Sample that uses only generated FlagKeys (strict mode).
 * Raw strings in isEnabled("...") would fail the build.
 */
fun main() {
    println("Strict mode sample: use FlagKeys.* only")
    println("Flag key for new_feature: ${FlagKeys.NEW_FEATURE}")
    println("Flag key for demo_flag: ${FlagKeys.DEMO_FLAG}")
}
