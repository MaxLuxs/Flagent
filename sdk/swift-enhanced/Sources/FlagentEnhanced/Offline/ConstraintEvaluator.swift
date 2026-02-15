import Foundation

/// Evaluates constraints against entity context (EQ, NEQ, IN, etc.). Matches shared ConstraintEvaluator.
struct ConstraintEvaluator {
    func evaluate(constraints: [OfflineLocalConstraint], context: [String: String]) -> Bool {
        if constraints.isEmpty { return true }
        return constraints.allSatisfy { evaluateConstraint($0, context: context) }
    }

    private func evaluateConstraint(_ c: OfflineLocalConstraint, context: [String: String]) -> Bool {
        guard let propertyValue = context[c.property] else { return false }
        let constraintValue = c.value
        switch c.operator_ {
        case "EQ": return equals(propertyValue, constraintValue)
        case "NEQ": return !equals(propertyValue, constraintValue)
        case "LT": return compareNumbers(propertyValue, constraintValue) { $0 < $1 }
        case "LTE": return compareNumbers(propertyValue, constraintValue) { $0 <= $1 }
        case "GT": return compareNumbers(propertyValue, constraintValue) { $0 > $1 }
        case "GTE": return compareNumbers(propertyValue, constraintValue) { $0 >= $1 }
        case "EREG": return matchesRegex(propertyValue, constraintValue)
        case "NEREG": return !matchesRegex(propertyValue, constraintValue)
        case "IN": return inList(propertyValue, constraintValue)
        case "NOTIN": return !inList(propertyValue, constraintValue)
        case "CONTAINS": return propertyValue.contains(constraintValue)
        case "NOTCONTAINS": return !propertyValue.contains(constraintValue)
        default: return false
        }
    }

    private func equals(_ a: String, _ b: String) -> Bool { a == b }

    private func compareNumbers(_ prop: String, _ constraint: String, _ cmp: (Double, Double) -> Bool) -> Bool {
        guard let a = Double(prop), let b = Double(constraint) else { return false }
        return cmp(a, b)
    }

    private func matchesRegex(_ prop: String, _ pattern: String) -> Bool {
        guard let regex = try? NSRegularExpression(pattern: pattern) else { return false }
        let range = NSRange(prop.startIndex..., in: prop)
        return regex.firstMatch(in: prop, range: range) != nil
    }

    private func inList(_ prop: String, _ list: String) -> Bool {
        let values = list.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }
        return values.contains(prop)
    }
}
